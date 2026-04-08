package org.c2sim.client;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.ByteString;
import org.c2sim.client.exceptions.C2ClientException;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.helpers.ResourceHelper;
import org.c2sim.client.invoker.ApiException;
import org.c2sim.client.model.*;
import org.c2sim.lox.C2SimMsgKind;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.exceptions.ValidationException;
import org.c2sim.lox.helpers.MessageBodyTypeHelper;
import org.c2sim.lox.helpers.MessageTypeHelper;
import org.c2sim.lox.helpers.XmlFactoryHelper;
import org.c2sim.lox.helpers.builders.*;
import org.c2sim.lox.schema.C2SIMInitializationBodyType;
import org.c2sim.lox.schema.MessageBodyType;
import org.c2sim.lox.schema.MessageType;
import org.c2sim.lox.schema.TaskStatusCodeType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Epic("C2SIM Server")
@Feature("C2SIM Client library")
@Story("Test C2Client streaming")
class C2SimClientStreamingTest {
  private static final Logger logger = LoggerFactory.getLogger(C2SimClientStreamingTest.class);
  private MockWebServer server;
  private final Map<String, WebSocket> webSockets = new ConcurrentHashMap<>();
  private C2SimClient.C2SimClientListener clientNotifications;
  private C2SimClient clientA;
  private C2SimClient clientB;

  private static MessageType createTaskStatusMsg() {
    var header = XmlFactoryHelper.createC2SimHeader("systemName");
    return MessageTypeBuilder.create()
        .c2SIMHeader(header)
        .messageBody(
            MessageBodyTypeBuilder.create()
                .domainMessageBody(
                    DomainMessageBodyTypeBuilder.create()
                        .reportBody(
                            ReportBodyTypeBuilder.create()
                                .reportID(UUID.randomUUID())
                                .fromSender(UUID.randomUUID())
                                .toReceiver(UUID.randomUUID())
                                .reportingEntity(UUID.randomUUID())
                                .addTaskStatusReport(
                                    TaskStatusTypeBuilder.create()
                                        .currentTask(UUID.randomUUID())
                                        .timeOfObservation(Instant.now())
                                        .taskStatusCode(TaskStatusCodeType.TASKCMPLT)))))
        .build();
  }

  private static C2SimClient.C2SimClientListener createListener() {
    return new C2SimClient.C2SimClientListener() {
      @Override
      public void onJoined(C2SimClient client, DynamicSessionInfo info) {
        logger.info(
            "C2SIM client '{}': notification joined shared session '{}'",
            client.getClientIdDisplayName(),
            info.getSessionName());
        C2SimClient.C2SimClientListener.super.onJoined(client, info);
      }

      @Override
      public void onResigned(C2SimClient client) {
        logger.info(
            "C2SIM Client '{}': notification resigned from shared session '{}",
            client.getClientIdDisplayName(),
            client.getSharedSessionName());
        C2SimClient.C2SimClientListener.super.onResigned(client);
      }

      @Override
      public void onC2SIMInitialization(C2SimClient client, C2SIMInitializationBodyType init) {
        logger.info(
            "C2SIM Client '{}': notification received initialization",
            client.getClientIdDisplayName());
        C2SimClient.C2SimClientListener.super.onC2SIMInitialization(client, init);
      }

      @Override
      public void onStreamConnected(C2SimClient client) {
        logger.info(
            "C2SIM Client '{}': notification stream connected for shared session '{}'.",
            client.getClientIdDisplayName(),
            client.getSharedSessionName());
        C2SimClient.C2SimClientListener.super.onStreamConnected(client);
      }

      @Override
      public void onStreamDisconnected(C2SimClient client, int code, String reason) {
        logger.info(
            "C2SIM Client '{}': notification stream disconnected", client.getClientIdDisplayName());
        C2SimClient.C2SimClientListener.super.onStreamDisconnected(client, code, reason);
      }

      @Override
      public void onStreamFault(C2SimClient client, String reason) {
        logger.info(
            "C2SIM Client '{}': notification stream fault: {}",
            client.getClientIdDisplayName(),
            reason);
        C2SimClient.C2SimClientListener.super.onStreamFault(client, reason);
      }

      @Override
      public void onStateChanged(C2SimClient client, StateType oldState, StateType newState) {
        logger.info(
            "C2SIM Client '{}': notification state changed from '{}' to '{}'",
            client.getClientIdDisplayName(),
            oldState,
            newState);
        C2SimClient.C2SimClientListener.super.onStateChanged(client, oldState, newState);
      }
    };
  }

