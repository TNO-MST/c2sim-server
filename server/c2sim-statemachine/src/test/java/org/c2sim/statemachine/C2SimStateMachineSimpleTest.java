package org.c2sim.statemachine;

import static org.c2sim.lox.C2SimMsgKind.*;
import static org.junit.jupiter.api.Assertions.*;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.Set;
import java.util.stream.Collectors;
import org.c2sim.lox.C2SimMsgKind;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM State machine library")
@Story("Validate C2SIM message only allowed in correct state")
class C2SimStateMachineSimpleTest {

  private C2SimStateMachine machine;

  @BeforeEach
  void setup() {
    machine = new C2SimStateMachine.Builder().build();
  }

  /** The initial state is UNINITIALIZED */
  @Description("State machine must initialize in UNINITIALIZED state")
  @Test
  void startsInUninitialized() {
    assertEquals(State.UNINITIALIZED, machine.getCurrentState());
  }

  @Description("State machine UNINITIALIZED => INITIALIZING")
  @Test
  void submitInitializationMovesToInitializing() {
    assertEquals(State.UNINITIALIZED, machine.getCurrentState());
    machine.fireTrigger(Trigger.SUBMIT_INITIALIZATION);
    assertEquals(State.INITIALIZING, machine.getCurrentState());
  }

  /** Test state INITIALIZED */
  @Description("State machine UNINITIALIZED => INITIALIZING => INITIALIZED")
  @Test
  void shareScenarioMovesToInitializedWhenGuardTrue() {
    machine.fireTrigger(Trigger.SUBMIT_INITIALIZATION);
    machine.fireTrigger(Trigger.SHARE_SCENARIO);
    assertEquals(State.INITIALIZED, machine.getCurrentState());
    checkOnlyThisC2SimMessagesAreAllowed(Set.of(RESET_SCENARIO, START_SCENARIO));
  }

  /** Test state RUNNING */
  @Description("Check allowed C2SIM messages in RUNNING state")
  @Test
  void startScenarioMovesToRunning() {
    // Move to state RUNNING
    machine.fireTrigger(Trigger.SUBMIT_INITIALIZATION);
    machine.fireTrigger(Trigger.SHARE_SCENARIO);
    machine.fireTrigger(Trigger.START_SCENARIO);
    assertEquals(State.RUNNING, machine.getCurrentState());
    checkOnlyThisC2SimMessagesAreAllowed(
        Set.of(
            STOP_SCENARIO,
            PAUSE_SCENARIO,
            REPORT,
            ORDER,
            MAGIC_MOVE,
            CHECKPOINT_RESTORE,
            SET_SIMULATION_REALTIME_MULTIPLE));
  }

  /** Test state PAUSED */
  @Description("Check allowed C2SIM messages in PAUSED state")
  @Test
  void pauseScenarioMovesToPaused() {
    machine.fireTrigger(Trigger.SUBMIT_INITIALIZATION);
    machine.fireTrigger(Trigger.C2SIM_INITIALIZATION_BODY);
    machine.fireTrigger(Trigger.INITIALIZATION_COMPLETE);
    machine.fireTrigger(Trigger.SHARE_SCENARIO);
    machine.fireTrigger(Trigger.START_SCENARIO);
    machine.fireTrigger(Trigger.PAUSE_SCENARIO);
    assertEquals(State.PAUSED, machine.getCurrentState());

    // TODO Is Pause state now allowing REPORTS/ORDERS/MAGIC MOVE etc. or should it be reject?
    checkOnlyThisC2SimMessagesAreAllowed(
        Set.of(
            RESUME_SCENARIO,
            STOP_SCENARIO,
            SET_SIMULATION_REALTIME_MULTIPLE,
            REPORT,
            ORDER,
            MAGIC_MOVE,
            CHECKPOINT_RESTORE));
  }

