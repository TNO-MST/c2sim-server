package org.c2sim.authorization.impl;

import java.net.URL;
import org.c2sim.authorization.datatypes.ClaimValueList;
import org.c2sim.authorization.exceptions.AuthorisationException;
import org.c2sim.authorization.interfaces.C2SimClaims;
import org.c2sim.authorization.openid.OidcProviderConfiguration;
import org.c2sim.authorization.utils.PemUtil;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.ErrorCodeValidator;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;

/**
 * Builder for validating JWT Bearer tokens and extracting {@link C2SimClaims}.
 *
 * <p>Two factory methods are provided to supply the JWT signature-verification key:
 *
 * <ul>
 *   <li>{@link #createWithPublicKey(String)} — use a static PEM-encoded RSA public key; no network
 *       access to the identity provider is required at runtime.
 *   <li>{@link #createWithKeycloakConfiguration(URL)} — fetch the JWKS (JSON Web Key Set)
 *       dynamically from the Keycloak OpenID Connect discovery endpoint.
 * </ul>
 *
 * <p>After construction, call {@link #addAudience(String)} to restrict accepted audiences and
 * {@link #build(String)} to validate a token and obtain a {@link C2SimClaims} instance.
 *
 * <p>The builder enforces expiration time validation and allows up to 30 seconds of clock skew by
 * default.
 */
public class C2SimClaimsBuilder {

  private final JwtConsumerBuilder jwtConsumerBuilder;

  /**
   * Creates a builder that verifies JWT signatures with the supplied PEM-encoded public key.
   *
   * @param publicKeyPem the RSA public key in PEM format (must include {@code -----BEGIN PUBLIC
   *     KEY-----} headers)
   * @throws AuthorisationException if {@code publicKeyPem} is {@code null} or cannot be parsed
   */
  // Use  public key (provided by keycloak) for signing validation (no need to access keycloak)
  private C2SimClaimsBuilder(String publicKeyPem) throws AuthorisationException {
    if (publicKeyPem == null) {
      throw new AuthorisationException("No public key provided");
    }
    // Convert PEM string to PublicKey
    java.security.PublicKey publicKey = null;
    try {
      publicKey = PemUtil.getPublicKeyFromPem(publicKeyPem);
    } catch (Exception ex) {
      throw new AuthorisationException("Public key has invalid format (must be PEM BASE64)");
    }

    this.jwtConsumerBuilder =
        createBaseJwtConsumerBuilder().setVerificationKey(publicKey); // Use public key
  }

  /**
   * Creates a builder that fetches JWT signing keys dynamically from the given OpenID Connect
   * provider.
   *
   * @param openIdProvider the URL of the OpenID Connect discovery endpoint (e.g. {@code
   *     http://host:8080/realms/demo/.well-known/openid-configuration})
   * @throws AuthorisationException if {@code openIdProvider} is {@code null}, the discovery
   *     endpoint cannot be reached, or the JWKS URI is absent from the provider configuration
   */
  private C2SimClaimsBuilder(URL openIdProvider) throws AuthorisationException {
    if (openIdProvider == null) {
      throw new AuthorisationException("No OpenID Provider provided");
    }

    var configuration =
        OidcProviderConfiguration.downloadOpenIdConfigurationFromKeycloak(openIdProvider);

    if (configuration.getJwksUri() == null) {
      throw new AuthorisationException(
          "No JWKS URI returned by OpenID Provider at " + openIdProvider.toExternalForm());
    }

    // Fetch encryption information from openid provider
    HttpsJwksVerificationKeyResolver httpsJwksKeyResolver =
        new HttpsJwksVerificationKeyResolver(new HttpsJwks(configuration.getJwksUri()));

    this.jwtConsumerBuilder =
        createBaseJwtConsumerBuilder()
            .setVerificationKeyResolver(
                httpsJwksKeyResolver); // Use openid provide for encryption keys
  }

  /**
   * Creates a {@link C2SimClaimsBuilder} that fetches JWT signing keys from the given Keycloak
   * OpenID Connect discovery URL.
   *
   * <p>The discovery URL follows the format: {@code
   * http://<host>:<port>/realms/<realm>/.well-known/openid-configuration}
   *
   * @param openIdProvider the Keycloak discovery URL
   * @return a new builder configured for dynamic key resolution
   * @throws AuthorisationException if the provider cannot be reached or the JWKS URI is missing
   */
  // The encryption keys are downloaded from keycloak
  // This http://<host keycloak>:<port keycloak>/realms/<realm>/.well-known/openid-configuration
  public static C2SimClaimsBuilder createWithKeycloakConfiguration(URL openIdProvider)
      throws AuthorisationException {
    return new C2SimClaimsBuilder(openIdProvider);
  }

