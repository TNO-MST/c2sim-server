package org.c2sim.authorization.openid;

// Example of openapi discovery url (keycloak)
// https://<KEYCLOAK-HOST>/realms/<REALM-NAME>/.well-known/openid-configuration

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.c2sim.authorization.exceptions.AuthorisationException;

/**
 * Get the public key from Identity Provider
 *
 * @param jwks_uri The discovery url of the IDP server
 */
public record OidcProviderConfiguration(String jwks_uri) {
  /**
   * Fetch Identity Provider public key (needed for signature validation)
   *
   * @param openIdProvider The discovery URL of IDP
   * @return The IDP configuration
   * @throws AuthorisationException Not allowed to request the IDP configuration
   */
  public static OidcProviderConfiguration downloadOpenIdConfigurationFromKeycloak(
      URL openIdProvider) throws AuthorisationException {
    HttpURLConnection con = null;
    try {
      con = (HttpURLConnection) openIdProvider.openConnection();
      con.setRequestMethod("GET");
      con.setConnectTimeout(5000);
      con.setReadTimeout(5000);

      int status = con.getResponseCode();
      if (status != HttpURLConnection.HTTP_OK) {
        throw new AuthorisationException(
            "OpenID Provider returned HTTP " + status + " at " + openIdProvider);
      }

      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(con.getInputStream()))) {
        return new Gson().fromJson(reader, OidcProviderConfiguration.class);
      }
    } catch (Exception ex) {

      throw new AuthorisationException(
          "Failed to retrieve OpenID configuration from " + openIdProvider.toExternalForm(), ex);
    } finally {
      if (con != null) {
        con.disconnect();
      }
    }
  }

  /**
   * Returns the active JWKS IDP url
   *
   * @return The JWKS url of IDP
   */
  public String getJwksUri() {
    return jwks_uri;
  }
}
