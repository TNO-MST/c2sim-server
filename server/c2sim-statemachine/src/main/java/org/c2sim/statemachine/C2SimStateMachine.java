package org.c2sim.statemachine;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.github.oxo42.stateless4j.delegates.FuncBoolean;
import com.github.oxo42.stateless4j.transitions.Transition;
import com.sun.istack.NotNull;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.c2sim.lox.C2SimMsgKind;
import org.c2sim.lox.helpers.XmlFactoryHelper;
import org.c2sim.lox.schema.C2SIMHeaderType;
import org.c2sim.lox.schema.MessageType;

/**
 * C2SIM simulation lifecycle state machine built on <a
 * href="https://github.com/oxo42/stateless4j">stateless4j</a>.
 *
 * <p>Incoming C2SIM XML messages that map to state-machine triggers are validated via {@link
 * #isC2SimMsgAllowedInCurrentState(C2SimMsgKind)}.
 *
 * <p>Use {@link Builder} for fluent construction:
 *
 * <pre>{@code
 * C2SimStateMachine sm = new C2SimStateMachine.Builder()
 *     .listener(myListener)
 *     .build();
 * sm.fireTrigger(Trigger.SUBMIT_INITIALIZATION);
 * }</pre>
 */
public class C2SimStateMachine {

  private static final Set<State> executingStates = Set.of(State.RUNNING, State.PAUSED);

  // What C2SIM XML messages are allowed in what states
  // When not in list, allowed in all states....

  private static final Map<C2SimMsgKind, Set<State>> allowedMsgState =
      Map.of(
          // For C2SimMsgKind defined in state machine, the state machine is used
          // C2SimMsgKind.StartScenario
          // C2SimMsgKind.StopScenario
          // C2SimMsgKind.SubmitInitialization
          // C2SimMsgKind.C2SIMInitialization
          // C2SimMsgKind.InitializationComplete
          // These msg are not in state machine, do manually:
          C2SimMsgKind.OBJECT_INITIALIZATION, Set.of(State.INITIALIZING),
          C2SimMsgKind.C2SIM_INITIALIZATION, Set.of(State.INITIALIZING),
          C2SimMsgKind.REPORT, executingStates,
          C2SimMsgKind.ORDER, executingStates,
          C2SimMsgKind.MAGIC_MOVE, executingStates,
          C2SimMsgKind.SET_SIMULATION_REALTIME_MULTIPLE, executingStates,
          C2SimMsgKind.CHECKPOINT_RESTORE, executingStates);

  /** Map the ENUM trigger from this module on the XSD message */
  private static final Map<Trigger, C2SimMsgKind> xml2cmdMapping =
      Map.of(
          Trigger.RESET_SCENARIO, C2SimMsgKind.RESET_SCENARIO,
          Trigger.START_SCENARIO, C2SimMsgKind.START_SCENARIO,
          Trigger.STOP_SCENARIO, C2SimMsgKind.STOP_SCENARIO,
          Trigger.PAUSE_SCENARIO, C2SimMsgKind.PAUSE_SCENARIO,
          Trigger.RESUME_SCENARIO, C2SimMsgKind.RESUME_SCENARIO,
          Trigger.SHARE_SCENARIO, C2SimMsgKind.SHARE_SCENARIO,
          Trigger.SUBMIT_INITIALIZATION, C2SimMsgKind.SUBMIT_INITIALIZATION,
          Trigger.INITIALIZATION_COMPLETE, C2SimMsgKind.INITIALIZATION_COMPLETE);

  private final StateMachine<State, Trigger> machine;
  private final List<Edge> edges = new ArrayList<>(); // for Mermaid export
  private final StateMachineListener stateMachineListener;
  private final AtomicReference<State> state = new AtomicReference<>(State.UNINITIALIZED);

