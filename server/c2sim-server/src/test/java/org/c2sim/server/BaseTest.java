package org.c2sim.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Objects;
import java.util.UUID;
import org.c2sim.lox.Global;
import org.c2sim.lox.helpers.MessageTypeHelper;
import org.c2sim.lox.helpers.XmlFactoryHelper;
import org.c2sim.lox.helpers.builders.GeodeticCoordinateTypeBuilder;
import org.c2sim.lox.schema.C2SIMHeaderType;
import org.c2sim.server.services.C2SimSchemaService;
import org.c2sim.server.services.C2SimService;
import org.c2sim.server.services.ConfigService;
import org.c2sim.server.services.MetricService;
import org.c2sim.server.sessions.SharedSession;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTest {

  protected static final Logger logger = LoggerFactory.getLogger("JUNIT");

  protected static final String SHARED_SESSION_NAME = "federates";
  protected static final String CLIENT_ID_A = "A";
  protected static final String CLIENT_ID_A_SYSTEM_NAME = "SYSTEM_A";
  protected static final String CLIENT_ID_B = "B";
  protected static final String CLIENT_ID_B_SYSTEM_NAME = "SYSTEM_B";

  protected Injector injector;
  protected C2SimService c2simService;
  protected ConfigService configService;
  protected C2SimSchemaService c2simSchemaService;
  protected MetricService metricService;

  //
  protected String submitInitializationXml;
  protected String shareScenarioXml;
  protected String startScenarioXml;
  protected String stopScenarioXml;
  protected String resetScenarioXml;
  protected String initializationCompleteXml;
  protected String c2SIMInitializationXml;

  protected BaseTest() {
    submitInitializationXml = XmlFactoryHelper.createSubmitInitialization(createHeader(), true);
    shareScenarioXml = XmlFactoryHelper.createShareScenario(createHeader(), true);
    startScenarioXml = XmlFactoryHelper.createStartScenario(createHeader(), true);
    stopScenarioXml = XmlFactoryHelper.createStopScenario(createHeader(), true);
    resetScenarioXml = XmlFactoryHelper.createReset(createHeader(), true);
    initializationCompleteXml = XmlFactoryHelper.createInitializationComplete(createHeader(), true);
    c2SIMInitializationXml =
        XmlFactoryHelper.createC2SIMInitialization(createHeader(), CLIENT_ID_A_SYSTEM_NAME, true);
  }

  protected static C2SIMHeaderType createHeader() {
    return XmlFactoryHelper.createC2SimHeader("JUNIT_TEST");
  }

  protected static String createManeuverWarfareTask() {
    var msg =
        XmlFactoryHelper.createManeuverWarfareTaskMsg(
            createHeader(),
            UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
            GeodeticCoordinateTypeBuilder.create(
                    12, 34, 0, GeodeticCoordinateTypeBuilder.EAltitude.NONE)
                .build());
    return MessageTypeHelper.writeMessage(msg, false, true, "");
  }

  @BeforeEach
  void setup() {
    injector = Guice.createInjector(new C2SimServiceStateMachineTest.JUnitModule());
    c2simService = Objects.requireNonNull(injector.getInstance(C2SimService.class));
    configService = Objects.requireNonNull(injector.getInstance(ConfigService.class));
    c2simSchemaService = Objects.requireNonNull(injector.getInstance(C2SimSchemaService.class));
    metricService = Objects.requireNonNull(injector.getInstance(MetricService.class));

    logger.info("JUNIT: Create a shared shared session " + SHARED_SESSION_NAME);
    // Create shared session
    c2simService.addSharedSession(
        new SharedSession(
            metricService,
            configService,
            c2simSchemaService,
            SHARED_SESSION_NAME,
            Global.C2SIM_SCHEMA_VERSION,
            SHARED_SESSION_NAME,
            "Session used for unit testing",
            true));
  }

  public SharedSession getSession() {
    logger.info("JUNIT: Check if shared session " + SHARED_SESSION_NAME + " exist.");
    return c2simService.getSharedSession(SHARED_SESSION_NAME, true);
  }
}
