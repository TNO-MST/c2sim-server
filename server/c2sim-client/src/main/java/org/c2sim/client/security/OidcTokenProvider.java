package org.c2sim.client.security;

import org.c2sim.client.exceptions.C2SimRestAuthException;

/**
 * Strategy interface for obtaining OIDC Bearer access tokens.
 *
 * <p>Implementations are responsible for token acquisition, caching, and renewal. The {@link
 * AuthInterceptor} calls {@link #getAccessToken()} before each HTTP request.
 */
public interface OidcTokenProvider {

  /**
   * Returns a currently valid Bearer access token.
   *
   * <p>Implementations may cache tokens and refresh them when they are about to expire.
   *
   * @return a non-null Bearer access token string
   * @throws C2SimRestAuthException if the token cannot be obtained or refreshed
   */
  String getAccessToken() throws C2SimRestAuthException;

  /**
   * Forces an immediate token refresh, bypassing any cached value.
   *
   * @throws C2SimRestAuthException if the token endpoint cannot be reached or returns an error
   */
  void forceRefresh() throws C2SimRestAuthException;
}
