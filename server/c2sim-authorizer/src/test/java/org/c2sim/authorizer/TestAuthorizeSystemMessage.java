package org.c2sim.authorizer;

import org.c2sim.authorization.datatypes.ClaimValueList;
import org.c2sim.authorization.exceptions.AuthorisationException;
import org.c2sim.authorization.impl.C2SimAuthorizerImpl;
import org.c2sim.authorization.interfaces.C2SimClaims;
import org.c2sim.authorization.lox.enums.ELoxMessageType;
import org.c2sim.authorization.lox.enums.ELoxSystemMessageType;
import org.c2sim.lox.C2SimMsgKind;
import org.junit.jupiter.api.Test;

import static org.c2sim.authorization.impl.AuthorizationResult.Code.AUTHORIZED;
import static org.c2sim.authorization.impl.AuthorizationResult.Code.UNAUTHORIZED;
import static org.c2sim.lox.C2SimMsgKindGroups.getSimanMsgGroupAsText;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestAuthorizeSystemMessage {

    @Test
    void testClaimSystemMessageNonSystem() throws AuthorisationException {
        C2SimClaims jwtClaims = mock(C2SimClaims.class);
        // Service is allowed to:
        var msgTypes =
                ClaimValueList.createWithTextNotation(
                        C2SimClaims.SYSTEM_MESSAGE_TYPE,
                        ELoxSystemMessageType.QUERY.getText());
        when(jwtClaims.getSystemMessageType()).thenReturn(msgTypes);

        var auth = new C2SimAuthorizerImpl(jwtClaims);
        assertSame(AUTHORIZED, auth.authorizeSystemMessageType(C2SimMsgKind.REPORT).code);
        assertSame(UNAUTHORIZED, auth.authorizeSystemMessageType(C2SimMsgKind.CHECKPOINT_RESTORE).code);

    }

    @Test
    void testClaimSystemMessageQuery() throws AuthorisationException {
        C2SimClaims jwtClaims = mock(C2SimClaims.class);
        // Service is allowed to:
        var msgTypes =
                ClaimValueList.createWithTextNotation(
                        C2SimClaims.SYSTEM_MESSAGE_TYPE, "Query");
        when(jwtClaims.getSystemMessageType()).thenReturn(msgTypes);

        var auth = new C2SimAuthorizerImpl(jwtClaims);
        assertSame(UNAUTHORIZED, auth.authorizeSystemMessageType(C2SimMsgKind.START_RECORDING).code);

    }

    @Test
    void testClaimSystemMessageAny() throws AuthorisationException {
        C2SimClaims jwtClaims = mock(C2SimClaims.class);
        // Service is allowed to:
        var msgTypes =
                ClaimValueList.createWithTextNotation(
                        C2SimClaims.SYSTEM_MESSAGE_TYPE, "ANY");
        when(jwtClaims.getSystemMessageType()).thenReturn(msgTypes);

        var auth = new C2SimAuthorizerImpl(jwtClaims);
        assertSame(AUTHORIZED, auth.authorizeSystemMessageType(C2SimMsgKind.START_RECORDING).code);
        assertSame(AUTHORIZED, auth.authorizeSystemMessageType(C2SimMsgKind.START_SCENARIO).code);

    }

    @Test
    void testClaimSystemMessageMultiple() throws AuthorisationException {
        C2SimClaims jwtClaims = mock(C2SimClaims.class);
        // Service is allowed to:
        var msgTypes =
                ClaimValueList.createWithTextNotation(
                        C2SimClaims.SYSTEM_MESSAGE_TYPE,
                        ELoxSystemMessageType.QUERY.getText() +
                        C2SimClaims.CLAIM_LIST_SEPARATOR +
                        ELoxSystemMessageType.SIMAN.getText());
        when(jwtClaims.getSystemMessageType()).thenReturn(msgTypes);
        var auth = new C2SimAuthorizerImpl(jwtClaims);
        assertSame(AUTHORIZED, auth.authorizeSystemMessageType(C2SimMsgKind.START_RECORDING).code);
        assertSame(AUTHORIZED, auth.authorizeSystemMessageType(C2SimMsgKind.START_SCENARIO).code);

    }

}
