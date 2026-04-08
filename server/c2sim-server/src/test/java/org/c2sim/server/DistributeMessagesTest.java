package org.c2sim.server;

import io.javalin.websocket.WsCloseStatus;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.c2sim.server.api.models.RequestJoinSession;
import org.c2sim.server.streaming.StreamingClient;
import org.c2sim.server.utils.StringHelper;
import org.c2sim.statemachine.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Server module")
@Story("Distribute C2SIM messages")
class DistributeMessagesTest extends BaseTest {

  private final CountDownLatch numberOfReceivedMessagesA = new CountDownLatch(1);
  private final CountDownLatch numberOfReceivedMessagesB = new CountDownLatch(6);

  @Test
  void distributeC2SimMessages() throws InterruptedException {
    var sharedSession = getSession();
    logger.info("JUNIT: Bring C2SIM server in executing state and publish order.");
    sharedSession.joinSharedSession(
        CLIENT_ID_A,
        "joining A",
        new RequestJoinSession(CLIENT_ID_A_SYSTEM_NAME, CLIENT_ID_A_SYSTEM_NAME));
    sharedSession.joinSharedSession(
        CLIENT_ID_B,
        "joining B",
        new RequestJoinSession(CLIENT_ID_B_SYSTEM_NAME, CLIENT_ID_B_SYSTEM_NAME));

    // Get the shared session client
    var clientA = sharedSession.getOrCreateClientById(CLIENT_ID_A);
    var clientB = sharedSession.getOrCreateClientById(CLIENT_ID_B);

    clientA.assignStreamingClient(
        new StreamingClient() {
          @Override
          public void onC2SimMessageReceived(Consumer<String> listener) {
            /* store listener */
          }

          @Override
          public void onClosed(Consumer<Void> listener) {
            /* not used in JUNIT test */
          }

          @Override
          public void onError(Consumer<String> listener) {
            /* not used in JUNIT test */
          }

          @Override
          public void closeConnection(WsCloseStatus status, String message) {
            /* not used in JUNIT test */
          }

          @Override
          public void sendC2SimMessage(String xml) {
            logger.info("JUNIT: Simulate sending XML message to client A");
            numberOfReceivedMessagesA.countDown();
          }
        });

    clientB.assignStreamingClient(
        new StreamingClient() {
          @Override
          public void onC2SimMessageReceived(Consumer<String> listener) {
            /* store listener */
          }

          @Override
          public void onClosed(Consumer<Void> listener) {
            /* not needed in this test */
          }

          @Override
          public void onError(Consumer<String> listener) {
            /* not needed in this test */
          }

          @Override
          public void closeConnection(WsCloseStatus status, String message) {
            /* not needed in this test */
          }

          @Override
          public void sendC2SimMessage(String xml) {
            logger.info("JUNIT: Simulate sending XML message to client B");
            numberOfReceivedMessagesB.countDown();
          }
        });

    Assertions.assertTrue(clientA.hasJoinedSharedSession());
    Assertions.assertTrue(clientB.hasJoinedSharedSession());

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

    logger.info("Client A+B send both an order");
    sharedSession.publishC2SimDoc(
        CLIENT_ID_A, "A:Order", StringHelper.toStream(createManeuverWarfareTask()));
    sharedSession.publishC2SimDoc(
        CLIENT_ID_B, "B:Order", StringHelper.toStream(createManeuverWarfareTask()));

    Assertions.assertTrue(
        numberOfReceivedMessagesA.await(2, TimeUnit.SECONDS), "Should have received: 'B:Order'");
    Assertions.assertTrue(
        numberOfReceivedMessagesB.await(2, TimeUnit.SECONDS),
        "Should have received: 'A:SubmitInitialization', 'A:c2SIMInitialization', etc..");
  }
}