  /**
   * Creates a state machine wired to the given listener.
   *
   * <p>The machine starts in {@link State#UNINITIALIZED}. Prefer {@link Builder} for construction.
   *
   * @param listener the lifecycle listener; must not be {@code null}
   */
  public C2SimStateMachine(@NotNull StateMachineListener listener) {
    this.stateMachineListener = listener;
    StateMachineConfig<State, Trigger> cfg = new StateMachineConfig<>();

    // --- UNINITIALIZED ---
    cfg.configure(State.UNINITIALIZED)
        .permit(Trigger.SUBMIT_INITIALIZATION, State.INITIALIZING)
        .onEntry(stateMachineListener::onEnterStateUninitialized);

    // --- INITIALIZING ---
    cfg.configure(State.INITIALIZING)
        .ignore(Trigger.C2SIM_INITIALIZATION_BODY) //
        .ignore(Trigger.INITIALIZATION_COMPLETE) // Each system will send INITIALIZATION_COMPLETE
        .ignore(Trigger.OBJECT_INITIALIZATION) // Not used at the moment
        .permit(Trigger.RESET_SCENARIO, State.UNINITIALIZED)
        .permitIf(
            Trigger.SHARE_SCENARIO,
            State.INITIALIZED,
            stateMachineListener.allSystemsAreInitialized())
        .onEntry(stateMachineListener::onEnterStateInitializing);

    // --- INITIALIZED ---
    cfg.configure(State.INITIALIZED)
        .permit(Trigger.START_SCENARIO, State.RUNNING)
        .permit(Trigger.RESET_SCENARIO, State.UNINITIALIZED)
        .onEntry(stateMachineListener::onEnterStateInitialized);

    // --- RUNNING ---
    cfg.configure(State.RUNNING)
        .permit(Trigger.PAUSE_SCENARIO, State.PAUSED)
        .permit(Trigger.STOP_SCENARIO, State.INITIALIZED)
        .onEntry(stateMachineListener::onEnterStateRunning);
    // --- PAUSED ---
    cfg.configure(State.PAUSED)
        .permit(Trigger.RESUME_SCENARIO, State.RUNNING)
        .permit(Trigger.STOP_SCENARIO, State.INITIALIZED)
        .onEntry(stateMachineListener::onEnterStatePaused);

    this.machine = new StateMachine<>(State.UNINITIALIZED, state::get, state::set, cfg);

    // Used to create UML plant notation
    edge(State.UNINITIALIZED, Trigger.SUBMIT_INITIALIZATION, State.INITIALIZING);
    edge(State.INITIALIZING, Trigger.SHARE_SCENARIO, State.INITIALIZED);
    edge(State.INITIALIZED, Trigger.START_SCENARIO, State.RUNNING);
    edge(State.INITIALIZED, Trigger.RESET_SCENARIO, State.UNINITIALIZED);
    edge(State.RUNNING, Trigger.PAUSE_SCENARIO, State.PAUSED);
    edge(State.RUNNING, Trigger.STOP_SCENARIO, State.INITIALIZED);
    edge(State.PAUSED, Trigger.START_SCENARIO, State.RUNNING);
  }

  /**
   * Returns {@code true} if the given message kind is handled by the state machine (i.e. it maps to
   * a {@link Trigger}).
   *
   * @param kind the C2SIM message kind to test
   * @return {@code true} if {@code kind} has a corresponding trigger
   */
  public static boolean isStateMachineMessage(C2SimMsgKind kind) {
    return xml2cmdMapping.containsValue(kind);
  }

  /**
   * Returns the first key in {@code map} whose value equals {@code value}.
   *
   * @param <K> the key type
   * @param <V> the value type
   * @param map the map to search
   * @param value the value to look for
   * @param defaultValue the value to return when no matching key is found
   * @return the matching key, or {@code defaultValue} if not found
   */
  public static <K, V> K getKeyByValue(Map<K, V> map, V value, K defaultValue) {
    for (Map.Entry<K, V> entry : map.entrySet()) {
      if (Objects.equals(entry.getValue(), value)) {
        return entry.getKey();
      }
    }
    return defaultValue; // not found
  }

