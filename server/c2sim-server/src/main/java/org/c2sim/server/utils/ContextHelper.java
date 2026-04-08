package org.c2sim.server.utils;

import io.javalin.http.Context;
import org.c2sim.authorization.interfaces.C2SimAuthorizer;
import org.c2sim.lox.schema.C2SIMHeaderType;
import org.c2sim.server.security.JavalinAuthHandler;
import org.c2sim.server.services.WebService;

/** Javalin context helper functions */
public class ContextHelper {
  /**
   * Javalin request-header name used to carry the caller's client identifier. Mandatory for all
   * {@code GET}, {@code POST}, {@code PUT}, and {@code DELETE} API calls.
   */
  public static final String ATTRIB_HEADER_PARAM_CLIENT_ID = "clientId";

  /** Javalin context-attribute key under which the per-request NanoID tracking ID is stored. */
  public static final String ATTRIB_TRACKING_ID = "trackingId"; // Tracking ID in javalin ctx

  public static final String ATTRIB_SHARED_SESSION_ID =
      "sharedSessionId"; // Shared Session ID in javalin ctx
  public static final String ATTRIB_C2SIM_HEADER = "c2simHeader"; // C2SIM Header

  /**
   * Javalin context-attribute key under which the {@link WebService} instance is stored so that
   * downstream handlers (e.g. {@link JavalinAuthHandler}) can retrieve it.
   */
  public static final String ATTRIB_WEB_SERVICE = "WebService"; // Attach config service to CTX

  /** Measure time of request */
  public static final String ATTRIB_METRIC_START = "metricStartTime";

  /**
   * Javalin context-attribute key under which the authenticated {@code C2SimAuthorizerImpl} is
   * stored after successful Bearer-token validation.
   */
  public static final String ATTRIB_AUTHORIZER = "authorizer";

  // Prevent instantiation
  private ContextHelper() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Get attribute value from context
   *
   * @param ctx Javalin context
   * @param key Storage key
   * @return the attribute value or default value
   */
  public static String getAttributeValue(Context ctx, String key) {
    return getAttributeValue(ctx, key, "<unknown>");
  }

  /**
   * Get attribute value from context
   *
   * @param ctx Javalin context
   * @param key Storage key
   * @param defaultValue Default value
   * @return the attribute value or default value
   */
  public static String getAttributeValue(Context ctx, String key, String defaultValue) {
    var result = ctx.attribute(key);
    return (result != null) ? (String) result : defaultValue;
  }

  /**
   * Return the C2SIM header from javalin context (if exist)
   *
   * @param ctx Javalin context
   * @return header or null
   */
  public static C2SIMHeaderType getC2SimHeader(Context ctx) {
    return ctx.attribute(ATTRIB_C2SIM_HEADER);
  }

  /**
   * Return the C2SimAuthorizer from javalin context (if exist)
   *
   * @param ctx Javalin context
   * @return C2SimAuthorizer or null
   */
  public static C2SimAuthorizer getAuthorizer(Context ctx) {
    return ctx.attribute(ATTRIB_AUTHORIZER);
  }
}
