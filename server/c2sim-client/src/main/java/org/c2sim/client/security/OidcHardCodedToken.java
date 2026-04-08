package org.c2sim.client.security;

import org.c2sim.client.exceptions.C2SimRestAuthException;

/**
 * {@link OidcTokenProvider} that always returns a fixed, pre-configured Bearer token.
 *
 * <p>Useful for testing or environments where a static token is known in advance and no dynamic
 * OIDC credential flow is required.
 */
public final class OidcHardCodedToken implements OidcTokenProvider {

  private final String token;

  /**
   * Creates a provider that returns the given Bearer token on every call.
   *
   * @param bearerToken the static Bearer token string
   */
  public OidcHardCodedToken(String bearerToken) {
    this.token = bearerToken;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the fixed Bearer token supplied at construction time.
   */
  @Override
  public String getAccessToken() throws C2SimRestAuthException {
    return token;
  }

  /**
   * {@inheritDoc}
   *
   * <p>No-op: a hard-coded token cannot be refreshed.
   */
  @Override
  public void forceRefresh() throws C2SimRestAuthException {
    // do nothing
  }
}
