package org.c2sim.authorization.openid;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.c2sim.authorization.exceptions.AuthorisationException;

// Get JWT token with grant flow client_credentials
public class OAuthClient {

  private OAuthClient() {
    throw new AssertionError("Only static functions");
  }

  public static String downloadAccessToken(
      String tokenUrl, String clientId, String clientSecret, String scope)
      throws AuthorisationException {
    try {
      String payload =
          "client_id="
              + clientId
              + "&scope="
              + scope
              + "&client_secret="
              + clientSecret
              + "&grant_type=client_credentials";

      URL url = URI.create(tokenUrl).toURL();
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

      try (OutputStream os = conn.getOutputStream()) {
        os.write(payload.getBytes(StandardCharsets.UTF_8));
      }

      if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new AuthorisationException("Failed to get token: HTTP " + conn.getResponseCode());
      }

      try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
        JsonObject json = JsonParser.parseReader(in).getAsJsonObject();
        return json.get("access_token").getAsString();
      }
    } catch (Exception e) {
      throw new AuthorisationException(e.getMessage(), e);
    }
  }
}