  /* Simulates an C2SIM server REST interface (in RUNNING state) */
  private Dispatcher createC2SIMServerStub() {
    return new Dispatcher() {
      @NotNull
      @Override
      public MockResponse dispatch(@NotNull RecordedRequest request) {
        // Simulate a C2SIM server in running state
        var serverState = StateType.RUNNING;
        HttpUrl url = request.getRequestUrl();
        String ep = url.encodedPath();
        Assertions.assertNotNull(ep);

        var clientId = request.getHeader("clientId");
        // WebSocket doesn't use clientId in header

        switch (ep) {
          case "//c2sim/session/list":
            return new MockResponse()
                .setResponseCode(200)
                .setBody(
                    CreateJson.toJson(CreateJson.createSessionsDummyData(serverState, "default")));
          case "//c2sim/session/default/stream-endpoints":
            return createStreamingEndPointResponse(server, clientId, "default");
          case "//c2sim/session/TEST123/stream-endpoints":
            return createStreamingEndPointResponse(server, clientId, "TEST123");
          case "//c2sim/session/default/join":
            return createJoinResponse(serverState, "default");
          case "//c2sim/session/TEST123/join":
            return createJoinResponse(serverState, "TEST123");
          case "//c2sim/session/default/resign", "//c2sim/session/TEST123/resign":
            return new MockResponse().setResponseCode(200);
          case "//c2sim/session/default/info":
            return new MockResponse()
                .setResponseCode(200)
                .setBody(CreateJson.createDynamicSessionInfo(serverState, "default").toJson());
          case "//c2sim/session/TEST123/info":
            return new MockResponse()
                .setResponseCode(200)
                .setBody(CreateJson.createDynamicSessionInfo(serverState, "TEST123").toJson());
          case "//c2sim/session/default/send":
            return createSendResponse(request, clientId, webSockets);
          case "/ws/default", "/ws/TEST123":
            return createWebsocket(
                request.getRequestUrl()); // Client is setting up websocket connection
          case "//c2sim/session/default/initialization", "//c2sim/session/TEST123/initialization":
            return createInitializationResponse();
          case "//c2sim/session/TEST123/create":
            logger.info("C2SIM Server stub: Create shared session TEST123");
            ResponseCreateSession response = new ResponseCreateSession();
            response.creation(CreateUpdateType.CREATED);
            response.setSession(CreateJson.createDynamicSessionInfo(serverState, "TEST123"));
            return new MockResponse().setBody(response.toJson()).setResponseCode(200);
          case null:
            break;
          default:
            return new MockResponse().setBody(ep + " not implemented in stub").setResponseCode(404);
        }
        return new MockResponse().setResponseCode(404);
      }
    };
  }

  private static MockResponse createInitializationResponse() {
    try {
      // Read C2SIM initialization body from resource
      var xml =
          ResourceHelper.readResourceAsBytes(
              C2SimClientStreamingTest.class, "/lox/C2SIMInitialization_small.xml");
      MessageBodyType initBody =
          MessageBodyTypeHelper.readMessageBody(new ByteArrayInputStream(xml));
      var header = XmlFactoryHelper.createC2SimHeader("systemName");
      var msg = MessageTypeBuilder.create().c2SIMHeader(header).messageBody(initBody).build();

      return new MockResponse()
          .setHeader("Content-Type", "application/xml")
          .setResponseCode(200)
          .setBody(MessageTypeHelper.writeMessageAsString(msg, true, true));
    } catch (IOException | LoxException e) {
      return new MockResponse()
          .setResponseCode(500)
          .setBody("Failed to read C2SIMInitializationBody");
    }
  }

