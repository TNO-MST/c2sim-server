package org.c2sim.client.security;

import java.net.URI;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable configuration record for {@link OidcCredentialFlow}.
 *
 * <p>Three convenience constructors are provided:
 *
 * <ol>
 *   <li>{@link #OidcCredentialFlowConfig(URI, String, String)} — supply the IDP URI directly, using
 *       default timeouts and scope.
 *   <li>{@link #OidcCredentialFlowConfig(String, boolean, int, String, String, String)} — build the
 *       Keycloak IDP URI from its host, port, and realm components.
 *   <li>The canonical record constructor — full control over all fields.
 * </ol>
 *
 * <p>The {@code ipdUri} may point to either the Keycloak realm root (e.g. {@code
 * http://host:8080/realms/demo/}) or directly to the token endpoint (e.g. {@code
 * .../protocol/openid-connect/token}).
 *
 * @param ipdUri The identity provider url (IPD)
 * @param clientId The client id (OIDC)
 * @param clientSecret The client secret (OIDC)
 * @param connectTimeoutMs Timeout for connecting
 * @param readTimeoutMs Timeout for read operations
 * @param scope The OIDC scope: default c2sim
 */
public record OidcCredentialFlowConfig(
    @NotNull URI ipdUri, // Keycloak example: http://localhost:8080/realms/demo/
    @NotNull String clientId,
    @NotNull String clientSecret,
    int connectTimeoutMs,
    int readTimeoutMs,
    @NotNull String scope) {

  private static final int DEFAULT_CONNECTION_TIMEOUT_MS = 3000;
  private static final int DEFAULT_READ_TIMEOUT_MS = 5000;
  private static final String DEFAULT_SCOPE = "c2sim";

  /**
   * Compact canonical constructor — validates required fields.
   *
   * @throws NullPointerException if {@code clientId} or {@code clientSecret} is {@code null}
   */
  public OidcCredentialFlowConfig {

    Objects.requireNonNull(clientId, "clientId must not be null");
    Objects.requireNonNull(clientSecret, "clientSecret must not be null");
  }

  /**
   * Creates a config from an explicit IDP URI with default timeouts ({@code 3 000 ms} connect,
   * {@code 5 000 ms} read) and the default scope {@code "c2sim"}.
   *
   * @param tokenUri the IDP (or token-endpoint) URI
   * @param clientId the OAuth 2.0 client ID
   * @param clientSecret the OAuth 2.0 client secret
   */
  public OidcCredentialFlowConfig(
      @NotNull URI tokenUri /* default to localhost when null */,
      @NotNull String clientId,
      @NotNull String clientSecret) {

    this(
        tokenUri,
        clientId,
        clientSecret,
        DEFAULT_CONNECTION_TIMEOUT_MS,
        DEFAULT_READ_TIMEOUT_MS,
        DEFAULT_SCOPE);
  }

  /**
   * Creates a config by constructing the Keycloak IDP URI from its components, using default
   * timeouts and scope.
   *
   * <p>The resulting URI follows the Keycloak realm format: {@code
   * http(s)://<host>:<port>/realms/<realm>}.
   *
   * @param keycloakHostName the Keycloak server hostname or IP address
   * @param keycloakIsSecureHost {@code true} to use {@code https}, {@code false} for {@code http}
   * @param keycloakPortNumber the Keycloak server port
   * @param keycloakRealm the Keycloak realm name
   * @param clientId the OAuth 2.0 client ID
   * @param clientSecret the OAuth 2.0 client secret
   */
  public OidcCredentialFlowConfig(
      @NotNull String keycloakHostName,
      boolean keycloakIsSecureHost,
      int keycloakPortNumber,
      @NotNull String keycloakRealm,
      @NotNull String clientId,
      @NotNull String clientSecret) {

    this(
        // Keycloak IPD format
        URI.create(
            String.format(
                "%s://%s:%d/realms/%s",
                keycloakIsSecureHost ? "https" : "http",
                keycloakHostName,
                keycloakPortNumber,
                keycloakRealm)),
        clientId,
        clientSecret,
        DEFAULT_CONNECTION_TIMEOUT_MS,
        DEFAULT_READ_TIMEOUT_MS,
        DEFAULT_SCOPE);
  }
}
