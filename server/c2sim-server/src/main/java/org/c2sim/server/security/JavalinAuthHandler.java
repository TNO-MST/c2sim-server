package org.c2sim.server.security;

import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import org.c2sim.authorization.exceptions.AuthorisationException;
import org.c2sim.authorization.impl.C2SimAuthorizerImpl;
import org.c2sim.server.services.ConfigService;
import org.c2sim.server.services.WebService;
import org.c2sim.server.utils.ContextHelper;

/**
 * Javalin {@code before}-filter that enforces Bearer-token authentication on all {@code /api}
 * endpoints.
 *
 * <p>Behaviour depends on the configured {@link EAuthLevel}:
 *
 * <ul>
 *   <li>{@link EAuthLevel#NO_AUTH} — all requests pass through immediately.
 *   <li>{@link EAuthLevel#MIXED_AUTH} — when a Bearer token is present it is validated and the
 *       resulting {@link C2SimAuthorizerImpl} is stored as a request attribute; when absent the
 *       request proceeds without authentication.
 *   <li>{@link EAuthLevel#STRICT_AUTH} — a valid Bearer token is mandatory; requests without one
 *       are rejected with HTTP 401.
 * </ul>
 *
 * <p>On success the {@link C2SimAuthorizerImpl} is stored in the Javalin request context under the
 * key {@link ContextHelper#ATTRIB_AUTHORIZER} for downstream use by service implementations.
 */
public class JavalinAuthHandler {

  // Prevent instantiation
  private JavalinAuthHandler() {
    throw new AssertionError("Only static functions");
  }

  private static String getBearToken(Context ctx, boolean throwAuthExceptionWhenMissing) {

    String authHeader = ctx.header("Authorization");

    // No Authorization is REST call
    if (authHeader == null) {
      // Is this allowed (e.g. development / testing) ?
      if (throwAuthExceptionWhenMissing) {
        throw new UnauthorizedResponse("Authorization header missing");
      }
      return null;
    }

    authHeader = authHeader.trim();

    if (!authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
      throw new UnauthorizedResponse("Authorization header must use Bearer scheme");
    }

    String token = authHeader.substring(7).trim();
    if (token.isEmpty()) {
      throw new UnauthorizedResponse("Bearer token missing (in Authorization header) ");
    }
    return token;
  }

  /**
   * Javalin {@code before}-handler that authenticates the incoming request.
   *
   * <p>Should be registered with {@code app.before(JavalinAuthHandler::addSecurityToAllEndpoints)}.
   * Throws {@link UnauthorizedResponse} (HTTP 401) when authentication fails.
   *
   * @param ctx the current Javalin request context
   * @throws UnauthorizedResponse if the Bearer token is missing (in strict mode), malformed, or
   *     fails JWT/OIDC validation
   */
  public static void addSecurityToAllEndpoints(Context ctx) throws AuthorisationException {

    // Get web service from context
    WebService webService = ctx.attribute(ContextHelper.ATTRIB_WEB_SERVICE);
    if (webService == null) {
      throw new UnauthorizedResponse("Web service service not attached (Javalin before handler)");
    }

    ConfigService configService = webService.getConfigService();
    if (configService == null) {
      throw new UnauthorizedResponse(
          "Config service service not attached (Javalin before handler)");
    }

    if (configService.getAuthMode() == EAuthLevel.NO_AUTH) {
      // Skip all authentication
      return;
    }

    if (!ctx.path().startsWith("/api")) {
      // Only API is under restriction
      return;
    }

    var token = getBearToken(ctx, configService.getAuthMode() == EAuthLevel.STRICT_AUTH);
    if (token == null) {
      // No bearer token, but is not required (mixed mode: if token use it, else skip auth)
      return;
    }

    // Throws Authorization exception when invalid
    var claims = webService.getClaimsBuilder().build(token);
    var authorizer = new C2SimAuthorizerImpl(claims);
    ctx.attribute(ContextHelper.ATTRIB_AUTHORIZER, authorizer);
  }
}