  /**
   * Converts a {@link Trigger} to the corresponding {@link C2SimMsgKind}.
   *
   * @param trigger the trigger to convert
   * @return the matching message kind, or {@link C2SimMsgKind#UNKNOWN} if not mapped
   */
  public static C2SimMsgKind convert(Trigger trigger) {
    return xml2cmdMapping.getOrDefault(trigger, C2SimMsgKind.UNKNOWN);
  }

  /**
   * Converts a {@link C2SimMsgKind} to the corresponding {@link Trigger}.
   *
   * @param kind the message kind to convert
   * @return the matching trigger, or {@link Trigger#UNKNOWN} if not mapped
   */
  public static Trigger convert(C2SimMsgKind kind) {
    return getKeyByValue(xml2cmdMapping, kind, Trigger.UNKNOWN);
  }

  /**
   * Creates the C2SIM {@link MessageType} XML message that corresponds to the given trigger.
   *
   * @param trigger the state-machine trigger
   * @param header the C2SIM message header to attach
   * @return the constructed message
   * @throws IllegalArgumentException if the trigger has no corresponding XML message factory
   */
  public static MessageType createMessageForTrigger(Trigger trigger, C2SIMHeaderType header) {
    Objects.requireNonNull(trigger, "Trigger has no value (null)");

    return switch (trigger) {
      case RESET_SCENARIO -> XmlFactoryHelper.createReset(header);
      case SUBMIT_INITIALIZATION -> XmlFactoryHelper.createSubmitInitialization(header);
      case START_SCENARIO -> XmlFactoryHelper.createStartScenario(header);
      case STOP_SCENARIO -> XmlFactoryHelper.createStopScenario(header);
      case SHARE_SCENARIO -> XmlFactoryHelper.createShareScenario(header);
      case PAUSE_SCENARIO -> XmlFactoryHelper.createPauseScenario(header);
      case RESUME_SCENARIO -> XmlFactoryHelper.createResumeScenario(header);
      case INITIALIZATION_COMPLETE -> XmlFactoryHelper.createInitializationComplete(header);
      case C2SIM_INITIALIZATION_BODY, OBJECT_INITIALIZATION, UNKNOWN -> null;
    };
  }

  /**
   * Returns {@code true} if the given message kind is permitted in the current state.
   *
   * <p>For message kinds that are part of the state machine the permitted-trigger list is
   * consulted. For message kinds that are not part of the state machine (e.g. {@link
   * C2SimMsgKind#REPORT}, {@link C2SimMsgKind#ORDER}) the configured allowed-state map is used. All
   * other message kinds are unrestricted.
   *
   * @param kind the C2SIM message kind to check
   * @return {@code true} if the message is allowed in the current state
   */
  public boolean isC2SimMsgAllowedInCurrentState(C2SimMsgKind kind) {
    // Use state machine to validate if message is allowed
    if (isStateMachineMessage(kind)) {
      return machine.getPermittedTriggers().contains(convert(kind));
    }
    // For messages that are not in state machine
    if (allowedMsgState.containsKey(kind)) {
      return allowedMsgState.getOrDefault(kind, Set.of()).contains(machine.getState());
    }
    // Set to false for 100% enforcement
    return true;
  }

  /**
   * Returns {@code true} if the given trigger can be fired from the current state.
   *
   * @param trigger the trigger to test
   * @return {@code true} if the trigger is permitted
   */
  public boolean isTriggerAllowed(Trigger trigger) {
    return machine.canFire(trigger);
  }

  /**
   * Fires the given trigger, causing a state transition.
   *
   * @param trigger the trigger to fire
   * @throws IllegalStateException if the trigger is not permitted in the current state
   */
  public void fireTrigger(Trigger trigger) {
    machine.fire(trigger); // throws IllegalStateException if not allowed
  }

  /**
   * Returns the current state of the machine.
   *
   * @return the current {@link State}
   */
  public State getCurrentState() {
    return machine.getState();
  }

  /**
   * Returns the triggers that are permitted from the current state.
   *
   * @return an set of permitted triggers
   */
  public Set<Trigger> getPermittedTriggers() {
    return EnumSet.copyOf(machine.getPermittedTriggers());
  }

