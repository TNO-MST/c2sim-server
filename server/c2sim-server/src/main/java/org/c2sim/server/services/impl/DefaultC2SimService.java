package org.c2sim.server.services.impl;

import static java.lang.String.*;

import com.google.inject.Inject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.c2sim.authorization.interfaces.C2SimAuthorizer;
import org.c2sim.server.api.models.*;
import org.c2sim.server.exceptions.C2SimException;
import org.c2sim.server.services.C2SimSchemaService;
import org.c2sim.server.services.C2SimService;
import org.c2sim.server.services.ConfigService;
import org.c2sim.server.services.MetricService;
import org.c2sim.server.sessions.SharedSession;
import org.c2sim.server.sessions.SharedSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link C2SimService} implementation that delegates to a {@link SharedSessionManager}.
 *
 * <p>On construction the service logs all detected C2SIM schema versions and creates the {@link
 * SharedSessionManager} that owns the active sessions for the lifetime of the server.
 */
public class DefaultC2SimService implements C2SimService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultC2SimService.class);

  private final ConfigService configService;
  private final C2SimSchemaService c2simSchemaService;
  private final MetricService metricService;
  // All active shared sessions
  private final SharedSessionManager sessionManager;

  /**
   * Creates the service, injecting configuration and schema services, and initialising the session
   * manager.
   *
   * @param configService the configuration service (must not be {@code null})
   * @param c2simSchemaService the schema service used to validate published documents (must not be
   *     {@code null})
   */
  @Inject
  public DefaultC2SimService(
      ConfigService configService,
      C2SimSchemaService c2simSchemaService,
      MetricService metricService) {

    this.metricService = Objects.requireNonNull(metricService, "Metric service is null");
    this.configService = Objects.requireNonNull(configService, "Config service is null");
    this.c2simSchemaService =
        Objects.requireNonNull(c2simSchemaService, "C2SIM schema service is null");
    this.sessionManager =
        new SharedSessionManager(this.configService, this.c2simSchemaService, this.metricService);

    var schemaVersions = join("','", c2simSchemaService.getSupportedSchemaVersions());
    logger.info("Detected C2SIM schema folders (versions): '{}'", schemaVersions);
  }

  /** {@inheritDoc} */
  @Override
  public List<DynamicSessionInfo> getSessionsFromServer() {
    var list = new ArrayList<DynamicSessionInfo>();
    for (SharedSession session : sessionManager) {
      list.add(session.createDynamicInfo());
    }
    return list;
  }

  /** {@inheritDoc} */
  @Override
  public SharedSessionManager getSharedSessionManager() {
    return sessionManager;
  }

  /** {@inheritDoc} */
  @Override
  public SharedSession getSharedSession(
      String sharedSessionName, boolean throwExceptionWhenNotExist) {
    return sessionManager.getSharedSession(sharedSessionName, throwExceptionWhenNotExist);
  }

  // Only use for unit testing and debugging

  /** {@inheritDoc} */
  @Override
  public void publishC2SimDoc(
      String sharedSessionName, String clientId, String trackingId, InputStream xmlDoc)
      throws C2SimException {
    publishC2SimDoc(sharedSessionName, clientId, trackingId, xmlDoc, null);
  }

  /** {@inheritDoc} */
  @Override
  public void publishC2SimDoc(
      String sharedSessionName,
      String clientId,
      String trackingId,
      InputStream xmlDoc,
      C2SimAuthorizer authorizer)
      throws C2SimException {
    Objects.requireNonNull(xmlDoc);
    Objects.requireNonNull(sharedSessionName);
    Objects.requireNonNull(trackingId);
    // Get shared session
    var sharedSession = sessionManager.getSharedSession(sharedSessionName, true);
    // Let shared session handle the XML document
    sharedSession.publishC2SimDoc(clientId, trackingId, xmlDoc, authorizer);
  }

  /** {@inheritDoc} */
  @Override
  public SharedSession addOrUpdateSharedSession(
      String sharedSessionName, String clientId, RequestCreateSession sharedSessionRequest) {
    return sessionManager.addOrUpdateSharedSession(sharedSessionName, sharedSessionRequest);
  }

  /** {@inheritDoc} */
  @Override
  public void addSharedSession(SharedSession sharedSession) {
    sessionManager.addSharedSession(sharedSession);
  }
}