  /** Test State change PAUSE (pause) -> RUNNING (resume) -> PAUSE (pause) */
  @Description("State machine PAUSE => RUNNING => PAUSE")
  @Test
  void resumeScenarioMovesBackToRunning() {
    machine.fireTrigger(Trigger.SUBMIT_INITIALIZATION);
    machine.fireTrigger(Trigger.C2SIM_INITIALIZATION_BODY);
    machine.fireTrigger(Trigger.INITIALIZATION_COMPLETE);
    machine.fireTrigger(Trigger.SHARE_SCENARIO);
    machine.fireTrigger(Trigger.START_SCENARIO);
    machine.fireTrigger(Trigger.PAUSE_SCENARIO);
    assertEquals(State.PAUSED, machine.getCurrentState());
    machine.fireTrigger(Trigger.RESUME_SCENARIO);
    assertEquals(State.RUNNING, machine.getCurrentState());
    machine.fireTrigger(Trigger.PAUSE_SCENARIO);
    assertEquals(State.PAUSED, machine.getCurrentState());
  }

  /** Change state RUNNING -> INITIALIZED */
  @Description("State machine RUNNING -> INITIALIZED ")
  @Test
  void stopScenarioReturnsToInitialized() {
    machine.fireTrigger(Trigger.SUBMIT_INITIALIZATION);
    machine.fireTrigger(Trigger.C2SIM_INITIALIZATION_BODY);
    machine.fireTrigger(Trigger.INITIALIZATION_COMPLETE);
    machine.fireTrigger(Trigger.SHARE_SCENARIO);
    machine.fireTrigger(Trigger.START_SCENARIO);
    assertEquals(State.RUNNING, machine.getCurrentState());
    machine.fireTrigger(Trigger.STOP_SCENARIO);
    assertEquals(State.INITIALIZED, machine.getCurrentState());
  }

  /** State change INITIALIZED (reset) -> UNINITIALIZED */
  @Test
  void resetScenarioReturnsToUninitialized() {
    machine.fireTrigger(Trigger.SUBMIT_INITIALIZATION);
    machine.fireTrigger(Trigger.SHARE_SCENARIO);
    assertEquals(State.INITIALIZED, machine.getCurrentState());
    machine.fireTrigger(Trigger.RESET_SCENARIO);
    assertEquals(State.UNINITIALIZED, machine.getCurrentState());
  }

  @Description("Throw exception when trigger used in invalid state")
  @Test
  void invalidTriggerThrowsException() {
    // UNINITIALIZED
    assertEquals(State.UNINITIALIZED, machine.getCurrentState());
    checkTriggerAllowedInState(Set.of(Trigger.SUBMIT_INITIALIZATION));

    // INITIALIZING
    machine.fireTrigger(Trigger.SUBMIT_INITIALIZATION);
    assertEquals(State.INITIALIZING, machine.getCurrentState());
    checkTriggerAllowedInState(
        Set.of(
            Trigger.SHARE_SCENARIO,
            Trigger.RESET_SCENARIO,
            Trigger.C2SIM_INITIALIZATION_BODY,
            Trigger.OBJECT_INITIALIZATION,
            Trigger.INITIALIZATION_COMPLETE));

    // INITIALIZED
    machine.fireTrigger(Trigger.SHARE_SCENARIO);
    assertEquals(State.INITIALIZED, machine.getCurrentState());
    checkTriggerAllowedInState(Set.of(Trigger.RESET_SCENARIO, Trigger.START_SCENARIO));

    // RUNNING
    machine.fireTrigger(Trigger.START_SCENARIO);
    assertEquals(State.RUNNING, machine.getCurrentState());
    checkTriggerAllowedInState(Set.of(Trigger.PAUSE_SCENARIO, Trigger.STOP_SCENARIO));

    // PAUSED
    machine.fireTrigger(Trigger.PAUSE_SCENARIO);
    assertEquals(State.PAUSED, machine.getCurrentState());
    checkTriggerAllowedInState(Set.of(Trigger.RESUME_SCENARIO, Trigger.STOP_SCENARIO));
  }

