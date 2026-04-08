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
import org.c2sim.authorization.lox.enums.ELoxMessageType;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Auth module")
@Story("Claim message type")
class TestAuthorizeMessageType {

  @Test
  void testClaimMessageType() throws AuthorisationException {
    C2SimClaims jwtClaims = mock(C2SimClaims.class);
    // Service is allowed to:
    var msgTypes =
        ClaimValueList.createWithTextNotation(
            C2SimClaims.MESSAGE_TYPE, "SystemMessage;DomainMessage");
    when(jwtClaims.getMessageType()).thenReturn(msgTypes);

    var auth = new C2SimAuthorizerImpl(jwtClaims);
    assertSame(AUTHORIZED, auth.authorizeMessageType(ELoxMessageType.DOMAIN_MESSAGE).code);
    assertSame(UNAUTHORIZED, auth.authorizeMessageType(ELoxMessageType.OBJECT_INITIALIZATION).code);
  }

  @Test
  void testClaimMessageTypeAny() throws AuthorisationException {
    C2SimClaims jwtClaims = mock(C2SimClaims.class);
    // Service is allowed to:
    var msgTypes = ClaimValueList.createWithTextNotation(C2SimClaims.MESSAGE_TYPE, "ANY");
    when(jwtClaims.getMessageType()).thenReturn(msgTypes);

    var auth = new C2SimAuthorizerImpl(jwtClaims);
    assertSame(AUTHORIZED, auth.authorizeMessageType(ELoxMessageType.DOMAIN_MESSAGE).code);
    assertSame(AUTHORIZED, auth.authorizeMessageType(ELoxMessageType.OBJECT_INITIALIZATION).code);
  }
}
