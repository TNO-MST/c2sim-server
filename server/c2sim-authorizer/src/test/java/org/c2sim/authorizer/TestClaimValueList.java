package org.c2sim.authorizer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.List;
import org.c2sim.authorization.datatypes.ClaimValueList;
import org.c2sim.authorization.exceptions.AuthorisationException;
import org.c2sim.authorization.interfaces.C2SimClaims;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Auth module")
@Story("Claim with lists")
class TestClaimValueList {

  // Test missing claim
  @Test
  void claimNotIsJwtToken() throws AuthorisationException {
    JwtClaims jwtClaims = mock(JwtClaims.class);
    when(jwtClaims.getClaimValue(C2SimClaims.TO_RECEIVING_SYSTEM)).thenReturn(null);
    // Claim value not in JWT token
    var claimValue = ClaimValueList.create(jwtClaims, C2SimClaims.TO_RECEIVING_SYSTEM);
    assertSame(ClaimValueList.CLAIM_NOT_IN_JWT_TOKEN, claimValue);
    assertFalse(claimValue.getClaimIsPresentInJwtToken());
    assertFalse(claimValue.getClaimIsAny());
    assertFalse(claimValue.getClaimIsValid());
    assertFalse(claimValue.getHasPermissionFor("SYSTEM_A"));
  }

  // Test claim value = "ANY"
  @Test
  void claimTextAny() throws AuthorisationException {
    JwtClaims jwtClaims = mock(JwtClaims.class);
    when(jwtClaims.getClaimValue(C2SimClaims.TO_RECEIVING_SYSTEM))
        .thenReturn(C2SimClaims.CLAIM_ANY);
    // Claim value is 'ANY'
    var claimValue = ClaimValueList.create(jwtClaims, C2SimClaims.TO_RECEIVING_SYSTEM);
    assertTrue(claimValue.getClaimIsPresentInJwtToken());
    assertTrue(claimValue.getClaimIsAny());
    assertTrue(claimValue.getClaimIsValid());
    assertTrue(claimValue.getHasPermissionFor("SYSTEM_A"));
  }

  // Test claim value is ["A", "B", "C"]
  @Test
  void claimAsArray() throws AuthorisationException {
    JwtClaims jwtClaims = mock(JwtClaims.class);
    List<?> claimActual = List.of("A", "B", "C");
    when(jwtClaims.getClaimValue(C2SimClaims.TO_RECEIVING_SYSTEM)).thenReturn(claimActual);
    // Claim value is 'A, B, C'
    var claimValue = ClaimValueList.create(jwtClaims, C2SimClaims.TO_RECEIVING_SYSTEM);
    assertEquals(3, claimValue.size());
    assertTrue(claimValue.getClaimIsPresentInJwtToken());
    assertFalse(claimValue.getClaimIsAny());

    assertTrue(claimValue.getClaimIsValid());

    assertTrue(claimValue.getHasPermissionFor("B"));
    assertFalse(claimValue.getHasPermissionFor("D"));
  }

  // Test claim value is "A;B;C"
  @Test
  void claimAsText() throws AuthorisationException {
    JwtClaims jwtClaims = mock(JwtClaims.class);
    String claimActual = "A;B;C";
    when(jwtClaims.getClaimValue(C2SimClaims.TO_RECEIVING_SYSTEM)).thenReturn(claimActual);
    // Claim value is 'A, B, C'
    var claimValue = ClaimValueList.create(jwtClaims, C2SimClaims.TO_RECEIVING_SYSTEM);
    assertEquals(3, claimValue.size());
    assertTrue(claimValue.getClaimIsPresentInJwtToken());
    assertFalse(claimValue.getClaimIsAny());

    assertTrue(claimValue.getClaimIsValid());

    assertTrue(claimValue.getHasPermissionFor("B"));
    assertFalse(claimValue.getHasPermissionFor("D"));
  }

  // Test claim value is ""
  @Test
  void claimAsEmptyText() throws AuthorisationException {
    JwtClaims jwtClaims = mock(JwtClaims.class);
    String claimActual = "";
    when(jwtClaims.getClaimValue(C2SimClaims.TO_RECEIVING_SYSTEM)).thenReturn(claimActual);
    // Claim value is ''
    var claimValue = ClaimValueList.create(jwtClaims, C2SimClaims.TO_RECEIVING_SYSTEM);
    assertEquals(0, claimValue.size());
    assertTrue(claimValue.getClaimIsPresentInJwtToken());
    assertFalse(claimValue.getClaimIsAny());
    assertTrue(claimValue.getClaimIsEmptyString());
    assertFalse(claimValue.getClaimIsValid());

    assertFalse(claimValue.getHasPermissionFor("B"));
    assertFalse(claimValue.getHasPermissionFor(""));
  }

  // Test claim value is Case Sensitive
  @Test
  void claimCaseSensitive() throws AuthorisationException {
    JwtClaims jwtClaims = mock(JwtClaims.class);
    String claimActual = "SyStEm_A";
    when(jwtClaims.getClaimValue(C2SimClaims.TO_RECEIVING_SYSTEM)).thenReturn(claimActual);
    // Claim value is ''
    var claimValue = ClaimValueList.create(jwtClaims, C2SimClaims.TO_RECEIVING_SYSTEM);
    assertEquals(1, claimValue.size());
    assertTrue(claimValue.getHasPermissionFor("SyStEm_A"));
    assertFalse(claimValue.getHasPermissionFor("SYSTEM_A"));
  }

  // Test claim value with spaces
  @Test
  void claimSpaces() throws AuthorisationException {
    JwtClaims jwtClaims = mock(JwtClaims.class);
    String claimActual = "SYSTEM_A   ;  SYSTEM B;SYSTEM C";
    when(jwtClaims.getClaimValue(C2SimClaims.TO_RECEIVING_SYSTEM)).thenReturn(claimActual);
    // Claim value is ''
    var claimValue = ClaimValueList.create(jwtClaims, C2SimClaims.TO_RECEIVING_SYSTEM);
    assertEquals(3, claimValue.size());
    assertTrue(claimValue.getHasPermissionFor("SYSTEM_A"));
    assertTrue(claimValue.getHasPermissionFor("SYSTEM B"));
    assertTrue(claimValue.getHasPermissionFor("SYSTEM C"));
  }
}