  /** Test INITIALIZING state */
  @Description("Check C2SIM messages in state INITIALIZING")
  @Test
  void testInitializingState() {
    machine.fireTrigger(Trigger.SUBMIT_INITIALIZATION);
    Assertions.assertEquals(State.INITIALIZING, machine.getCurrentState());
    checkOnlyThisC2SimMessagesAreAllowed(
        Set.of(
            C2SIM_INITIALIZATION,
            OBJECT_INITIALIZATION,
            INITIALIZATION_COMPLETE,
            RESET_SCENARIO,
            SHARE_SCENARIO));
  }

  @Description("Check C2SIM messages in state RUNNING")
  @Test
  void nonStateMachineMessagesAllowedOnlyInRunningOrPaused() {
    machine.fireTrigger(Trigger.SUBMIT_INITIALIZATION);
    machine.fireTrigger(Trigger.SHARE_SCENARIO);
    machine.fireTrigger(Trigger.START_SCENARIO);
    Assertions.assertEquals(State.RUNNING, machine.getCurrentState());
    assertTrue(machine.isC2SimMsgAllowedInCurrentState(REPORT));
    assertTrue(machine.isC2SimMsgAllowedInCurrentState(ORDER));
  }

  @Description("Check C2SIM messages in state UNINITIALIZED")
  @Test
  void nonStateMachineMessagesNotAllowedInUninitialized() {
    Assertions.assertEquals(State.UNINITIALIZED, machine.getCurrentState());
    assertFalse(machine.isC2SimMsgAllowedInCurrentState(REPORT));
  }

  @Description("Mermaid notation generation")
  @Test
  void testSetStateAndToMermaid() {

    machine.setState(State.RUNNING);
    assertEquals(State.RUNNING, machine.getCurrentState());
    String mermaid = machine.toMermaid();
    assertNotNull(mermaid);
    assertTrue(mermaid.contains("stateDiagram-v2"));
  }

  private void checkOnlyThisC2SimMessagesAreAllowed(Set<C2SimMsgKind> allowed) {
    for (var msgType : C2SimMsgKind.values()) {
      if ((msgType == MESSAGE_BODY_NOT_WRAPPED) || (msgType == ERROR) || (msgType == UNKNOWN)) {
        // TODO When C2SIM message kind detection failed are now handled as raw, and allowed
        continue; // ignore
      }

      /* TODO Allow unknown C2SIM message kinds or reject? */
      if (allowed.contains(msgType)) {
        assertTrue(
            machine.isC2SimMsgAllowedInCurrentState(msgType),
            String.format(
                "In state '%s' the C2SIM message '%s' should be allowed.",
                machine.getCurrentState(), msgType));
      } else {
        assertFalse(
            machine.isC2SimMsgAllowedInCurrentState(msgType),
            String.format(
                "In state '%s' the C2SIM message '%s' should NOT be allowed.",
                machine.getCurrentState(), msgType));
      }
    }
  }

  private void checkTriggerAllowedInState(Set<Trigger> triggers) {
    var expectedPermittedTriggers =
        machine.getPermittedTriggers().stream()
            .map(Enum::name)
            .sorted()
            .collect(Collectors.joining(";"));

    var unittestPermittedTriggers =
        machine.getPermittedTriggers().stream()
            .map(Enum::name)
            .sorted()
            .collect(Collectors.joining(";"));

    assertEquals(
        machine.getPermittedTriggers(),
        triggers,
        String.format(
            "Expected triggers in unit test, don't match; expected '%s', unit test '%s'",
            expectedPermittedTriggers, unittestPermittedTriggers));

    for (var trigger : Trigger.values()) {
      // Skip all triggers that are allowed
      if (!triggers.contains(trigger)) {
        assertThrows(
            IllegalStateException.class,
            () -> {
              machine.fireTrigger(trigger);
            },
            String.format(
                "The trigger '%s' should not be allowed in state '%s'.",
                trigger, machine.getCurrentState()));
      }
    }
  }
}
