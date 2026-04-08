package org.c2sim.authorization.datatypes;

import java.util.Set;
import org.c2sim.authorization.exceptions.AuthorisationException;
import org.c2sim.lox.schema.CommunicativeActTypeCodeType;
import org.jose4j.jwt.JwtClaims;

public class ClaimValueListCommunicative
    extends ClaimValueListEnumSet<CommunicativeActTypeCodeType> {
  public ClaimValueListCommunicative(Set<CommunicativeActTypeCodeType> claimValueList) {
    super(CommunicativeActTypeCodeType.class, claimValueList);
  }

  public static ClaimValueListCommunicative create(JwtClaims claims, String claimName)
      throws AuthorisationException {
    var enumSet =
        ClaimValueListEnumSet.create(CommunicativeActTypeCodeType.class, claims, claimName);
    return new ClaimValueListCommunicative(enumSet);
  }
}
