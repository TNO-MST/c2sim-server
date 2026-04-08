package org.c2sim.lox.helpers;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.exceptions.ValidationException;
import org.c2sim.lox.helpers.builders.GeodeticCoordinateTypeBuilder;
import org.c2sim.lox.helpers.builders.PositionReportContentTypeBuilder;
import org.c2sim.lox.schema.OperationalStatusCodeType;
import org.c2sim.lox.schema.TaskStatusCodeType;
import org.c2sim.lox.validation.LoxXsdValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Epic("C2SIM Server")
@Feature("C2SIM LOX module")
@Story("C2SIM message build with factory")
class FactoryHelperTest {
  private static final Logger logger = LoggerFactory.getLogger(FactoryHelperTest.class);
  private static final String SYSTEM_NAME = "SYSTEM_A";
  private static final SecureRandom random = new SecureRandom();

  private static PositionReportContentTypeBuilder createPositionReport() {

    return PositionReportContentTypeBuilder.create()
        .timeOfObservation(Instant.now())
        .operationalStatus(OperationalStatusCodeType.FULLY_OPERATIONAL)
        .strength(random.nextInt(101))
        .headingAngle((double) random.nextInt(361))
        .location(
            GeodeticCoordinateTypeBuilder.create(
                (double) random.nextInt((90 * 2) + 1) - 90,
                (double) random.nextInt((180 * 2) + 1) - 180,
                0.0f,
                GeodeticCoordinateTypeBuilder.EAltitude.ABOVE_GROUND_LEVEL))
        .speed((double) random.nextInt(101))
        .subjectEntity(UUID.randomUUID());
  }

  @Test()
  @Description("Create C2SIM message Maneuver Warfare Order")
  void validateManeuverWarfareOrder() throws LoxException, ValidationException {
    logger.info("Create ManeuverWarfare order (move task) from code and validate it against XSD");
    var msg =
        XmlFactoryHelper.createManeuverWarfareTaskMsg(
            XmlFactoryHelper.createC2SimHeader("TEST"),
            UUID.fromString("3f92a4b1-2c5d-4a08-bf61-8a79b62c9384"),
            GeodeticCoordinateTypeBuilder.create(
                    34, 56, 0, GeodeticCoordinateTypeBuilder.EAltitude.NONE)
                .build());
    var xml = MessageTypeHelper.writeMessageAsString(msg, true, true);
    Assertions.assertTrue(LoxXsdValidator.doValidation(xml).isValid(), "Invalid XML: \n" + xml);
  }

  @Test()
  @Description("Create C2SIM message START_SCENARIO")
  void createStartScenario() throws LoxException, ValidationException {
    logger.info("Create Start Scenario System CMD from code and validate it against XSD");
    var msg = XmlFactoryHelper.createStartScenario(XmlFactoryHelper.createC2SimHeader(SYSTEM_NAME));
    var xml = MessageTypeHelper.writeMessageAsString(msg, true, true);
    Assertions.assertTrue(LoxXsdValidator.doValidation(xml).isValid(), "Invalid XML: \n" + xml);
  }

  @Test()
  void createSimulationRealtimeMultiple() throws LoxException, ValidationException {
    logger.info("Create SimulationRealtimeMultiple CMD from code and validate it against XSD");
    var msg =
        XmlFactoryHelper.createSimulationRealtimeMultiple(
            XmlFactoryHelper.createC2SimHeader(SYSTEM_NAME), 1);
    var xml = MessageTypeHelper.writeMessageAsString(msg, true, true);
    Assertions.assertTrue(LoxXsdValidator.doValidation(xml).isValid(), "Invalid XML: \n" + xml);
  }

  @Test()
  @Description("Create C2SIM message C2SIMInitialization")
  void createC2SIMInitialization() throws ValidationException, LoxException {
    logger.info("Create C2SIMInitialization from code and validate it against XSD");
    var init =
        XmlFactoryHelper.createC2SIMInitialization(
            XmlFactoryHelper.createC2SimHeader(SYSTEM_NAME), SYSTEM_NAME);
    var xml = MessageTypeHelper.writeMessageAsString(init, true, true);
    var validator = LoxXsdValidator.doValidation(xml);
    Assertions.assertTrue(validator.isValid(), "Invalid XML: \n" + xml);
  }

  @Test()
  @Description("Create C2SIM message MAGIC_MOVE")
  void createMagicMove() throws LoxException, ValidationException {
    logger.info("Create Magic Move");
    var magicMove =
        XmlFactoryHelper.createMagicMove(
            XmlFactoryHelper.createC2SimHeader(SYSTEM_NAME),
            UUID.randomUUID(),
            10,
            20,
            30,
            GeodeticCoordinateTypeBuilder.EAltitude.NONE);

    var xml = MessageTypeHelper.writeMessageAsString(magicMove, true, true);
    var validator = LoxXsdValidator.doValidation(xml);
    Assertions.assertTrue(validator.isValid(), "Invalid XML: \n" + xml);
  }

  @Test()
  void createPositionReports() throws LoxException, ValidationException {
    logger.info("Create Position Reports from code and validate it against XSD");
    List<PositionReportContentTypeBuilder> list = new ArrayList<>();
    list.add(createPositionReport());
    var report =
        XmlFactoryHelper.createPositionReport(
            XmlFactoryHelper.createC2SimHeader(SYSTEM_NAME),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            list);

    var xml = MessageTypeHelper.writeMessageAsString(report, true, true);
    var validator = LoxXsdValidator.doValidation(xml);
    Assertions.assertTrue(validator.isValid(), "Invalid XML: \n" + xml);
  }

  @Test()
  void createTaskStatusReport() throws LoxException, ValidationException {
    logger.info("Create Task Status Reports from code and validate it against XSD");
    var report =
        XmlFactoryHelper.createTaskStatusReport(
            XmlFactoryHelper.createC2SimHeader(SYSTEM_NAME),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            TaskStatusCodeType.TASKSTRT);
    var xml = MessageTypeHelper.writeMessageAsString(report, true, true);
    var validator = LoxXsdValidator.doValidation(xml);
    Assertions.assertTrue(validator.isValid(), "Invalid XML: \n" + xml);
  }
}
