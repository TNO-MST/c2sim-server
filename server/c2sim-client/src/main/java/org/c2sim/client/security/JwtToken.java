package org.c2sim.client.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Date;
import java.util.Objects;

/**
 * Utility for creating HS256-signed JSON Web Tokens (JWTs) for C2SIM system authentication.
 *
 * <p>The HMAC secret is read from the {@code C2SIM_SHARED_SECRET} environment variable. If the
 * variable is not set, the literal string {@code "C2SIM_SHARED_SECRET NOT SET"} is used as a
 * fallback (resulting in an invalid token for any properly configured server).
 */
public final class JwtToken {
  private static final String SECRET =
      Objects.requireNonNullElse(
          System.getenv("C2SIM_SHARED_SECRET"), "C2SIM_SHARED_SECRET NOT SET");

  // Prevent instantiation
  private JwtToken() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Creates a signed HS256 JWT for the given system name.
   *
   * <p>The token is valid for one hour and carries a {@code role=admin} claim and an {@code
   * iss=c2sim} issuer claim.
   *
   * @param systemName the subject ({@code sub}) claim value identifying the C2SIM system
   * @return the serialized JWT string, or {@code "ERROR"} if signing fails
   */
  public static String createJwtToken(String systemName) {
    try {
      // Prepare JWT with claims set
      JWTClaimsSet claims =
          new JWTClaimsSet.Builder()
              .subject(systemName)
              .issuer("c2sim")
              .expirationTime(new Date(System.currentTimeMillis() + 3600_000)) // 1 hour
              .claim("role", "admin")
              .build();

      // Create the header and sign with HS256
      JWSSigner signer = null;

      signer = new MACSigner(SECRET);

      SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);

      signedJWT.sign(signer);

      // Serialize token
      return signedJWT.serialize();

    } catch (Exception e) {
      return "ERROR";
    }
  }
}
