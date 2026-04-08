package org.c2sim.server.security;

/**
 * Authentication enforcement levels for the C2SIM server REST API.
 *
 * <p>The active level is read from the server configuration and applied by {@link
 * JavalinAuthHandler#addSecurityToAllEndpoints(io.javalin.http.Context)}.
 */
public enum EAuthLevel {

  /** Authorization is fully disabled; all requests are accepted without a token. */
  NO_AUTH(0),

  /**
   * Mixed mode: when a Bearer token is present it is validated; when absent the request is accepted
   * without authentication. <b>Do not use in production.</b>
   */
  MIXED_AUTH(1),

  /** Strict mode: only requests carrying a valid Bearer token are accepted. */
  STRICT_AUTH(2);

  private final int authLevel;

  EAuthLevel(int authLevel) {
    this.authLevel = authLevel;
  }

  /**
   * Returns the numeric auth level.
   *
   * @return the integer auth level ({@code 0}, {@code 1}, or {@code 2})
   */
  public int getAuthLevel() {
    return authLevel;
  }

  /**
   * Returns the {@link EAuthLevel} constant that matches the given numeric level.
   *
   * @param authLevel the numeric auth level to look up
   * @return the matching constant
   * @throws IllegalArgumentException if no constant matches the given level
   */
  public static EAuthLevel fromAuthLevel(int authLevel) {
    for (EAuthLevel l : values()) {
      if (l.authLevel == authLevel) {
        return l;
      }
    }
    throw new IllegalArgumentException("Unknown auth level: " + authLevel);
  }
}
