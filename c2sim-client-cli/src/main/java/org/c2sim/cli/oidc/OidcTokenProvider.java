package org.c2sim.cli.oidc;

import com.google.gson.Gson;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Fetches a bearer token from an OIDC-compliant token endpoint
 * using the OAuth 2.0 client_credentials grant.
 *
 * Expected endpoint: {@code <idpUrl>/token}
 */
public class OidcTokenProvider {

    private static final Gson GSON = new Gson();

    private final String idpUrl;
    private final String clientId;
    private final String clientSecret;
    private final OkHttpClient http = new OkHttpClient();

    public OidcTokenProvider(String idpUrl, String clientId, String clientSecret) {
        // Normalise trailing slash
        this.idpUrl       = idpUrl.endsWith("/") ? idpUrl.substring(0, idpUrl.length() - 1) : idpUrl;
        this.clientId     = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * Performs the token request and returns the {@code access_token} value.
     *
     * @throws IOException if the request fails or the response contains no token
     */
    public String fetchToken() throws IOException {
        String tokenUrl = idpUrl + "/token";

        var body = new FormBody.Builder()
                .add("grant_type",    "client_credentials")
                .add("client_id",     clientId)
                .add("client_secret", clientSecret)
                .build();

        var request = new Request.Builder()
                .url(tokenUrl)
                .post(body)
                .build();

        try (Response response = http.newCall(request).execute()) {
            String json = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code() + " from IDP: " + json);
            }
            TokenResponse token = GSON.fromJson(json, TokenResponse.class);
            if (token == null || token.access_token == null || token.access_token.isBlank()) {
                throw new IOException("No access_token in IDP response");
            }
            return token.access_token;
        }
    }

    @SuppressWarnings("all")
    private static class TokenResponse {
        String access_token;
        String token_type;
        int    expires_in;
        String scope;
    }
}
