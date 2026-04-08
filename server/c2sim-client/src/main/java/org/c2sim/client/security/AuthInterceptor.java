package org.c2sim.client.security;

import java.io.IOException;
import java.util.Objects;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OkHttp {@link Interceptor} that attaches a Bearer token to every outgoing request.
 *
 * <p>The token is fetched lazily from the configured {@link OidcTokenProvider} on each request,
 * allowing the provider to handle expiry and refresh transparently.
 */
public final class AuthInterceptor implements Interceptor {

  private final OidcTokenProvider tokenProvider;

  /**
   * Creates an interceptor backed by the given token provider.
   *
   * @param provider the OIDC token provider; must not be {@code null}
   * @throws NullPointerException if {@code provider} is {@code null}
   */
  public AuthInterceptor(OidcTokenProvider provider) {
    Objects.requireNonNull(provider, "provider must not be null");
    this.tokenProvider = provider;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Adds an {@code Authorization: Bearer <token>} header to the request before forwarding it
   * through the chain.
   */
  @Override
  public Response intercept(Chain chain) throws IOException {
    Objects.requireNonNull(chain, "intercept chain must not be null");
    String token = tokenProvider.getAccessToken();
    Request request =
        chain.request().newBuilder().header("Authorization", "Bearer " + token).build();
    return chain.proceed(request);
  }
}
