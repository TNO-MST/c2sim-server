package org.c2sim.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.*;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.c2sim.server.api.models.RequestJoinSession;
import org.c2sim.server.exceptions.C2SimException;
import org.c2sim.server.services.*;
import org.c2sim.server.services.impl.*;
import org.c2sim.server.utils.C2SimObjectMapper;
import org.c2sim.server.utils.StringHelper;
import org.c2sim.statemachine.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Server module")
@Story("State machine test")
class C2SimServiceStateMachineTest extends BaseTest {

  @Test
  void stateTransitionTest() {
    var sharedSession = getSession();
    // =====================================================================================================
    logger.info(
        "JUNIT: Check if shared session " + SHARED_SESSION_NAME + " state is UNINITIALIZED.");
    Assertions.assertEquals(
        State.UNINITIALIZED,
        sharedSession.getCurrentState(),
        "The shared session state must be UNINITIALIZED");
    // =====================================================================================================
    logger.info("JUNIT: C2SIM client must join shared session " + SHARED_SESSION_NAME);
    // REST JOIN
    sharedSession.joinSharedSession(
        CLIENT_ID_A, "joining", new RequestJoinSession("NLD_LOX", "SYSTEM"));
    // ====================================================================================================
    logger.info(
        "JUNIT: Publish C2SIM XML 'submitInitialization' (simulate REST call):\n{}",
        submitInitializationXml);
    c2simService.publishC2SimDoc(
        SHARED_SESSION_NAME,
        CLIENT_ID_A,
        "submitInitialization",
        StringHelper.toStream(submitInitializationXml));
    Assertions.assertEquals(
        State.INITIALIZING,
        sharedSession.getCurrentState(),
        "The shared session state must be INITIALIZING, state is "
            + sharedSession.getCurrentState());
    // ====================================================================================================
    logger.info("JUNIT: Check if startScenario is rejected in state INITIALIZING (not allowed).");
    C2SimException startScenarioNotAllowed =
        Assertions.assertThrowsExactly(
            C2SimException.class,
            () ->
                sharedSession.publishC2SimDoc(
                    CLIENT_ID_A,
                    "startScenario_not_allowed",
                    StringHelper.toStream(startScenarioXml)));
    Assertions.assertEquals(
        C2SimException.ErrorCode.STATE_TRANSITION_NOT_ALLOWED.toString(),
        startScenarioNotAllowed.getError().getCode());
    logger.info(
        "JUNIT: Expected ERROR[{}]: '{}'",
        startScenarioNotAllowed.getError().getCode(),
        startScenarioNotAllowed.getMessage());
    // ====================================================================================================
    logger.info("JUNIT: Check if shareScenario is rejected if there is no C2SIM initialization.");
    C2SimException shareScenarioNoInit =
        Assertions.assertThrowsExactly(
            C2SimException.class,
            () ->
                sharedSession.publishC2SimDoc(
                    CLIENT_ID_A,
                    "startScenario_not_allowed",
                    StringHelper.toStream(shareScenarioXml)));
    Assertions.assertEquals(
        C2SimException.ErrorCode.INITIALIZATION_NOT_COMPLETED.toString(),
        shareScenarioNoInit.getError().getCode());
    logger.info(
        "JUNIT: Expected ERROR[{}]: '{}'",
        startScenarioNotAllowed.getError().getCode(),
        startScenarioNotAllowed.getMessage());

    // ====================================================================================================
    logger.info("JUNIT: Publish C2SIM XML 'C2SIMInitialization' (simulate REST call)");
    c2simService.publishC2SimDoc(
        SHARED_SESSION_NAME,
        CLIENT_ID_A,
        "c2SIMInitialization",
        StringHelper.toStream(c2SIMInitializationXml));
    Assertions.assertEquals(
        State.INITIALIZING,
        sharedSession.getCurrentState(),
        "The shared session state must be INITIALIZING, state is "
            + sharedSession.getCurrentState());
    // ====================================================================================================
    logger.info("JUNIT: Publish C2SIM XML 'initializationComplete' (simulate REST call)");
    c2simService.publishC2SimDoc(
        SHARED_SESSION_NAME,
        CLIENT_ID_A,
        "initializationComplete",
        StringHelper.toStream(initializationCompleteXml));

    // ====================================================================================================
    logger.info(
        "JUNIT: Reject request C2SIM initialization when not in executing state (late join)");
    C2SimException requestC2SIM =
        Assertions.assertThrowsExactly(
            C2SimException.class, sharedSession::getC2SIMInitializationAsTextXml);
    Assertions.assertEquals(
        C2SimException.ErrorCode.NO_C2SIM_INITIALIZATION_BODY.toString(),
        requestC2SIM.getError().getCode());
    logger.info(
        "JUNIT: Expected ERROR[{}]: '{}'",
        requestC2SIM.getError().getCode(),
        requestC2SIM.getMessage());
    // ====================================================================================================
    logger.info(
        "JUNIT: Publish C2SIM XML 'shareScenario' (simulate REST call), move to state INITIALIZED");
    c2simService.publishC2SimDoc(
        SHARED_SESSION_NAME,
        CLIENT_ID_A,
        "shareScenarioOke",
        StringHelper.toStream(shareScenarioXml));
    Assertions.assertEquals(
        State.INITIALIZED,
        sharedSession.getCurrentState(),
        "The shared session state must be INITIALIZED, state is "
            + sharedSession.getCurrentState());
    // ====================================================================================================
    logger.info("JUNIT: Request C2SIM initialization, should be allowed.");
    var xml = sharedSession.getC2SIMInitializationAsTextXml();
    Assertions.assertNotNull(xml);
    Assertions.assertFalse(xml.isEmpty());
    logger.info("C2SIM initialization:\n{}", xml);
  }

