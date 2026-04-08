package org.c2sim.authorizer;

import static org.c2sim.authorization.impl.AuthorizationResult.Code.AUTHORIZED;
import static org.c2sim.authorization.impl.AuthorizationResult.Code.UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.c2sim.authorization.datatypes.ClaimValueList;
import org.c2sim.authorization.exceptions.AuthorisationException;
import org.c2sim.authorization.impl.C2SimAuthorizerImpl;
import org.c2sim.authorization.interfaces.C2SimClaims;
import org.c2sim.lox.schema.SecurityClassificationCodeType;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Auth module")
@Story("Claim classification")
class TestAuthorizeSecurityClassificationCode {

  @Test
  void testClaimSecurityClassificationCode1() throws AuthorisationException {
    C2SimClaims jwtClaims = mock(C2SimClaims.class);
    // Service is allowed to:
    var classifications =
        ClaimValueList.createWithTextNotation(
            C2SimClaims.SECURITY_CLASSIFICATION_CODE, "Unclassified");
    when(jwtClaims.getSecurityClassificationCode()).thenReturn(classifications);

    var auth = new C2SimAuthorizerImpl(jwtClaims);
    assertSame(
        AUTHORIZED,
        auth.authorizeSecurityClassificationCode(SecurityClassificationCodeType.UNCLASSIFIED).code);
    assertSame(
        UNAUTHORIZED,
        auth.authorizeSecurityClassificationCode(SecurityClassificationCodeType.TOP_SECRET).code);
  }

  @Test
  void testClaimSecurityClassificationCode2() throws AuthorisationException {
    C2SimClaims jwtClaims = mock(C2SimClaims.class);
    // Service is allowed to:
    var classifications =
        ClaimValueList.createWithTextNotation(
            C2SimClaims.SECURITY_CLASSIFICATION_CODE, "TopSecret;Unclassified");
    when(jwtClaims.getSecurityClassificationCode()).thenReturn(classifications);

    var auth = new C2SimAuthorizerImpl(jwtClaims);
    assertSame(
        AUTHORIZED,
        auth.authorizeSecurityClassificationCode(SecurityClassificationCodeType.UNCLASSIFIED).code);
    assertSame(
        AUTHORIZED,
        auth.authorizeSecurityClassificationCode(SecurityClassificationCodeType.TOP_SECRET).code);
  }
}
