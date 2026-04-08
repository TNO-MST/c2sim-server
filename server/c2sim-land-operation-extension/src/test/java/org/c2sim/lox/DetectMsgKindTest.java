package org.c2sim.lox;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.exceptions.ValidationException;
import org.c2sim.lox.helpers.MessageTypeHelper;
import org.c2sim.lox.helpers.XmlFactoryHelper;
import org.c2sim.lox.sax.DetectMsgKind;
import org.c2sim.lox.schema.C2SIMHeaderType;
import org.c2sim.lox.schema.MessageType;
import org.c2sim.lox.validation.LoxXsdValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Epic("C2SIM Server")
@Feature("C2SIM LOX module")
@Story("C2SIM message kind detection")
class DetectMsgKindTest {
  private static final Logger logger = LoggerFactory.getLogger(DetectMsgKindTest.class);

  @Test()
  @Description("Detect C2SIMInitializationBody in XML test")
  void determineC2SIMInitializationBody() throws IOException {

    String resourcePath = "lox/C2SIMInitialization_small.xml";
    try (InputStream inputStream =
        DetectMsgKindTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
      Assertions.assertEquals(
          C2SimMsgKind.C2SIM_INITIALIZATION, DetectMsgKind.determineMsgKind(inputStream));
    }
  }

  private static C2SIMHeaderType createHeader() {
    return XmlFactoryHelper.createC2SimHeader("DONT_CARE");
  }

  private static C2SimMsgKind determineKind(MessageType msg)
      throws LoxException, ValidationException {
    var xml = MessageTypeHelper.writeMessageAsString(msg, true, true);
    Assertions.assertTrue(LoxXsdValidator.doValidation(xml).isValid());
    var result = DetectMsgKind.determineMsgKindMeasured(new ByteArrayInputStream(xml.getBytes()));
    logger.debug("Searched XML in {} ms, detected kind {}", result.timeMs(), result.result());
    return result.result();
  }

  @Test()
  @Description("Detect START_SCENARIO in XML C2SIM message")
  void determineStartScenario() throws LoxException, ValidationException {
    Assertions.assertEquals(
        C2SimMsgKind.START_SCENARIO,
        determineKind(XmlFactoryHelper.createStartScenario(createHeader())));
  }

  @Test
  @Description("Detect STOP_SCENARIO in XML C2SIM message")
  void determineStopScenario() throws LoxException, ValidationException {
    Assertions.assertEquals(
        C2SimMsgKind.STOP_SCENARIO,
        determineKind(XmlFactoryHelper.createStopScenario(createHeader())));
  }

  @Test
  @Description("Detect PAUSE_SCENARIO in XML C2SIM message")
  void determinePauseScenario() throws LoxException, ValidationException {
    Assertions.assertEquals(
        C2SimMsgKind.PAUSE_SCENARIO,
        determineKind(XmlFactoryHelper.createPauseScenario(createHeader())));
  }

  @Test
  @Description("Detect SHARE_SCENARIO in XML C2SIM message")
  void determineShareScenario() throws LoxException, ValidationException {
    Assertions.assertEquals(
        C2SimMsgKind.SHARE_SCENARIO,
        determineKind(XmlFactoryHelper.createShareScenario(createHeader())));
  }

  @Test
  @Description("Detect RESUME_SCENARIO in XML C2SIM message")
  void determineResumeScenario() throws LoxException, ValidationException {
    Assertions.assertEquals(
        C2SimMsgKind.RESUME_SCENARIO,
        determineKind(XmlFactoryHelper.createResumeScenario(createHeader())));
  }

  @Test
  @Description("Detect RESET_SCENARIO in XML C2SIM message")
  void determineResetScenario() throws LoxException, ValidationException {
    Assertions.assertEquals(
        C2SimMsgKind.RESET_SCENARIO, determineKind(XmlFactoryHelper.createReset(createHeader())));
  }

  @Test
  @Description("Detect REALTIME_MULTIPLE in XML C2SIM message")
  void determineSimulationRealtimeMultiple() throws LoxException, ValidationException {
    Assertions.assertEquals(
        C2SimMsgKind.SET_SIMULATION_REALTIME_MULTIPLE,
        determineKind(XmlFactoryHelper.createSimulationRealtimeMultiple(createHeader(), 1)));
  }

  @Test
  @Description("Detect SUBMIT_INITIALIZATION in XML C2SIM message")
  void determineSubmitInitialization() throws LoxException, ValidationException {
    Assertions.assertEquals(
        C2SimMsgKind.SUBMIT_INITIALIZATION,
        determineKind(XmlFactoryHelper.createSubmitInitialization(createHeader())));
  }

  /*
  CheckpointRestore("/Message/MessageBody/SystemMessageBody/CheckpointRestore"),
  */
}
