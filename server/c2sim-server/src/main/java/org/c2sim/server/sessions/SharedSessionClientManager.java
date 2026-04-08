package org.c2sim.server.sessions;

import jakarta.validation.constraints.Null;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.c2sim.lox.C2SimMsgKind;
import org.c2sim.server.exceptions.C2SimException;
import org.c2sim.server.utils.XmlMinifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages all {@link SharedSessionClient}s connected to a single {@link SharedSession}.
 *
 * <p>Provides thread-safe lookup, creation, and removal of clients as well as the {@link
 * #distributeMessage} method that fans out a C2SIM XML document to every eligible connected client.
 *
 * <p>Implements {@link Iterable} so callers can iterate over all registered clients.
 */
public class SharedSessionClientManager implements Iterable<SharedSessionClient> {

  private static final Logger logger = LoggerFactory.getLogger(SharedSessionClientManager.class);

  private final ConcurrentMap<String, SharedSessionClient> sharedSessionClients =
      new ConcurrentHashMap<>();
  private final SharedSession owner;

  /**
   * Creates the manager for the given session.
   *
   * @param owner the owning {@link SharedSession}
   */
  public SharedSessionClientManager(SharedSession owner) {
    this.owner = owner;
  }

  /**
   * Returns the existing client for {@code clientId} or atomically creates a new one.
   *
   * @param clientId the client identifier
   * @return the existing or newly created {@link SharedSessionClient}
   */
  public SharedSessionClient getOrCreateClientById(String clientId) {
    return sharedSessionClients.computeIfAbsent(
        clientId, id -> new SharedSessionClient(owner.getSharedSessionName(), clientId));
  }

  /**
   * Returns the client for the given ID, or {@code null} if no such client exists.
   *
   * @param clientId the client identifier
   * @return the {@link SharedSessionClient}, or {@code null}
   */
  public SharedSessionClient getClientById(String clientId) {
    return sharedSessionClients.getOrDefault(clientId, null);
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public Iterator<SharedSessionClient> iterator() {
    return sharedSessionClients.values().iterator();
  }

  /**
   * Returns an unmodifiable view of all registered clients.
   *
   * @return the collection of all {@link SharedSessionClient}s
   */
  public Collection<SharedSessionClient> getClientSessions() {
    // Expose read-only view if desired
    return sharedSessionClients.values();
  }

  /**
   * Minifies the C2SIM XML document and delivers it to all eligible clients.
   *
   * <p>Newline-minified XML is used as the stream-wire format; a trailing {@code \n} acts as the
   * message separator for clients. Each client's {@link SharedSessionClient#sendC2SimMessage}
   * applies its own eligibility rules (joined, has stream, not the publisher).
   *
   * @param publisher the publishing client, or {@code null} when the C2SIM server itself is the
   *     source
   * @param trackingId a correlation identifier for tracing
   * @param kind the C2SIM message kind
   * @param c2simMessage the raw XML content
   */
  // When publisher is null, the C2SIM server self is publishing
  public void distributeMessage(
      @Null SharedSessionClient publisher,
      @NotNull String trackingId,
      @NotNull C2SimMsgKind kind,
      @NotNull ByteArrayInputStream c2simMessage) {

    // TODO Optimize (c2simMessage directly to stripped string)
    var xml = new String(c2simMessage.readAllBytes(), StandardCharsets.UTF_8);
    // For easy parsing on the streaming client the \n is used as message separator
    // Remove all \n from XML (and spaces to compact message)
    var streamReadyXml = XmlMinifier.minifyXml(xml) + "\n";
    logger.trace(
        "Session '{}': C2SIM XML doc '{}' content: '{}'.",
        trackingId,
        owner.getSharedSessionName(),
        streamReadyXml);

    List<String> sendTo = new ArrayList<>();
    long distributeCount = 0;
    for (var client : this) {
      // sendC2SimMessage will check if client is allowed to receive te message (is joined, no
      // filter, etc.)
      if (client.sendC2SimMessage(publisher, streamReadyXml)) {
        sendTo.add(client.getClientNameForDebug());
        distributeCount++;
      }
    }
    String toSystems = String.join(",", sendTo);
    logger.debug(
        "Session '{}': C2SIM XML doc '{}' send to {}.",
        owner.getSharedSessionName(),
        trackingId,
        toSystems);
    logger.info(
        "Session '{}': Distributed C2SIM XML document '{}' ({} bytes) of kind '{}' "
            + "published by {} to {} C2SIM client(s). ",
        owner.getSharedSessionName(),
        trackingId,
        streamReadyXml.length(),
        kind,
        publisher != null ? publisher.getClientNameForDebug() : "C2SIM-SERVER",
        distributeCount);
  }

  /**
   * Removes the client from this session and closes any active streaming connection.
   *
   * @param clientId the client identifier
   * @param trackingId a correlation identifier for tracing
   * @param reason the reason for resigning
   * @throws C2SimException with {@link C2SimException.ErrorCode#NO_CLIENT_ID} if no client with the
   *     given ID exists in this session
   */
  public void resignClient(
      @NotNull String clientId, @NotNull String trackingId, @NotNull String reason) {
    var client = getClientById(clientId);
    if (client == null) {
      var clientIdNotFound =
          new C2SimException(
              C2SimException.ErrorCode.NO_CLIENT_ID,
              String.format(
                  "Cannot resign from Shared Session '%s', client ID '%s' not exist (tracking id '%s').",
                  owner.getSharedSessionName(), clientId, trackingId),
              new HashMap<>(Map.of(C2SimException.PROP_TRACKING_ID, trackingId)));
      logC2SimException(clientIdNotFound);
      throw clientIdNotFound;
    }
    sharedSessionClients.remove(clientId); // Remove client from list
    client.resign(reason); // Disconnect streaming client
  }

  private void logC2SimException(C2SimException exception) {
    logger.error(
        "Session '{}': ERROR[{}]: {}",
        owner.getSharedSessionName(),
        exception.getError().getCode(),
        exception.getMessage());
  }

  /** Logs the current list of registered clients at {@code DEBUG} level. */
  public void logC2SimClientsInfo() {
    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format(
            "Shared session '%s' registered C2SIM clients:%n", owner.getSharedSessionName()));
    for (var client : sharedSessionClients.entrySet()) {
      sb.append(String.format("- %s%n", client.getValue()));
    }
    var logText = sb.toString();
    logger.debug(logText);
  }
}
