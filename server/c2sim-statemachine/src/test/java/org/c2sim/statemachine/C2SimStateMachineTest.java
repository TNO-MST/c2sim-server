package org.c2sim.statemachine;

import com.github.oxo42.stateless4j.delegates.FuncBoolean;
import com.github.oxo42.stateless4j.transitions.Transition;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.c2sim.statemachine.C2SimStateMachine.StateMachineListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Epic("C2SIM Server")
@Feature("C2SIM State machine library")
@Story("Test the state machine callback mechanism ")
class C2SimStateMachineTest {
  private static final Logger logger = LoggerFactory.getLogger(C2SimStateMachineTest.class);

  private static void logTransition(Transition<State, Trigger> transition) {
    logger.info(
        "Trigger {} activated state change from {} to {}",
        transition.getTrigger(),
        transition.getSource(),
        transition.getDestination());
  }

  @Description("Move trough all States and check if callback event are invoked.")
  @Test
  void stateTransition() throws InterruptedException {
    var semaphoreEnterUninitialized = new CountDownLatch(1);
    var semaphoreEnterInitializing = new CountDownLatch(1);
    var semaphoreEnterInitialized = new CountDownLatch(1);
    var semaphoreEnterRunning = new CountDownLatch(1);
    var semaphoreEnterPause = new CountDownLatch(1);
    var allSystemInitialized = new CountDownLatch(2);

    var machine =
        new C2SimStateMachine.Builder()
            .listener(
                new StateMachineListener() {
                  @Override
                  public void onEnterStateUninitialized(Transition<State, Trigger> transition) {
                    logTransition(transition);
                    semaphoreEnterUninitialized.countDown();
                  }

                  @Override
                  public void onEnterStateInitializing(Transition<State, Trigger> transition) {
                    logTransition(transition);
                    semaphoreEnterInitializing.countDown();
                  }

                  @Override
                  public void onEnterStateInitialized(Transition<State, Trigger> transition) {

                    logTransition(transition);
                    semaphoreEnterInitialized.countDown();
                  }

                  @Override
                  public void onEnterStateRunning(Transition<State, Trigger> transition) {
                    logTransition(transition);
                    semaphoreEnterRunning.countDown();
                  }

                  @Override
                  public void onEnterStatePaused(Transition<State, Trigger> transition) {
                    logTransition(transition);
                    semaphoreEnterPause.countDown();
                  }

                  @Override
                  public FuncBoolean allSystemsAreInitialized() {
                    return () -> {
                      // Simulate that transition from INITIALIZING -> INITIALIZED is only allowed
                      // when all systems acknowledged
                      try {
                        logger.info(
                            "Waiting on {} systems to initialize", allSystemInitialized.getCount());
                        return allSystemInitialized.await(1, TimeUnit.SECONDS);
                      } catch (InterruptedException e) {
                        return false;
                      }
                    };
                  }
                })
            .build();

    // Initial state: UNINITIALIZED
    Assertions.assertEquals(State.UNINITIALIZED, machine.getCurrentState());

    // State UNINITIALIZED => INITIALIZING (Trigger RECEIVED_SUBMIT_INITIALIZATION)
    logger.info("Trigger RECEIVED_SUBMIT_INITIALIZATION");
    machine.fireTrigger(Trigger.SUBMIT_INITIALIZATION);
    Assertions.assertTrue(
        semaphoreEnterInitializing.await(1, TimeUnit.SECONDS),
        "Trigger RECEIVED_SUBMIT_INITIALIZATION should result in state Initializing");
    Assertions.assertEquals(State.INITIALIZING, machine.getCurrentState());

    // Only change to initialized when all systems are ready
    Assertions.assertFalse(
        machine.isTriggerAllowed(Trigger.SHARE_SCENARIO),
        "Not all systems initialized trigger RECEIVED_SHARE_SCENARIO not allowed.");
    allSystemInitialized.countDown();
    Assertions.assertFalse(
        machine.isTriggerAllowed(Trigger.SHARE_SCENARIO),
        "Not all systems initialized trigger RECEIVED_SHARE_SCENARIO not allowed.");
    allSystemInitialized.countDown(); // fake all systems are ready
    Assertions.assertTrue(
        machine.isTriggerAllowed(Trigger.SHARE_SCENARIO),
        "All systems initialized trigger RECEIVED_SHARE_SCENARIO allowed.");

    // State INITIALIZING => INITIALIZED (Trigger RECEIVED_SHARE_SCENARIO)
    logger.info("Trigger RECEIVED_SHARE_SCENARIO");
    machine.fireTrigger(Trigger.SHARE_SCENARIO);
    Assertions.assertTrue(
        semaphoreEnterInitialized.await(3, TimeUnit.SECONDS),
        "Trigger RECEIVED_SHARE_SCENARIO should result in state INITIALIZED");
    Assertions.assertEquals(State.INITIALIZED, machine.getCurrentState());

    // State INITIALIZED => RUNNING (Trigger RECEIVED_START_SCENARIO)
    machine.fireTrigger(Trigger.START_SCENARIO);
    Assertions.assertTrue(
        semaphoreEnterRunning.await(3, TimeUnit.SECONDS),
        "Trigger RECEIVED_START_SCENARIO should result in state RUNNING");
    Assertions.assertEquals(State.RUNNING, machine.getCurrentState());

    // State RUNNING => PAUSED (Trigger RECEIVED_PAUSE_SCENARIO)
    machine.fireTrigger(Trigger.PAUSE_SCENARIO);
    Assertions.assertTrue(
        semaphoreEnterRunning.await(3, TimeUnit.SECONDS),
        "Trigger RECEIVED_PAUSE_SCENARIO should result in state PAUSED");
    Assertions.assertEquals(State.PAUSED, machine.getCurrentState());

    // State PAUSED -> RUNNING =>  (Trigger RECEIVED_PLAY_SCENARIO)
    machine.fireTrigger(Trigger.RESUME_SCENARIO);
    Assertions.assertEquals(State.RUNNING, machine.getCurrentState());
  }
}
