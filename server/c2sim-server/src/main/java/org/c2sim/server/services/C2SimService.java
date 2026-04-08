package org.c2sim.server.services;

import java.io.InputStream;
import java.util.List;
import org.c2sim.authorization.interfaces.C2SimAuthorizer;
import org.c2sim.server.api.models.DynamicSessionInfo;
import org.c2sim.server.api.models.RequestCreateSession;
import org.c2sim.server.exceptions.C2SimException;
import org.c2sim.server.sessions.SharedSession;
import org.c2sim.server.sessions.SharedSessionManager;
import org.jetbrains.annotations.Nullable;

/**
 * Core C2SIM server service that manages shared sessions and document publishing.
 *
 * <p>Implementations maintain the set of active {@link SharedSession}s and route published C2SIM
 * XML documents to the appropriate session's connected clients.
 */
public interface C2SimService {

  /**
   * Returns dynamic session information for all active shared sessions on this server.
   *
   * @return the list of session info objects (never {@code null}, may be empty)
   */
  List<DynamicSessionInfo> getSessionsFromServer();

  /**
   * Looks up a shared session by name.
   *
   * @param sharedSessionName the name of the shared session
   * @param throwExceptionWhenNotExist {@code true} to throw a {@link C2SimException} when the
   *     session does not exist; {@code false} to return {@code null}
   * @return the {@link SharedSession}, or {@code null} if not found and {@code
   *     throwExceptionWhenNotExist} is {@code false}
   */
  SharedSession getSharedSession(String sharedSessionName, boolean throwExceptionWhenNotExist);

  /**
   * Creates or updates a shared session from the supplied REST request.
   *
   * @param sharedSessionName the name of the session to create or update
   * @param clientId the ID of the requesting client
   * @param sharedSessionRequest the session creation/update request body
   * @return the created or updated {@link SharedSession}
   */
  SharedSession addOrUpdateSharedSession(
      String sharedSessionName, String clientId, RequestCreateSession sharedSessionRequest);

  /**
   * Returns the {@link SharedSessionManager} responsible for managing all active sessions.
   *
   * @return the shared session manager
   */
  SharedSessionManager getSharedSessionManager();

  /**
   * Adds a pre-constructed shared session to the server's session registry.
   *
   * @param sharedSession the session to add
   */
  void addSharedSession(SharedSession sharedSession);

  /**
   * Publishes a C2SIM XML document (as a stream) to the given shared session.
   *
   * @param sharedSessionName the target shared session
   * @param publishingClientId the ID of the publishing client
   * @param trackingId a correlation identifier for the publish operation
   * @param auth authorization information for message
   * @param xmlDoc the C2SIM XML content as an input stream
   * @throws C2SimException if the session is not found, the XML is invalid, or publishing fails
   */
  void publishC2SimDoc(
      String sharedSessionName,
      String publishingClientId,
      String trackingId,
      InputStream xmlDoc,
      @Nullable C2SimAuthorizer auth)
      throws C2SimException;

  /**
   * See publishC2SimDoc
   *
   * @param sharedSessionName the target shared session
   * @param publishingClientId the ID of the publishing client
   * @param trackingId a correlation identifier for the publish operation
   * @param xmlDoc the C2SIM XML content as an input stream
   * @throws C2SimException if the session is not found, the XML is invalid, or publishing fails
   */
  void publishC2SimDoc(
      String sharedSessionName, String publishingClientId, String trackingId, InputStream xmlDoc)
      throws C2SimException;
}