  @Test
  void publishOrderInNonExecutingState() {
    var sharedSession = getSession();

    // =====================================================================================================
    logger.info("JUNIT: Check C2SIM client can not publish when not joined.");
    // Publish C2SIM order
    C2SimException ex =
        Assertions.assertThrowsExactly(
            C2SimException.class,
            () ->
                sharedSession.publishC2SimDoc(
                    CLIENT_ID_A, "", StringHelper.toStream(createManeuverWarfareTask())));
    Assertions.assertEquals(
        C2SimException.ErrorCode.CLIENT_NOT_JOINED_SHARED_SESSION.toString(),
        ex.getError().getCode());
    // =====================================================================================================
    logger.info("JUNIT: Check C2SIM client joins shared session " + SHARED_SESSION_NAME);
    // Simulate REST join request
    sharedSession.joinSharedSession(CLIENT_ID_A, "", new RequestJoinSession("NLD_LOX", "SYSTEM"));
    // =====================================================================================================
    logger.info(
        "JUNIT: Check C2SIM client can not publish C2SIM order when not in executing state.");
    // Publish C2SIM order
    C2SimException notIncorrectState =
        Assertions.assertThrowsExactly(
            C2SimException.class,
            () ->
                sharedSession.publishC2SimDoc(
                    CLIENT_ID_A,
                    "publish_not_allowed_in_state",
                    StringHelper.toStream(createManeuverWarfareTask())));
    Assertions.assertEquals(
        C2SimException.ErrorCode.C2SIM_MSG_NOT_ALLOWED_IN_STATE.toString(),
        notIncorrectState.getError().getCode());
    logger.info("JUNIT: Expected C2SIM Error msg '{}'", notIncorrectState.getMessage());

    // =====================================================================================================
    logger.info("JUNIT: Bring C2SIM server in executing state.");

    sharedSession.publishC2SimDoc(
        CLIENT_ID_A, "A:SubmitInitialization", StringHelper.toStream(submitInitializationXml));
    sharedSession.publishC2SimDoc(
        CLIENT_ID_A, "A:c2SIMInitialization", StringHelper.toStream(c2SIMInitializationXml));
    sharedSession.publishC2SimDoc(
        CLIENT_ID_A, "A:InitializationComplete", StringHelper.toStream(initializationCompleteXml));
    sharedSession.publishC2SimDoc(
        CLIENT_ID_A, "A:ShareScenario", StringHelper.toStream(shareScenarioXml));
    sharedSession.publishC2SimDoc(
        CLIENT_ID_A, "A:StartScenario", StringHelper.toStream(startScenarioXml));
    Assertions.assertEquals(
        State.RUNNING,
        sharedSession.getCurrentState(),
        "The shared session state must be RUNNING, state is " + sharedSession.getCurrentState());
    // =====================================================================================================
    logger.info("JUNIT: Check if publishing an order is allowed.");
    sharedSession.publishC2SimDoc(
        CLIENT_ID_A, "publish_allowed", StringHelper.toStream(createManeuverWarfareTask()));

    //
    sharedSession.publishC2SimDoc(
        CLIENT_ID_A, "A:StopScenario", StringHelper.toStream(stopScenarioXml));
    Assertions.assertEquals(State.INITIALIZED, sharedSession.getCurrentState());

    sharedSession.publishC2SimDoc(CLIENT_ID_A, "A:Reset", StringHelper.toStream(resetScenarioXml));
    Assertions.assertEquals(State.UNINITIALIZED, sharedSession.getCurrentState());

    // =====================================================================================================
    logger.info("JUNIT: Bring C2SIM server in executing state (after reset).");

    sharedSession.publishC2SimDoc(
        CLIENT_ID_A, "A:SubmitInitialization", StringHelper.toStream(submitInitializationXml));
    sharedSession.publishC2SimDoc(
        CLIENT_ID_A, "A:c2SIMInitialization", StringHelper.toStream(c2SIMInitializationXml));
    sharedSession.publishC2SimDoc(
        CLIENT_ID_A, "A:InitializationComplete", StringHelper.toStream(initializationCompleteXml));
    sharedSession.publishC2SimDoc(
        CLIENT_ID_A, "A:ShareScenario", StringHelper.toStream(shareScenarioXml));
    sharedSession.publishC2SimDoc(
        CLIENT_ID_A, "A:StartScenario", StringHelper.toStream(startScenarioXml));
    Assertions.assertEquals(
        State.RUNNING,
        sharedSession.getCurrentState(),
        "The shared session state must be RUNNING, state is " + sharedSession.getCurrentState());
  }

  public static class JUnitModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(EnvService.class).to(DefaultEnvService.class).in(Scopes.SINGLETON);
      bind(ConfigService.class).to(DefaultConfigService.class).in(Scopes.SINGLETON);
      bind(C2SimSchemaService.class).to(DefaultC2SimSchemaService.class).in(Scopes.SINGLETON);
      bind(C2SimService.class).to(DefaultC2SimService.class).in(Scopes.SINGLETON);
      bind(WebService.class).to(DefaultWebService.class).in(Scopes.SINGLETON);
      bind(WebSocketService.class).to(DefaultWebSocketService.class).in(Scopes.SINGLETON);
      bind(MetricService.class).to(DefaultMetricService.class).in(Scopes.SINGLETON);
    }

    @Provides
    ObjectMapper provideObjectMapper() {
      // Configure the ObjectMapper if needed (e.g., register modules, set properties)
      return C2SimObjectMapper.mapper;
    }
  }
}
