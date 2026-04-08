package org.c2sim.server.sessions;

import static org.c2sim.server.exceptions.C2SimException.PROP_SCHEMA_VERSION;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.c2sim.server.api.models.RequestCreateSession;
import org.c2sim.server.exceptions.C2SimException;
import org.c2sim.server.services.C2SimSchemaService;
import org.c2sim.server.services.ConfigService;
import org.c2sim.server.services.MetricService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe registry that owns all active {@link SharedSession}s for the server.
 *
 * <p>Sessions are keyed by their unique name. Implements {@link Iterable} so callers can iterate
 * over all active sessions without exposing the backing map directly.
 */
public class SharedSessionManager implements Iterable<SharedSession> {
  private static final Logger logger = LoggerFactory.getLogger(SharedSessionManager.class);

  private final ConcurrentMap<String, SharedSession> sharedSessions = new ConcurrentHashMap<>();
  private final ConfigService configService;
  private final C2SimSchemaService c2simSchemaService;
  private final MetricService metricService;

  /**
   * Creates the manager.
   *
   * @param configService the configuration service
   * @param c2simSchemaService the schema service used when creating new sessions
   * @param metricService the metric service
   */
  public SharedSessionManager(
      ConfigService configService,
      C2SimSchemaService c2simSchemaService,
      MetricService metricService) {
    this.configService = Objects.requireNonNull(configService);
    this.c2simSchemaService = Objects.requireNonNull(c2simSchemaService);
    this.metricService = Objects.requireNonNull(metricService);
  }

  /**
   * Looks up a session by name.
   *
   * @param sharedSessionName the session name
   * @param throwExceptionWhenNotExist {@code true} to throw when the session does not exist
   * @return the {@link SharedSession}, or {@code null} when not found and {@code
   *     throwExceptionWhenNotExist} is {@code false}
   * @throws C2SimException with {@link C2SimException.ErrorCode#SHARED_SESSION_NOT_FOUND} if the
   *     session does not exist and {@code throwExceptionWhenNotExist} is {@code true}
   */
  public SharedSession getSharedSession(
      String sharedSessionName, boolean throwExceptionWhenNotExist) {
    if (throwExceptionWhenNotExist && !sharedSessions.containsKey(sharedSessionName)) {

      throw new C2SimException(
          C2SimException.ErrorCode.SHARED_SESSION_NOT_FOUND,
          String.format("Shared session '%s' doesn't exists.", sharedSessionName),
          new HashMap<>(
              Map.of(C2SimException.PROP_ACTIVE_SESSIONS, (Object) getSharedSessionNames())));
    }
    return sharedSessions.getOrDefault(sharedSessionName, null);
  }

  /**
   * Returns a semicolon-separated string of all active session names.
   *
   * @return the session names string
   */
  public String getSharedSessionNames() {
    return String.join(";", sharedSessions.keySet());
  }

  /**
   * Creates a new shared session or updates an existing one from the supplied request.
   *
   * <p>If the session already exists, the schema version must not change; attempting to change it
   * throws a {@link C2SimException}.
   *
   * @param sharedSessionName the session name
   * @param sharedSessionRequest the create/update request body
   * @return the created or updated {@link SharedSession}
   * @throws C2SimException with {@link
   *     C2SimException.ErrorCode#SHARED_SESSION_SCHEMA_VERSION_CHANGED} if an existing session's
   *     schema version would be changed
   */
  public SharedSession addOrUpdateSharedSession(
      String sharedSessionName, RequestCreateSession sharedSessionRequest) {

    var session = getSharedSession(sharedSessionName, false);
    if (session == null) {
      // Create shared session
      var sharedSession =
          new SharedSession(
              metricService,
              configService,
              c2simSchemaService,
              sharedSessionName,
              sharedSessionRequest.getData().getC2simSchemaVersion(),
              sharedSessionRequest.getData().getDisplayName(),
              sharedSessionRequest.getData().getDescription(),
              false);
      addSharedSession(sharedSession);
      return sharedSession;
    } else {
      // Update shared session

      // Check if schema version is changed (not allowed)
      if (!session
          .getSchemaVersion()
          .equalsIgnoreCase(sharedSessionRequest.getData().getC2simSchemaVersion())) {
        throw new C2SimException(
            C2SimException.ErrorCode.SHARED_SESSION_SCHEMA_VERSION_CHANGED,
            String.format(
                "Not allowed to change schema version in shared session ('%s' -> '%s')",
                session.getSchemaVersion(), sharedSessionRequest.getData().getC2simSchemaVersion()),
            new HashMap<>(Map.of(PROP_SCHEMA_VERSION, session.getSchemaVersion())));
      }
      // TODO Update session properties....
      return session;
    }
  }

  /**
   * Registers a pre-constructed {@link SharedSession} in the registry.
   *
   * @param sharedSession the session to add
   * @throws C2SimException with {@link C2SimException.ErrorCode#SHARED_SESSION_ALREADY_EXIST} if a
   *     session with the same name already exists
   */
  public void addSharedSession(SharedSession sharedSession) {
    if (getSharedSession(sharedSession.getSharedSessionName(), false) == null /* doesn't exist */) {
      logger.info("Adding shared session :\n{}", sharedSession);
      sharedSessions.put(sharedSession.getSharedSessionName(), sharedSession);
    } else {
      throw new C2SimException(
          C2SimException.ErrorCode.SHARED_SESSION_ALREADY_EXIST,
          String.format(
              "Shared session '%s' already exist.", sharedSession.getSharedSessionName()));
    }
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public Iterator<SharedSession> iterator() {
    return sharedSessions.values().iterator();
  }

  /**
   * Returns an unmodifiable view of all active sessions.
   *
   * @return the collection of active {@link SharedSession}s
   */
  public Collection<SharedSession> getSessions() {
    // Expose read-only view if desired
    return sharedSessions.values();
  }
}
