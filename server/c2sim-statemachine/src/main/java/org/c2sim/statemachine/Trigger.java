package org.c2sim.statemachine;

/**
 * Triggers (events) that drive the C2SIM simulation lifecycle state machine.
 *
 * <p>Each trigger corresponds to a C2SIM system command or message that causes a state transition
 * in {@link C2SimStateMachine}. The mapping between triggers and C2SIM XML message kinds is
 * maintained in {@link C2SimStateMachine}.
 */
public enum Trigger {
  /** Sent by a federate to submit its initialization data to the server. */
  SUBMIT_INITIALIZATION,

  /** Carries the C2SIM initialization body (entity and task definitions). */
  C2SIM_INITIALIZATION_BODY,

  /** Signals object-level initialization . */
  OBJECT_INITIALIZATION,

  /** Signals that all systems have completed initialization; */
  SHARE_SCENARIO,

  /** Resets the scenario back to {@link State#UNINITIALIZED}. */
  RESET_SCENARIO,

  /** Starts the scenario; */
  START_SCENARIO,

  /** Pauses a running scenario; transitions from {@link State#RUNNING} to {@link State#PAUSED}. */
  PAUSE_SCENARIO,

  /**
   * Stops the scenario; transitions from {@link State#RUNNING} back to {@link State#INITIALIZED}.
   */
  STOP_SCENARIO,

  /** Resumes a paused scenario; transitions from {@link State#PAUSED} to {@link State#RUNNING}. */
  RESUME_SCENARIO,

  /** Sent by a federate to confirm that its initialization phase is complete. */
  INITIALIZATION_COMPLETE,

  /** Fallback value used when a trigger cannot be determined. */
  UNKNOWN
}
