package org.c2sim.server.streaming;

import io.javalin.websocket.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.c2sim.server.services.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link StreamingClient} implementation backed by a Javalin WebSocket connection.
 *
 * <p>Created when a C2SIM client opens a WebSocket connection at {@code
 * /api/c2sim/session/{sessionName}/ws}. Automatic pings are enabled on construction to keep the
 * connection alive through proxies.
 *
 * <p>Incoming messages from the client are logged and the connection is closed, since the C2SIM
 * protocol does not allow clients to send data over the streaming channel.
 */
public class WsSharedSessionClient implements StreamingClient {

  private static final Logger logger = LoggerFactory.getLogger(WsSharedSessionClient.class);
  private final String clientId;
  private final String sharedSessionName;
  private final WsConnectContext ctx;

  private final AtomicReference<Consumer<String>> onC2SimMessageReceived = new AtomicReference<>();
  private final AtomicReference<Consumer<Void>> onClosed = new AtomicReference<>();

  /**
   * Creates the WebSocket streaming client.
   *
   * @param owner the {@link WebSocketService} that owns the WebSocket endpoint
   * @param clientId the C2SIM client identifier
   * @param sharedSessionName the name of the shared session this connection belongs to
   * @param ctx the Javalin WebSocket connect context for this connection
   */
  public WsSharedSessionClient(
      WebSocketService owner, String clientId, String sharedSessionName, WsConnectContext ctx) {
    this.sharedSessionName = sharedSessionName;
    this.clientId = clientId;
    this.ctx = ctx;
    ctx.enableAutomaticPings();
  }

  /**
   * Called by the WebSocket handler when the client sends a message.
   *
   * <p>Logs the unexpected message; the caller is responsible for closing the connection
   * afterwards.
   *
   * @param ignoredCtx the Javalin WebSocket message context (content is not used)
   */
  public void handleOnMessage(WsMessageContext ignoredCtx) {
    logger.debug(
        "Session '{}': C2SIM streaming client '{}' send data, not allowed",
        sharedSessionName,
        clientId);
  }

  /**
   * Called by the WebSocket handler when the client closes the connection.
   *
   * @param ctx the Javalin WebSocket close context
   */
  public void handleOnClose(WsCloseContext ctx) {
    logger.debug(
        "Session '{}': C2SIM streaming client '{}' closed connection (STATUS {}: '{}')",
        sharedSessionName,
        clientId,
        ctx.closeStatus(),
        ctx.reason());
    emitClosed();
  }

  /**
   * Called by the WebSocket handler when a transport error occurs.
   *
   * @param ctx the Javalin WebSocket error context
   */
  public void handleOnError(WsErrorContext ctx) {
    var error = ctx.error();
    logger.warn(
        "Error WebSocket client {}: {}",
        ctx.sessionId(),
        error != null ? error.getMessage() : "Error in websocket");
  }

  /** {@inheritDoc} */
  @Override
  public void sendC2SimMessage(String msg) {

    ctx.send(msg);
  }

  /** {@inheritDoc} */
  @Override
  public void onC2SimMessageReceived(Consumer<String> listener) {
    onC2SimMessageReceived.set(listener);
  }

  /**
   * Fires the {@link #onC2SimMessageReceived} listener with the given XML string.
   *
   * @param xml the XML message received from the client
   */
  public void emitC2SimMessageReceived(String xml) {
    Consumer<String> listener = onC2SimMessageReceived.get();
    if (listener != null) {
      listener.accept(xml);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void onClosed(Consumer<Void> listener) {
    onClosed.set(listener);
  }

  /**
   * Fires the {@link #onClosed} listener to notify observers that the connection has been closed.
   */
  public void emitClosed() {
    Consumer<Void> listener = onClosed.get();
    if (listener != null) {
      listener.accept(null);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void onError(Consumer<String> listener) {
    /* Not needed; ignore */
  }

  /** {@inheritDoc} */
  @Override
  public void closeConnection(WsCloseStatus status, String message) {
    ctx.closeSession(status, message);
  }
}
