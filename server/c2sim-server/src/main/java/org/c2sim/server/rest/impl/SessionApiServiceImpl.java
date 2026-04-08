package org.c2sim.server.rest.impl;

import static org.c2sim.server.utils.ContextHelper.ATTRIB_SHARED_SESSION_ID;
import static org.c2sim.server.utils.ContextHelper.ATTRIB_TRACKING_ID;

import io.javalin.http.Context;
import java.util.List;
import java.util.Objects;
import org.c2sim.server.api.apis.SessionApiService;
import org.c2sim.server.api.models.*;
import org.c2sim.server.services.C2SimService;
import org.c2sim.server.utils.ContextHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SessionApiService} implementation that handles all C2SIM session lifecycle operations.
 *
 * <p>Delegates to {@link C2SimService} for the actual business logic and maps results to the
 * generated API model objects expected by the Javalin router.
 */
public class SessionApiServiceImpl implements SessionApiService {

  private static final Logger logger = LoggerFactory.getLogger(SessionApiServiceImpl.class);

  private final C2SimService c2simService;

  /**
   * Creates the service.
   *
   * @param c2simService the C2SIM service that manages shared sessions
   */
  public SessionApiServiceImpl(C2SimService c2simService) {
    this.c2simService = c2simService;
    logger.debug("SessionApiServiceImpl created.");
  }

  /**
   * {@inheritDoc}
   *
   * <p>Creates or updates the named session and returns whether it was newly {@link
   * CreateUpdateType#CREATED} or {@link CreateUpdateType#UPDATED}.
   */
  @NotNull
  @Override
  public ResponseCreateSession createSession(
      @NotNull String clientId,
      @NotNull String sharedSessionName,
      @NotNull RequestCreateSession requestCreateSession,
      @NotNull Context ctx) {
    ctx.attribute(ATTRIB_SHARED_SESSION_ID, sharedSessionName);
    var current = c2simService.getSharedSession(sharedSessionName, false);
    var session =
        c2simService.addOrUpdateSharedSession(sharedSessionName, clientId, requestCreateSession);
    return new ResponseCreateSession(
        current != null ? CreateUpdateType.UPDATED : CreateUpdateType.CREATED,
        session.createDynamicInfo());
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public ResponseDeleteSession deleteSession(
      @NotNull String clientId,
      @NotNull String sharedSessionName,
      @Nullable Boolean force,
      @NotNull Context ctx) {
    ctx.attribute(ATTRIB_SHARED_SESSION_ID, sharedSessionName);
    var session = c2simService.getSharedSession(sharedSessionName, true);
    session.deleteSession(clientId, force);
    return new ResponseDeleteSession("session deleted");
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the C2SIM initialization document for the session as a raw XML string. The Javalin
   * before-handler intercepts this route and sets {@code Content-Type: application/xml} before the
   * generated route handler can override it with JSON.
   */
  @NotNull
  @Override
  public String getSessionInitialization(
      @NotNull String clientId, @NotNull String sharedSessionName, @NotNull Context ctx) {
    ctx.attribute(ATTRIB_SHARED_SESSION_ID, sharedSessionName);
    var session = c2simService.getSharedSession(sharedSessionName, true);
    // Fixed in after handler
    return session.getC2SIMInitializationAsTextXml();
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public DynamicSessionInfo getSessionInfo(
      @NotNull String clientId, @NotNull String sharedSessionName, @NotNull Context ctx) {
    ctx.attribute(ATTRIB_SHARED_SESSION_ID, sharedSessionName);
    var session = c2simService.getSharedSession(sharedSessionName, true);
    return session.createDynamicInfo();
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public List<DynamicSessionInfo> getSessions(@NotNull String clientId, @NotNull Context ctx) {
    return c2simService.getSessionsFromServer();
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public ResponseJoinSession joinSession(
      @NotNull String clientId,
      @NotNull String sharedSessionName,
      @NotNull RequestJoinSession requestJoinSession,
      @NotNull Context ctx) {
    ctx.attribute(ATTRIB_SHARED_SESSION_ID, sharedSessionName);
    var trackingId = ContextHelper.getAttributeValue(ctx, ATTRIB_TRACKING_ID);
    var session = c2simService.getSharedSession(sharedSessionName, true);
    session.joinSharedSession(clientId, trackingId, requestJoinSession);
    return new ResponseJoinSession(session.createDynamicInfo());
  }

  /** {@inheritDoc} */
  @NotNull
  @Override
  public ResponseResignSession resignFromSession(
      @NotNull String clientId,
      @NotNull String sharedSessionName,
      @NotNull RequestResignSession requestResignSession,
      @NotNull Context ctx) {
    ctx.attribute(ATTRIB_SHARED_SESSION_ID, sharedSessionName);
    var trackingId = ContextHelper.getAttributeValue(ctx, ATTRIB_TRACKING_ID);
    var session = c2simService.getSharedSession(sharedSessionName, true);
    String reason = Objects.requireNonNullElse(requestResignSession.getReason(), "");
    session.resignSession(clientId, trackingId, reason);
    return new ResponseResignSession(
        String.format(
            "Resigned client Id '%s' from shared session '%s' (tracking id %s)",
            clientId, session.getSharedSessionName(), trackingId));
  }
}