  /**
   * Creates a {@link C2SimClaimsBuilder} that verifies JWT signatures with a static PEM public key.
   *
   * <p>The public key must be in PEM format:
   *
   * <pre>{@code
   * -----BEGIN PUBLIC KEY-----
   * MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwG1w0eN5eVc...
   * -----END PUBLIC KEY-----
   * }</pre>
   *
   * @param publicKeyPem the PEM-encoded RSA public key string
   * @return a new builder configured for static key verification
   * @throws AuthorisationException if the key is {@code null} or cannot be parsed
   */
  /*
  Public key in PEM format
  -----BEGIN PUBLIC KEY-----
  MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwG1w0eN5eVc+i5ZQbl...
  ...rest of your public key...
   -----END PUBLIC KEY-----
   */
  public static C2SimClaimsBuilder createWithPublicKey(String publicKeyPem)
      throws AuthorisationException {
    return new C2SimClaimsBuilder(publicKeyPem);
  }

  /**
   * Extracts the OAuth 2.0 client name from the {@code client_id} claim of the given JWT claims.
   *
   * @param jwtClaims the parsed JWT claims
   * @return the client name, or an empty string if the claim is absent or malformed
   */
  public static String getClientName(JwtClaims jwtClaims) {
    // TODO is azp claim better?
    if (jwtClaims.hasClaim("client_id")) {
      try {
        return jwtClaims.getStringClaimValue("client_id");
      } catch (MalformedClaimException e) {
        return "";
      }
    }
    return "";
  }

  private JwtConsumerBuilder createBaseJwtConsumerBuilder() {
    return new JwtConsumerBuilder().setRequireExpirationTime().setAllowedClockSkewInSeconds(30);
  }

  /**
   * Restricts token acceptance to the given audience value.
   *
   * <p>Tokens whose {@code aud} claim does not include {@code audience} will be rejected by {@link
   * #build(String)}.
   *
   * @param audience the expected audience string (e.g. {@code "c2sim"})
   * @return this builder for chaining
   */
  public C2SimClaimsBuilder addAudience(String audience) {
    this.jwtConsumerBuilder.setExpectedAudience(audience);
    return this;
  }

  /**
   * Disables all JWT validators (expiration, audience, signature).
   *
   * <p><strong>For testing purposes only.</strong> Do not use in production.
   *
   * @return this builder for chaining
   */
  public C2SimClaimsBuilder disableValidation() {
    this.jwtConsumerBuilder.setSkipAllValidators();
    return this;
  }

  /**
   * Validates the given JWT string and extracts its C2SIM claims.
   *
   * @param jwt the raw JWT Bearer token string
   * @return the validated and parsed {@link C2SimClaims}
   * @throws AuthorisationException if the JWT is invalid, expired, or fails signature verification
   */
  public C2SimClaims build(String jwt) throws AuthorisationException {
    try {
      var jwtConsumer = this.jwtConsumerBuilder.build();
      // Verify that the JWT is valid.
      JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);

      // Extract claims
      return new C2SimClaimsImpl(
          getClientName(jwtClaims),
          ClaimValueList.create(jwtClaims, C2SimClaims.COMMUNICATIVE_ACT_TYPE_CODE),
          ClaimValueList.create(jwtClaims, C2SimClaims.FROM_SENDING_SYSTEM),
          ClaimValueList.create(jwtClaims, C2SimClaims.REPLY_TO_SYSTEM),
          ClaimValueList.create(jwtClaims, C2SimClaims.SECURITY_CLASSIFICATION_CODE),
          ClaimValueList.create(jwtClaims, C2SimClaims.TO_RECEIVING_SYSTEM),
          ClaimValueList.create(jwtClaims, C2SimClaims.MESSAGE_TYPE),
          ClaimValueList.create(jwtClaims, C2SimClaims.SYSTEM_MESSAGE_TYPE));

    } catch (InvalidJwtException ex) {
      var errors =
          ex.getErrorDetails().stream().map(ErrorCodeValidator.Error::getErrorMessage).toList();
      throw new AuthorisationException("Invalid JWT: " + String.join("\n", errors), ex);
    }
  }
}
