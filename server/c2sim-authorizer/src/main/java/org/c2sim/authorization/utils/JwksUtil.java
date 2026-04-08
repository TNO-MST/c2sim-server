package org.c2sim.authorization.utils;

import java.net.URL;
import java.security.Key;
import java.security.PublicKey;
import java.util.List;
import org.c2sim.authorization.exceptions.AuthorisationException;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwk.JsonWebKey;

/** used for internal testing, not needed for general workflow */
public class JwksUtil {

  private JwksUtil() {
    throw new AssertionError("Only static functions");
  }

  // https://keycloak.example.com/realms/myrealm/protocol/openid-connect/certs
  // This method takes the first key its find!

  /**
   * Get public key from IDP
   *
   * @param url The IDP url
   * @return The public key
   * @throws AuthorisationException Not allowed to access public key
   */
  public static PublicKey getPublicKeyFromOpenIdProvider(URL url) throws AuthorisationException {
    try {
      // Load JWKS
      HttpsJwks jwks = new HttpsJwks(url.toExternalForm());

      // Fetch keys (will download from remote)
      List<JsonWebKey> keys = jwks.getJsonWebKeys();

      // Get first key (or select by key ID if needed)
      JsonWebKey jwk = keys.get(0);

      // Get the Java PublicKey object
      Key key = jwk.getKey(); // returns java.security.Key
      return (PublicKey) key;

    } catch (Exception e) {
      throw new AuthorisationException("Failed to get public key from openid provider");
    }
  }

  /**
   * Get public key of IDP in PEM format
   *
   * @param url The IDP url
   * @return Public key in PEM format
   * @throws AuthorisationException Not allowed to get public key
   */
  public static String getPublicKeyFromOpenIdProviderAsPem(URL url) throws AuthorisationException {
    PublicKey key = getPublicKeyFromOpenIdProvider(url);
    return PemUtil.convertToPem(key);
  }
}