  private MockResponse createJoinResponse(StateType serverState, String sharedSessionName) {
    // Return the C2SIM server is in running state
    ResponseJoinSession session = new ResponseJoinSession();
    session.session(CreateJson.createDynamicSessionInfo(serverState, sharedSessionName));
    return new MockResponse().setResponseCode(200).setBody(session.toJson());
  }

  private static MockResponse createSendResponse(
      RecordedRequest request, String clientId, Map<String, WebSocket> webSockets) {
    // Only for testing
    // C2SIM server removes enter in xml (one line)
    // Doesn't send it to the sender (this test does)

    // Decode multi part body
    String body = request.getBody().readUtf8();

    // Find start of file content
    int start = body.indexOf("\r\n\r\n") + 4;

    // Find end before boundary
    int end = body.lastIndexOf("\r\n--");

    String fileContent = body.substring(start, end);

    for (var entry : webSockets.entrySet()) {
      if (!entry.getKey().equalsIgnoreCase(clientId)) {
        Assertions.assertTrue(
            entry.getValue().send(fileContent + "\n"),
            "C2SIM Server stub: Send to websocket failed");
      }
    }
    return new MockResponse().setResponseCode(200);
  }

  private MockResponse createWebsocket(HttpUrl requestUrl) {
    URI uri = URI.create(String.valueOf(requestUrl));
    // var sharedSessionName = uri.getPath().substring("/ws/".length()); // "/users"
    String clientId = uri.getQuery().split("=")[1];
    return new MockResponse()
        .withWebSocketUpgrade(
            new WebSocketListener() {
              @Override
              public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                logger.debug(
                    "C2SIM Server stub: C2SIM client send data over WebSocket, not allowed");
                webSocket.close(1011, "C2SIM client send data over WebSocket (not allowed)");
              }

              @Override
              public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                super.onMessage(webSocket, text);
                webSocket.close(1011, "C2SIM client send data over WebSocket (not allowed)");
              }

              @Override
              public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosed(webSocket, code, reason);
              }

              @Override
              public void onOpen(WebSocket webSocket, Response response) {
                webSockets.put(clientId, webSocket);
              }

              @Override
              public void onClosing(WebSocket webSocket, int code, String reason) {
                Optional<String> clientId =
                    webSockets.entrySet().stream()
                        .filter(entry -> entry.getValue() == webSocket)
                        .map(Map.Entry::getKey)
                        .findFirst();
                if (clientId.isPresent()) {
                  logger.info(
                      "C2SIM Server stub: WebSocket for client id '{}' closing ({}: '{}')",
                      clientId.get(),
                      code,
                      reason);
                  webSockets.values().remove(webSocket);
                  webSocket.close(code, reason); // Send ACK
                } else logger.error("C2SIM Server stub: Unknown websocket (should not happen)");
              }

