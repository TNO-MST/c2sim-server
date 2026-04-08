package org.c2sim.server.sessions;

import io.javalin.websocket.WsCloseStatus;
import java.time.Duration;
import java.time.Instant;
import org.c2sim.server.streaming.StreamingClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks a single connected C2SIM client within a {@link SharedSession}.
 *
 * <p>One {@code SharedSessionClient} is created per unique {@code clientId} that interacts with a
 * session via REST calls. When the client also establishes a WebSocket streaming connection, the
 * corresponding {@link StreamingClient} is attached via {@link
 * #assignStreamingClient(StreamingClient)}.
 *
 * <p>A client must {@link #setHasJoinedSharedSession(boolean) join} the session before it is
 * allowed to publish C2SIM messages or receive document distributions.
 */
public class SharedSessionClient {

  private static final Logger logger = LoggerFactory.getLogger(SharedSessionClient.class);

  private final String sessionName;
  private final String clientId;
  private String systemName;
  private String clientIdDisplayName;
  private boolean joinCompleted = false;
  private StreamingClient webSocketClient = null;
  private Instant created = Instant.now();

  /**
   * Creates a new client tracker.
   *
   * @param sessionName the name of the {@link SharedSession} this client belongs to
   * @param clientId the unique client identifier
   */
  public SharedSessionClient(String sessionName, String clientId) {
    this.sessionName = sessionName;
    this.clientId = clientId;
    this.clientIdDisplayName = clientId;
    this.systemName = clientId;
  }

  /**
   * Attaches a WebSocket streaming connection to this client.
   *
   * <p>If a streaming connection is already active, the new connection is rejected with {@link
   * WsCloseStatus#ABNORMAL_CLOSURE}.
   *
   * @param sharedSessionWsClient the new streaming client to attach (may be {@code null} to detach)
   */
  public void assignStreamingClient(StreamingClient sharedSessionWsClient) {

    if (webSocketClient != null) {
      logger.warn(
          "There is already a websocket connection for client id {}, close this connection",
          clientId);
      sharedSessionWsClient.closeConnection(
          WsCloseStatus.ABNORMAL_CLOSURE,
          String.format("There is already a websocket connection for client id %s", clientId));

      sharedSessionWsClient = null;
    }
    webSocketClient = sharedSessionWsClient;
    if (webSocketClient != null) {
      logger.info(
          "Session '{}': {} is connected with a WebSocket ({}).",
          sessionName,
          getClientNameForDebug(),
          this.joinCompleted
              ? "C2SIM client already JOINED, start streaming"
              : "waiting for C2SIM client to JOIN before start streaming");
      // Subscribe to client streaming events
      webSocketClient.onC2SimMessageReceived(this::handleOnMessageFromStreamingClient);
      webSocketClient.onClosed(x -> handleOnClosedFromStreamingClient());
    }
  }

  // Placeholder; in current specification no messages will be received
  private void handleOnMessageFromStreamingClient(String xml) {
    /* place holder */
  }

  private void handleOnClosedFromStreamingClient() {
    logger.info(
        "Session '{}': Lost streaming connection to {} (WebSocket closed).",
        sessionName,
        getClientNameForDebug());
    webSocketClient = null;
  }

  /**
   * Sends a C2SIM XML document to this client over its WebSocket streaming connection.
   *
   * <p>The message is delivered only when:
   *
   * <ul>
   *   <li>a streaming connection is active ({@link #hasStreamToClient()})
   *   <li>the client has joined the session ({@link #hasJoinedSharedSession()})
   *   <li>the client is not the original publisher (clients do not receive their own messages)
   * </ul>
   *
   * @param publisher the publishing client, or {@code null} if the server itself is the publisher
   * @param c2simMessageXml the XML document to send
   * @return {@code true} if the message was sent; {@code false} otherwise
   */
  public boolean sendC2SimMessage(@Nullable SharedSessionClient publisher, String c2simMessageXml) {
    boolean isPublisher =
        ((publisher != null) && (publisher.getClientId().contentEquals(clientId)));
    if (hasStreamToClient() && hasJoinedSharedSession() && (publisher == null || !isPublisher)) {
      webSocketClient.sendC2SimMessage(c2simMessageXml);
      return true;
    }
    return false;
  }

  /**
   * Returns the unique client identifier.
   *
   * @return the client ID
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * Sets the C2SIM system name for this client.
   *
   * @param name the system name (must not be {@code null})
   */
  public void setSystemName(@NotNull String name) {
    systemName = name;
  }

  /**
   * Returns the C2SIM system name for this client.
   *
   * @return the system name
   */
  public String getSystemName() {
    return systemName;
  }

  /**
   * Sets the human-readable display name for the client ID.
   *
   * @param name the display name (must not be {@code null})
   */
  public void setClientIdDisplayName(@NotNull String name) {
    clientIdDisplayName = name;
  }

  /**
   * Returns the human-readable display name for the client ID.
   *
   * @return the client ID display name
   */
  public String getClientIdDisplayName() {
    return clientIdDisplayName;
  }

  /**
   * Marks whether this client has successfully joined the shared session.
   *
   * @param joined {@code true} if the client has joined
   */
  public void setHasJoinedSharedSession(boolean joined) {
    joinCompleted = joined;
  }

  /**
   * Returns whether this client has joined the shared session.
   *
   * @return {@code true} if the client has joined
   */
  public boolean hasJoinedSharedSession() {
    return joinCompleted;
  }

  /**
   * Returns whether this client has an active WebSocket streaming connection.
   *
   * @return {@code true} if a streaming connection is attached
   */
  public boolean hasStreamToClient() {
    return this.webSocketClient != null;
  }

  /**
   * Returns a short debug-friendly name combining the display name and raw client ID.
   *
   * @return a human-readable client identifier for log messages
   */
  public String getClientNameForDebug() {
    return String.format("C2SIM Client '%s(%s)'", clientIdDisplayName, clientId);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format(
        "%s [System %s]: %s | %s | lifetime %d min ",
        getClientNameForDebug(),
        systemName,
        hasJoinedSharedSession() ? "JOINED" : "NOT JOINED",
        hasStreamToClient() ? "STREAM CONNECTED" : "STREAM NOT CONNECTED",
        getCreationLifetimeInMinutes());
  }

  /**
   * Returns the number of minutes since this client was created.
   *
   * @return lifetime in minutes
   */
  public long getCreationLifetimeInMinutes() {
    return Duration.between(created, Instant.now()).toMinutes();
  }

  /**
   * Marks this client as resigned, logs the event, and closes any open streaming connection.
   *
   * @param reason the reason provided by the client
   */
  public void resign(@NotNull String reason) {
    logger.info(
        "Session '{}': {} has resigned (left session), reason: '{}'.",
        sessionName,
        getClientNameForDebug(),
        reason);
    if (hasStreamToClient()) {
      logger.debug(
          "Session '{}': {} closing streaming connection to C2SIM client.",
          sessionName,
          getClientNameForDebug());
      this.webSocketClient.closeConnection(
          WsCloseStatus.NORMAL_CLOSURE, "Requested by C2SIM client (resigned)");
    }
  }
}
