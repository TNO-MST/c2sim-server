package org.c2sim.authorization.utils;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/** Helper class for handling public keys */
public class PemUtil {

  private PemUtil() {
    throw new AssertionError("Only static functions");
  }

  /**
   * @param pem The PEM value
   * @return The public key
   * @throws InvalidKeySpecException Invalid PEM format
   * @throws NoSuchAlgorithmException Algorithm not supported
   */
  public static PublicKey getPublicKeyFromPem(String pem)
      throws InvalidKeySpecException, NoSuchAlgorithmException {
    // Remove PEM header/footer and line breaks
    String publicKeyPEM =
        pem.replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");

    // Decode Base64
    byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

    // Create PublicKey
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // or "EC" for EC keys
    return keyFactory.generatePublic(keySpec);
  }

  /**
   * Convert public key to PEM text format
   *
   * @param publicKey The public key
   * @return Pem in text format
   */
  public static String convertToPem(PublicKey publicKey) {
    String encoded =
        Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(publicKey.getEncoded());
    return "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----";
  }
}