              @Override
              public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                logger.error("C2SIM server stub socket error: {}", t.getMessage());
              }
            });
  }

  private static MockResponse createStreamingEndPointResponse(
      MockWebServer server, String clientId, String sharedSessionName) {
    var responseStreaming = new ResponseStreamEndpoints();
    var websocket = new ResponseStreamEndpointsWebsocket();
    // Convert REST url to WS url
    var baseUrl = server.url("/ws").toString().replace("http", "ws");

    var epUrl = String.format("%s/%s?clientId=%s", baseUrl, sharedSessionName, clientId);
    websocket.setUrl(epUrl);
    responseStreaming.setWebsocket(websocket);

    return new MockResponse().setResponseCode(200).setBody(responseStreaming.toJson());
  }

  @BeforeEach
  void setup() throws Exception {
    server = new MockWebServer();
    server.setDispatcher(createC2SIMServerStub());
    server.start();
    clientNotifications = createListener();

    clientA =
        C2SimClient.create()
            .url(server.url("/").toString())
            .clientIdDisplayName("SYSTEM-A-CLNT")
            .systemName("SYSTEM_A")
            .listener(clientNotifications)
            .build();

    clientB =
        C2SimClient.create()
            .url(server.url("/").toString())
            .systemName("SYSTEM_B")
            .clientIdDisplayName("SYSTEM-B-CLNT")
            .enableReceivedMessageValidation()
            .enableReceivedMessageDecode()
            .build();
  }

  private void connectToC2SimServer() throws C2SimRestException, C2ClientException, ApiException {
    logger.info("C2SIM client A&B joining shared session 'default'.");
    clientA.connect();
    clientB.connect();
    Assertions.assertTrue(clientA.isJoined());
    Assertions.assertTrue(clientA.hasStreamToSharedSession());
    Assertions.assertTrue(clientB.isJoined());
    Assertions.assertTrue(clientB.hasStreamToSharedSession());
  }

  private void disconnectFromC2SimServer() throws C2SimRestException, ApiException {
    logger.info("C2SIM client A&B resign");
    clientA.resignAndDisconnect();
    clientB.resignAndDisconnect();
    Assertions.assertFalse(clientA.isJoined());
    Assertions.assertFalse(clientA.hasStreamToSharedSession());
    Assertions.assertFalse(clientB.isJoined());
    Assertions.assertFalse(clientB.hasStreamToSharedSession());
  }

  @AfterEach
  void teardown() throws Exception {
    server.shutdown();
  }

  @Test
  @Description("Receive message validation (XSD validation error)")
  void receiveMessageValidationXsdValidationTest()
      throws C2SimRestException,
          C2ClientException,
          ApiException,
          ValidationException,
          InterruptedException {
    var received = new CountDownLatch(1);
    clientB.onReceivedMessage(
        msg -> {
          Assertions.assertEquals(C2SimMsgKind.UNKNOWN, msg.kind());
          Assertions.assertNotNull(msg.validation());
          Assertions.assertFalse(msg.validation().isValid());
          Assertions.assertFalse(msg.validation().getValidationsErrors().isEmpty());
          received.countDown();
        });
    connectToC2SimServer();
    clientA.publishC2SimDocument("<error></error>", false);
    Assertions.assertTrue(
        received.await(5, TimeUnit.SECONDS), "C2SIM client should have received message");
    disconnectFromC2SimServer();
  }

  @Test
  @Description("Receive message validation (invalid XML)")
  void receiveMessageValidationInvalidXmlTest()
      throws C2SimRestException,
          C2ClientException,
          ApiException,
          ValidationException,
          InterruptedException {
    var received = new CountDownLatch(1);
    clientB.onReceivedMessage(
        msg -> {
          Assertions.assertEquals(C2SimMsgKind.ERROR, msg.kind());
          Assertions.assertNotNull(msg.validationException());
          Assertions.assertNull(msg.validation());
          received.countDown();
        });
    connectToC2SimServer();
    clientA.publishC2SimDocument("<abc></def>", false);
    Assertions.assertTrue(
        received.await(5, TimeUnit.SECONDS), "C2SIM client should have received message");
    disconnectFromC2SimServer();
  }

  @Test
  @Description("Receive message validation (valid XML)")
  void receiveMessageValidationValidXmlTest()
      throws C2SimRestException,
          C2ClientException,
          ApiException,
          ValidationException,
          InterruptedException,
          IOException {
    var received = new CountDownLatch(1);
    clientB.onReceivedMessage(
        msg -> {
          Assertions.assertEquals(C2SimMsgKind.ORDER, msg.kind());
          Assertions.assertNull(msg.validationException());
          Assertions.assertNotNull(msg.validation());
          Assertions.assertTrue(
              msg.validation().isValid(), "There should not be an validation error");
          Assertions.assertNotNull(msg.decodedMsg(), "XML was not deserialized");
          received.countDown();
        });
    connectToC2SimServer();
    var xml =
        ResourceHelper.readResourceAsString(C2SimClientStreamingTest.class, "/Order.xml")
            .replace("\n", ""); // The C2SIM server removes the \n
    clientA.publishC2SimDocument(xml, true);
    Assertions.assertTrue(
        received.await(5, TimeUnit.SECONDS), "C2SIM client should have received message");
    disconnectFromC2SimServer();
  }

  @Test
  @Description("Test reconnect of C2SIM client")
  void reconnectTest()
      throws C2SimRestException, C2ClientException, ApiException, InterruptedException {
    var connected = new CountDownLatch(2);
    clientA.setC2simClientListener(
        new C2SimClient.C2SimClientListener() {
          @Override
          public void onStreamFault(C2SimClient client, String reason) {
            logger.info(
                "Test C2SIM client '{}({})': notified WebSocket failure '{}'.",
                client.getClientId(),
                client.getClientIdDisplayName(),
                reason);
            C2SimClient.C2SimClientListener.super.onStreamFault(client, reason);
          }

          @Override
          public void onStreamConnected(C2SimClient client) {

            logger.info(
                "Test C2SIM client '{}({})': notified WebSocket connected",
                client.getClientId(),
                client.getClientIdDisplayName());
            // Should connect 2 times, first normal, then reconnect
            connected.countDown();
          }
        });
    clientA.connect();
    Assertions.assertTrue(clientA.isJoined(), "C2SIM client should be joined");
    Assertions.assertTrue(
        clientA.hasStreamToSharedSession(), "C2SIM client should have streaming connection");
    logger.info("Simulate WebSocket connection lost, C2SIM client should automatically reconnect");
    for (WebSocket socket : new ArrayList<>(webSockets.values())) {
      if (socket == null) {
        continue;
      }
      try {

        socket.close(1011, "Simulate crash");
        // cancel option doesn't work in mocking
      } catch (Exception e) {
        logger.error("Error closing socket", e);
      }
    }

    Assertions.assertTrue(
        connected.await(5, TimeUnit.SECONDS), "C2SIM client should have reconnect");
    Assertions.assertTrue(clientA.hasStreamToSharedSession(), "C2SIM client is not reconnected");
    clientA.resignAndDisconnect();
  }

  @Test
  @Description("Join 'default' shared session and resign from shared session")
  void joinAndResignDefaultSharedSessionTest()
      throws C2SimRestException, C2ClientException, ApiException {
    connectToC2SimServer();
    disconnectFromC2SimServer();
  }

  @Test
  @Description("Join 'TEST123' shared session and resign from shared session")
  void joinAndResignNewSharedSessionTest()
      throws C2SimRestException, C2ClientException, ApiException, InterruptedException {

    clientA.setSharedSessionName("TEST123");
    clientB.setSharedSessionName("TEST123");

    // No provider defined
    C2ClientException ex =
        assertThrows(
            C2ClientException.class,
            () -> {
              clientA.connect();
            });
    Assertions.assertEquals(
        C2ClientException.ErrorCode.NO_SHARED_SESSION_PROVIDER, ex.getErrorCode());
    // Define provider
    clientA.whenCreatingSharedSession(
        new C2SimClient.SharedSessionInfoProvider() {
          @Override
          public RequestCreateSession provideSharedSessionInfo() {
            var result = new RequestCreateSession();
            var sessionInfo = new SessionInfo();
            sessionInfo.setDisplayName("TEST123");
            sessionInfo.setC2simSchemaVersion("1.0.2");
            sessionInfo.setDescription("TEST123");
            result.setData(sessionInfo);
            return result;
          }
        });
    clientA.connect();
    Assertions.assertTrue(clientA.isJoined());
    Assertions.assertTrue(clientA.hasStreamToSharedSession());
    Thread.sleep(10000);
    clientA.resignAndDisconnect();
  }

  @Test
  void testWebSocketStreaming() throws Exception {

    var received = new CountDownLatch(1);

    clientB.onReceivedMessage(
        msg -> {
          Assertions.assertEquals(C2SimMsgKind.REPORT, msg.kind());
          Assertions.assertNotNull(msg.xmlMessage());
          Assertions.assertNotNull(msg.decodedMsg());
          Assertions.assertNotNull(msg.decodedMsg().getMessageBody());
          Assertions.assertNotNull(msg.decodedMsg().getMessageBody().getDomainMessageBody());
          Assertions.assertNotNull(
              msg.decodedMsg().getMessageBody().getDomainMessageBody().getReportBody());
          received.countDown();
        });

    connectToC2SimServer();
    logger.info("C2SIM client A sends task status report");
    clientA.publishC2SimDocument(createTaskStatusMsg());

    Assertions.assertTrue(
        received.await(2, TimeUnit.SECONDS),
        "C2SIM client B should have received task status report");
    logger.info("C2SIM client B received task status report");
    disconnectFromC2SimServer();
  }
}
