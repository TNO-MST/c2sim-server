package org.c2sim.server.services.impl;

import com.google.inject.Inject;
import io.javalin.websocket.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.c2sim.server.services.C2SimService;
import org.c2sim.server.services.WebService;
import org.c2sim.server.services.WebSocketService;
import org.c2sim.server.sessions.SharedSession;
import org.c2sim.server.streaming.WsSharedSessionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link WebSocketService} implementation that manages C2SIM WebSocket connections.
 *
 * <p>Registers a Javalin WebSocket endpoint at {@code /api/c2sim/session/{sessionName}/ws}. Each
 * incoming connection is validated (session exists, {@code clientId} query parameter present) and
 * then associated with the corresponding {@link org.c2sim.server.sessions.SharedSession} via a
 * {@link WsSharedSessionClient}.
 *
 * <p>Incoming messages from clients are not supported — clients that send data have their
 * connection closed immediately with {@link WsCloseStatus#NORMAL_CLOSURE}.
 */
public class DefaultWebSocketService implements WebSocketService {

  /**
   * URL query-parameter name that clients must supply when opening a WebSocket connection. The
   * value identifies the C2SIM client within its shared session.
   */
  public static final String QUERY_PARAM_CLIENT_ID = "clientId";

  private static final Logger logger = LoggerFactory.getLogger(DefaultWebSocketService.class);

  // WebSocket sessions per shared Session
  final Map<String /* web socket session id */, WsSharedSessionClient> wsClients =
      new ConcurrentHashMap<>();
  private final WebService webService;
  private final C2SimService c2SimService;
  private Consumer<WsSharedSessionClient> onConnect = null;

  /**
   * Creates the service.
   *
   * @param webService the web service used to register the WebSocket endpoint
   * @param c2SimService the C2SIM service used to look up shared sessions
   */
  @Inject
  public DefaultWebSocketService(WebService webService, C2SimService c2SimService) {
    this.webService = webService;
    this.c2SimService = c2SimService;
  }

  /**
     Invoked when there is a new WebSocket incoming connection
   */

  public void onNewWebSocket(WsConfig webSocket) {
    webSocket.onConnect(this::handleConnect);
    webSocket.onMessage(this::handleMessage);
    webSocket.onClose(this::handleClose);
    webSocket.onError(this::handleError);
  }

  private boolean isValidSessionName(String sessionName, WsContext ctx) {
    if (sessionName == null || sessionName.isBlank()) {
      ctx.closeSession(WsCloseStatus.UNSUPPORTED_DATA, "Shared session is not specified in url.");
      return false;
    }
    return true;
  }

  private boolean isValidClientId(String clientId, WsContext ctx) {
    if (clientId == null || clientId.isBlank()) {
      ctx.closeSession(
          WsCloseStatus.UNSUPPORTED_DATA,
          "Client id (clientId) is not specified as query parameter in url.");
      return false;
    }
    return true;
  }

  private boolean isValidSharedSession(Object sharedSession, String sessionName, WsContext ctx) {
    if (sharedSession == null) {
      ctx.closeSession(
          WsCloseStatus.UNSUPPORTED_DATA,
          String.format("Shared session '%s' from WS url doesn't exist.", sessionName));
      return false;
    }
    return true;
  }

  private void handleConnect(WsConnectContext ctx) {
    String sessionName = ctx.pathParam("sessionName");
    if (!isValidSessionName(sessionName, ctx)) {
      return;
    }

    String clientId = ctx.queryParam(QUERY_PARAM_CLIENT_ID);
    if (!isValidClientId(clientId, ctx)) {
      return;
    }

    var sharedSession = c2SimService.getSharedSession(sessionName, false);
    if (!isValidSharedSession(sharedSession, sessionName, ctx)) {
      return;
    }

    registerClient(ctx, sessionName, clientId, sharedSession);
  }

  private void registerClient(
      WsConnectContext ctx, String sessionName, String clientId, SharedSession sharedSession) {

    var sharedSessionClient = sharedSession.getOrCreateClientById(clientId);

    logger.info(
        "Session '{}': Incoming WebSocket connection ID '{}' assign it to C2SIM client {}.",
        sessionName,
        ctx.sessionId(),
        sharedSessionClient.getClientNameForDebug());

    WsSharedSessionClient wsClient = new WsSharedSessionClient(this, clientId, sessionName, ctx);

    sharedSessionClient.assignStreamingClient(wsClient);
    wsClients.put(ctx.sessionId(), wsClient);

    if (onConnect != null) {
      onConnect.accept(wsClient);
    }
  }

  private void handleMessage(WsMessageContext ctx) {
    var client = wsClients.get(ctx.sessionId());

    if (client == null) {
      logger.error("WebSocket client {}: not assigned to shared sessions.", ctx.sessionId());
    } else {
      client.handleOnMessage(ctx);
    }

    ctx.closeSession(
        WsCloseStatus.NORMAL_CLOSURE,
        "C2SIM client is not allowed to send data to C2SIM server, connection closed.");
  }

  private void handleClose(WsCloseContext ctx) {
    var client = wsClients.remove(ctx.sessionId());

    if (client != null) {
      client.handleOnClose(ctx);
    } else {
      logger.debug(
          "WebSocket connection ID '{}', the connection was closed (no C2SIM client assigned).",
          ctx.sessionId());
    }
  }

  private void handleError(WsErrorContext ctx) {
    var client = wsClients.get(ctx.sessionId());
    if (client != null) {
      client.handleOnError(ctx);
    }
  }

  /**
   * Sets a callback invoked each time a new WebSocket client successfully connects and is
   * associated with a shared session.
   *
   * @param handler the callback; receives the newly created {@link WsSharedSessionClient}
   */
  public void setOnConnectHandler(Consumer<WsSharedSessionClient> handler) {
    onConnect = handler;
  }

  /**
   * Closes all active WebSocket connections with {@link WsCloseStatus#SERVICE_RESTART}.
   *
   * <p>Should be called during server shutdown to cleanly disconnect all clients.
   */
  @Override
  public void stop() {
    for (WsSharedSessionClient session : this.wsClients.values()) {
      session.closeConnection(WsCloseStatus.SERVICE_RESTART, "Server shutdown");
    }
  }
}
