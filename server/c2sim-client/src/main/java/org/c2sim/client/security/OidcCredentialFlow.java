package org.c2sim.client.security;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import org.c2sim.client.exceptions.C2SimRestAuthException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link OidcTokenProvider} that obtains access tokens via the OAuth 2.0 Client Credentials grant
 * flow.
 *
 * <p>On the first call to {@link #getAccessToken()} the token endpoint is resolved either directly
 * (when the configured URI points to the token endpoint) or via OIDC Discovery ({@code
 * /.well-known/openid-configuration}). Subsequent calls return the cached token until it expires,
 * at which point a new token is fetched automatically.
 *
 * <p>Thread-safe: concurrent callers contend on an internal {@link ReentrantLock} during token
 * refresh to prevent duplicate requests.
 */
public final class OidcCredentialFlow implements OidcTokenProvider {

  private static final Logger logger = LoggerFactory.getLogger(OidcCredentialFlow.class);

  private final OidcCredentialFlowConfig config;
  private URI tokenEndpoint = null;

  private final ReentrantLock lock = new ReentrantLock();

  private volatile String accessToken;
  private volatile Instant expiryTime;

  /**
   * Creates an {@code OidcCredentialFlow} with the given configuration.
   *
   * @param config the OIDC credential-flow configuration; must not be {@code null}
   * @throws NullPointerException if {@code config} is {@code null}
   */
  public OidcCredentialFlow(@NotNull OidcCredentialFlowConfig config) {

    Objects.requireNonNull(config, "OAuth2ClientCredentialsConfig cannot be null.");

    this.config = config;

    var path = config.ipdUri().getPath();
    if (path != null) {
      if (path.matches("^/realms/[^/]+/protocol/openid-connect/token$")) {
        logger.warn("OIDC Token endpoint was provided, skip discovery");
        logger.warn("OIDC Token endpoint: {}", config.ipdUri());
        this.tokenEndpoint = config.ipdUri();
      } else if (path.endsWith("/well-known/openid-configuration")) {
        logger.warn(
            "Warning: 'well-known/openid-configuration' not needed in URI, is automatically added");
      }
    }
  }

  // ---------------------------------------------------------
  // Public API
  // ---------------------------------------------------------

  /**
   * Returns a valid access token, fetching or refreshing it if necessary.
   *
   * @return the current Bearer access token string
   * @throws C2SimRestAuthException if the token endpoint cannot be reached or returns an error
   */
  public String getAccessToken() throws C2SimRestAuthException {

    if (isTokenValid()) {
      return accessToken;
    }

    lock.lock();
    try {
      if (!isTokenValid()) {
        requestNewToken();
      }
      return accessToken;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Forces an immediate token refresh, bypassing the cached value.
   *
   * @throws C2SimRestAuthException if the token endpoint cannot be reached or returns an error
   */
  public void forceRefresh() throws C2SimRestAuthException {
    lock.lock();
    try {
      requestNewToken();
    } finally {
      lock.unlock();
    }
  }

  // ---------------------------------------------------------
  // Token Handling
  // ---------------------------------------------------------

  private boolean isTokenValid() {
    return accessToken != null && expiryTime != null && Instant.now().isBefore(expiryTime);
  }

  private void requestNewToken() throws C2SimRestAuthException {

    try {
      HTTPResponse httpResponse = getTokenRequestResponse();

      TokenResponse tokenResponse = TokenResponse.parse(httpResponse);

      if (!tokenResponse.indicatesSuccess()) {
        TokenErrorResponse error = tokenResponse.toErrorResponse();

        throw new C2SimRestAuthException(
            C2SimRestAuthException.ErrorType.ACCESS_TOKEN_RETRIEVAL_FAILED,
            "OIDC Access Token request failed: "
                + error.getErrorObject().getCode()
                + " - "
                + error.getErrorObject().getDescription());
      }

      AccessTokenResponse success = tokenResponse.toSuccessResponse();

      AccessToken token = success.getTokens().getAccessToken();

      if (token == null) {
        throw new C2SimRestAuthException(
            C2SimRestAuthException.ErrorType.ACCESS_TOKEN_RETRIEVAL_FAILED,
            "Missing access_token in identity provider response");
      }

      long lifetime = token.getLifetime();
      this.accessToken = token.getValue();

      // Refresh 60 seconds early (if possible)
      long refreshSeconds = lifetime > 60 ? lifetime - 60 : lifetime;

      this.expiryTime = Instant.now().plusSeconds(refreshSeconds);

      logger.debug("Obtained access token, expires at {}", expiryTime);

    } catch (C2SimRestAuthException authException) {
      throw authException; // rethrow
    } catch (Exception e) {
      // Wrap in exception
      throw new C2SimRestAuthException(
          C2SimRestAuthException.ErrorType.ACCESS_TOKEN_RETRIEVAL_FAILED,
          "Failed to obtain access token",
          e);
    }
  }

  @NotNull
  private HTTPResponse getTokenRequestResponse() throws IOException {

    if (tokenEndpoint == null) {
      this.tokenEndpoint = resolveTokenEndpoint();
      logger.info("Resolved OIDC token endpoint: {}", tokenEndpoint);
    }

    TokenRequest request =
        new TokenRequest(
            tokenEndpoint,
            new ClientSecretBasic(
                new ClientID(config.clientId()), new Secret(config.clientSecret())),
            new ClientCredentialsGrant(),
            new Scope(config.scope()));

    HTTPRequest httpRequest = request.toHTTPRequest();
    httpRequest.setConnectTimeout(config.connectTimeoutMs());
    httpRequest.setReadTimeout(config.readTimeoutMs());

    HTTPResponse httpResponse = httpRequest.send();

    if (httpResponse.getStatusCode() != 200) {
      logger.info("OIDC error message: {}", httpResponse.getContent());
      throw new C2SimRestAuthException(
          C2SimRestAuthException.ErrorType.ACCESS_TOKEN_RETRIEVAL_FAILED,
          "Token endpoint returned HTTP "
              + httpResponse.getStatusCode()
              + " - "
              + httpResponse.getContent());
    }

    return httpResponse;
  }

  // ---------------------------------------------------------
  // OpenID Discovery
  // ---------------------------------------------------------

  private static final String POSTFIX_DISCOVERY = "/.well-known/openid-configuration";

  // Auto discovery URI
  private URI resolveTokenEndpoint() throws C2SimRestAuthException {

    try {
      var discoveryEndpoint = config.ipdUri().toString();
      // bug fix, /.well-known/openid-configuration is added by lib
      if (discoveryEndpoint.endsWith(POSTFIX_DISCOVERY)) {
        discoveryEndpoint = discoveryEndpoint.replace(POSTFIX_DISCOVERY, "");
      }

      OIDCProviderMetadata metadata = OIDCProviderMetadata.resolve(new Issuer(discoveryEndpoint));

      URI endpoint = metadata.getTokenEndpointURI();

      if (endpoint == null) {
        throw new C2SimRestAuthException(
            C2SimRestAuthException.ErrorType.ENDPOINT_DISCOVERY_ACCESS_TOKEN_FAILED,
            "OIDC Token endpoint not found in OpenID "
                + "auto discovery configuration (/well-known/openid-configuration)");
      }

      return endpoint;

    } catch (Exception e) {
      throw new C2SimRestAuthException(
          C2SimRestAuthException.ErrorType.ENDPOINT_DISCOVERY_ACCESS_TOKEN_FAILED,
          "   Auto OIDC discovery failed for '" + config.ipdUri() + "'; reason: " + e.getMessage(),
          e);
    }
  }
}