  /**
   * Force to a state; use only in special cases
   *
   * @param state the state to jump to
   */
  public void setState(State state) {
    this.state.set(state);
  }

  /**
   * Mermaid state diagram highlighting the current state. Paste the output into
   * https://mermaid.live (use "stateDiagram-v2").
   *
   * @return a Mermaid {@code stateDiagram-v2} string with the current state highlighted
   */
  public String toMermaid() {
    StringBuilder sb = new StringBuilder();
    sb.append("stateDiagram-v2\n");
    sb.append("    [*] --> ").append(State.UNINITIALIZED.name()).append("\n");
    for (Edge e : edges) {
      sb.append("    ")
          .append(e.from.name())
          .append(" --> ")
          .append(e.to.name())
          .append(": ")
          .append(e.trigger.name())
          .append("\n");
    }
    sb.append("\n");
    // Highlight current state
    sb.append("    classDef highlight fill:#f96,stroke:#333,stroke-width:4px;\n");
    sb.append("    class ").append(getCurrentState().name()).append(" highlight;\n");
    return sb.toString();
  }

  // --- helpers ---
  private void edge(State from, Trigger trig, State to) {
    edges.add(new Edge(from, trig, to));
  }

  /**
   * Fires the trigger that corresponds to the given C2SIM message kind.
   *
   * @param kind the message kind whose corresponding trigger is fired
   * @throws IllegalStateException if the derived trigger is not permitted in the current state
   */
  public void fireTrigger(C2SimMsgKind kind) {
    machine.fire(convert(kind));
  }

  /**
   * Listener interface for C2SIM state-machine lifecycle callbacks.
   *
   * <p>All methods have default no-op implementations. Override the entry callbacks you need and
   * optionally override {@link #allSystemsAreInitialized()} to control the guard on the {@link
   * Trigger#SHARE_SCENARIO} transition.
   */
  public interface StateMachineListener {
    /**
     * Called when the machine enters {@link State#UNINITIALIZED}.
     *
     * @param transition the transition that caused the entry
     */
    default void onEnterStateUninitialized(Transition<State, Trigger> transition) {}

    /**
     * Called when the machine enters {@link State#INITIALIZING}.
     *
     * @param transition the transition that caused the entry
     */
    default void onEnterStateInitializing(Transition<State, Trigger> transition) {}

    /**
     * Called when the machine enters {@link State#INITIALIZED}.
     *
     * @param transition the transition that caused the entry
     */
    default void onEnterStateInitialized(Transition<State, Trigger> transition) {}

    /**
     * Called when the machine enters {@link State#RUNNING}.
     *
     * @param transition the transition that caused the entry
     */
    default void onEnterStateRunning(Transition<State, Trigger> transition) {}

    /**
     * Called when the machine enters {@link State#PAUSED}.
     *
     * @param transition the transition that caused the entry
     */
    default void onEnterStatePaused(Transition<State, Trigger> transition) {}

    /**
     * Returns the guard function for the {@link Trigger#SHARE_SCENARIO} transition.
     *
     * <p>The transition from {@link State#INITIALIZING} to {@link State#INITIALIZED} is only
     * permitted when this function returns {@code true}. The default implementation always returns
     * {@code true}.
     *
     * @return a {@link FuncBoolean} that evaluates whether all systems have initialized
     */
    default FuncBoolean allSystemsAreInitialized() {
      return () -> true;
    }
  }

  /** Fluent builder for {@link C2SimStateMachine}. */
  public static class Builder {
    private StateMachineListener listener = new StateMachineListener() {};

    /**
     * Sets the state-machine lifecycle listener.
     *
     * @param listener the listener to use
     * @return this builder
     */
    public Builder listener(StateMachineListener listener) {
      this.listener = listener;
      return this;
    }

    /**
     * Builds and returns the configured {@link C2SimStateMachine}.
     *
     * @return the new state machine starting in {@link State#UNINITIALIZED}
     */
    public C2SimStateMachine build() {
      return new C2SimStateMachine(listener);
    }
  }

  private record Edge(State from, Trigger trigger, State to) {}
}
