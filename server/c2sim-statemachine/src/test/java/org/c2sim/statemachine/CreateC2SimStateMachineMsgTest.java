package org.c2sim.statemachine;

import static org.junit.jupiter.api.Assertions.*;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.c2sim.lox.helpers.XmlFactoryHelper;
import org.c2sim.lox.schema.C2SIMHeaderType;
import org.c2sim.lox.schema.MessageType;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM State machine library")
@Story("Validate creation of C2SIM messages creation based on enum")
class CreateC2SimStateMachineMsgTest {

  @Description("Create C2SIM Message RESET_SCENARIO based on enum")
  @Test
  void testCreateMessageForTrigger_ResetScenario() {
    C2SIMHeaderType header = XmlFactoryHelper.createC2SimHeader("DUMMY");
    MessageType msg = C2SimStateMachine.createMessageForTrigger(Trigger.RESET_SCENARIO, header);
    assertNotNull(msg);
    assertNotNull(msg.getMessageBody().getSystemMessageBody().getResetScenario());
  }

  @Description("Create C2SIM Message SUBMIT_INITIALIZATION based on enum")
  @Test
  void testCreateMessageForTrigger_SubmitInitialization() {
    C2SIMHeaderType header = XmlFactoryHelper.createC2SimHeader("DUMMY");
    MessageType msg =
        C2SimStateMachine.createMessageForTrigger(Trigger.SUBMIT_INITIALIZATION, header);
    assertNotNull(msg);
    assertNotNull(msg.getMessageBody().getSystemMessageBody().getSubmitInitialization());
  }

  /** For some triggers there is no C2SIM message generated. */
  @Description("Not all enums can be converted to C2SIM message")
  @Test
  void testCreateMessageForTrigger_NotAllowed() {
    C2SIMHeaderType header = XmlFactoryHelper.createC2SimHeader("DUMMY");
    assertNull(
        C2SimStateMachine.createMessageForTrigger(Trigger.C2SIM_INITIALIZATION_BODY, header));
    assertNull(C2SimStateMachine.createMessageForTrigger(Trigger.UNKNOWN, header));
    assertNull(C2SimStateMachine.createMessageForTrigger(Trigger.OBJECT_INITIALIZATION, header));
  }

  @Description("Create C2SIM Message START_SCENARIO based on enum")
  @Test
  void testCreateMessageForTrigger_StartScenario() {
    C2SIMHeaderType header = XmlFactoryHelper.createC2SimHeader("DUMMY");
    MessageType msg = C2SimStateMachine.createMessageForTrigger(Trigger.START_SCENARIO, header);
    assertNotNull(msg);
    assertNotNull(msg.getMessageBody().getSystemMessageBody().getStartScenario());
  }

  @Description("Create C2SIM Message STOP_SCENARIO based on enum")
  @Test
  void testCreateMessageForTrigger_StopScenario() {
    C2SIMHeaderType header = XmlFactoryHelper.createC2SimHeader("DUMMY");
    MessageType msg = C2SimStateMachine.createMessageForTrigger(Trigger.STOP_SCENARIO, header);
    assertNotNull(msg);
    assertNotNull(msg.getMessageBody().getSystemMessageBody().getStopScenario());
  }

  @Description("Create C2SIM Message SHARE_SCENARIO based on enum")
  @Test
  void testCreateMessageForTrigger_ShareScenario() {
    C2SIMHeaderType header = XmlFactoryHelper.createC2SimHeader("DUMMY");
    MessageType msg = C2SimStateMachine.createMessageForTrigger(Trigger.SHARE_SCENARIO, header);
    assertNotNull(msg);
    assertNotNull(msg.getMessageBody().getSystemMessageBody().getShareScenario());
  }

  @Description("Create C2SIM Message PAUSE_SCENARIO based on enum")
  @Test
  void testCreateMessageForTrigger_PauseScenario() {
    C2SIMHeaderType header = XmlFactoryHelper.createC2SimHeader("DUMMY");
    MessageType msg = C2SimStateMachine.createMessageForTrigger(Trigger.PAUSE_SCENARIO, header);
    assertNotNull(msg);
    assertNotNull(msg.getMessageBody().getSystemMessageBody().getPauseScenario());
  }

  @Description("Create C2SIM Message RESUME_SCENARIO based on enum")
  @Test
  void testCreateMessageForTrigger_ResumeScenario() {
    C2SIMHeaderType header = XmlFactoryHelper.createC2SimHeader("DUMMY");
    MessageType msg = C2SimStateMachine.createMessageForTrigger(Trigger.RESUME_SCENARIO, header);
    assertNotNull(msg);
    assertNotNull(msg.getMessageBody().getSystemMessageBody().getResumeScenario());
  }

  @Description("Create C2SIM Message INITIALIZATION_COMPLETE based on enum")
  @Test
  void testCreateMessageForTrigger_InitializationComplete() {
    C2SIMHeaderType header = XmlFactoryHelper.createC2SimHeader("DUMMY");
    MessageType msg =
        C2SimStateMachine.createMessageForTrigger(Trigger.INITIALIZATION_COMPLETE, header);
    assertNotNull(msg);
    assertNotNull(msg.getMessageBody().getSystemMessageBody().getInitializationComplete());
  }

  @Description("C2SIM header is mandatory")
  @Test
  void testCreateMessageForTrigger_DefaultThrows() {
    C2SIMHeaderType header = XmlFactoryHelper.createC2SimHeader("DUMMY");

    assertThrows(
        NullPointerException.class, () -> C2SimStateMachine.createMessageForTrigger(null, header));
  }
}
