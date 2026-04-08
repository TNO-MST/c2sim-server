package org.c2sim.server.rest.impl;

import static org.c2sim.server.utils.ContextHelper.ATTRIB_SHARED_SESSION_ID;

import io.javalin.http.Context;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.c2sim.server.api.apis.NotificationsApiService;
import org.c2sim.server.api.models.ResponseStreamEndpoints;
import org.c2sim.server.api.models.ResponseStreamEndpointsWebsocket;
import org.c2sim.server.services.C2SimService;
import org.c2sim.server.services.ConfigService;
import org.c2sim.server.services.impl.DefaultWebSocketService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link NotificationsApiService} implementation that handles stream-endpoint discovery and client
 * subscription requests.
 *
 * <p>The stream endpoint URL is constructed from the configured external hostname / port so that
 * clients receive a reachable WebSocket URL even when the server is behind a reverse proxy.
 */
public class NotificationsApiServiceImp implements NotificationsApiService {

  private static final Logger logger = LoggerFactory.getLogger(NotificationsApiServiceImp.class);

  private final C2SimService c2simService;
  private final ConfigService config;

  /**
   * Creates the service.
   *
   * @param config the configuration service used to resolve external hostname and port
   * @param c2simService the C2SIM service used to validate session names
   */
  public NotificationsApiServiceImp(ConfigService config, C2SimService c2simService) {
    this.c2simService = c2simService;
    this.config = config;
    logger.debug(("NotificationsApiServiceImp started"));
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the WebSocket URL through which clients can receive C2SIM document notifications.
   * Uses the configured external hostname/port when available; falls back to the hostname from the
   * incoming HTTP request.
   */
  @NotNull
  @Override
  public ResponseStreamEndpoints getStreamEndpoints(
      @NotNull String clientId, @NotNull String sharedSessionName, @NotNull Context ctx) {
    ctx.attribute(ATTRIB_SHARED_SESSION_ID, sharedSessionName);
    // Check if session name is valid
    c2simService.getSharedSession(sharedSessionName, true);

    // TODO Should return FQDN; DNS resolve problem clients + cors
    String externalHostName = config.getExternalHostname();
    var hostname =
        (externalHostName != null && !externalHostName.isBlank())
            ? String.format("%s:%d", externalHostName, config.getExternalPort())
            : ctx.host();

    String encodedClientId = URLEncoder.encode(clientId, StandardCharsets.UTF_8);
    String url =
        String.format(
            "%s://%s/api/c2sim/session/%s/ws?%s=%s",
            ctx.scheme().equals("http") ? "ws" : "wss",
            hostname,
            sharedSessionName,
            DefaultWebSocketService.QUERY_PARAM_CLIENT_ID,
            encodedClientId);

    return new ResponseStreamEndpoints(new ResponseStreamEndpointsWebsocket(url));
  }
}
