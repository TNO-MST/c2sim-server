package org.c2sim.server.security;

import java.net.URI;

/**
 * Immutable configuration for OIDC token validation.
 *
 * <p>Used to configure the JWT/OIDC claims builder with the expected issuer, audience, and optional
 * JWKS endpoint URI.
 */
public class OidcConfig {

  /** The expected {@code iss} (issuer) claim value. */
  public final String issuer;

  /** The expected {@code aud} (audience) claim value. */
  public final String audience;

  /**
   * The JWKS URI used to retrieve public keys for token signature verification, or {@code null} to
   * use OIDC discovery.
   */
  public final URI jwksUri;

  /**
   * Creates an OIDC configuration with automatic JWKS discovery (no explicit JWKS URI).
   *
   * @param issuer the expected issuer claim
   * @param audience the expected audience claim
   */
  public OidcConfig(String issuer, String audience) {
    this(issuer, audience, null);
  }

  /**
   * Creates an OIDC configuration with an explicit JWKS URI.
   *
   * @param issuer the expected issuer claim
   * @param audience the expected audience claim
   * @param jwksUri the JWKS endpoint URI, or {@code null} to use OIDC discovery
   */
  public OidcConfig(String issuer, String audience, URI jwksUri) {
    this.issuer = issuer;
    this.audience = audience;
    this.jwksUri = jwksUri;
  }
}
