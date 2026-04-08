package org.c2sim.authorizer;

import static org.c2sim.authorization.impl.AuthorizationResult.Code.AUTHORIZED;
import static org.c2sim.authorization.impl.AuthorizationResult.Code.UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.io.IOException;
import java.io.StringReader;
import org.c2sim.authorization.datatypes.ClaimValueList;
import org.c2sim.authorization.exceptions.AuthorisationException;
import org.c2sim.authorization.impl.C2SimAuthorizerImpl;
import org.c2sim.authorization.impl.C2SimClaimsBuilder;
import org.c2sim.authorization.interfaces.C2SimClaims;
import org.c2sim.authorization.utils.ResourceHelper;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.exceptions.ValidationException;
import org.c2sim.lox.helpers.MessageTypeHelper;
import org.c2sim.lox.validation.LoxXsdValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Auth module")
@Story("Claim message body type")
class TestAuthorizeMessageTypeBody {

  private static C2SimClaims claimsSystemMessage;

  @BeforeAll
  static void setup() throws IOException, AuthorisationException {
    var testJwtToken =
        ResourceHelper.readUntilFirstSpace(
            ResourceHelper.readResourceAsString(
                TestAuthorizeMessageTypeBody.class, "/jwt-token-pep-system-message.txt"));

    // Public key: keycloak => Realm settings => keys => RS256
    claimsSystemMessage =
        C2SimClaimsBuilder.createWithPublicKey(
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtunfZ9ENCJC/8FA6Z5I/1bNlfCbJpu3ySHtT8gDIIjL/SezNhXQUc609Khp/IBxYMBUX4am01rFdFe8B+zpah5Iy/6wNRiIF2u4XXXgNxbIJU2AE67GNipqVoJBYumyz6/Cd/8pdP51vClobSwrqgMFZ62mDXmVxSSvPych6ZoLyqNONS+O7oDJ0hr0Y52jMOrXNmCOOuybT/CGHj8xom4BOkJwmHqO4Qws2afng+UP6tVq69M6siAgFnKK3PAgySimO+2N4I9lpo6b2CHNmlk1qX7uNIbRtIs0Me/Dl7B7Ym65BoPQvNmjI8MRmrLXK6W8PoXAVFq30owxHrQ9tRwIDAQAB")
            .addAudience("c2sim")
            .disableValidation()
            .build(testJwtToken);
  }

  @Test
  void testAuthorizeMessageTypeBodyDomainMessage()
      throws IOException, ValidationException, LoxException {
    var xml =
        ResourceHelper.readResourceAsString(
            TestAuthorizeMessageTypeBody.class, "/lox-xml-samples/position-report.xml");
    var validator = LoxXsdValidator.doValidation(xml);
    assertTrue(validator.isValid(), "XML test report is invalid (XSD validator)");
    var msg = MessageTypeHelper.readMessage(new StringReader(xml));
    assertNotNull(msg, "Failed to load XML sample data");

    var auth = new C2SimAuthorizerImpl(claimsSystemMessage);

    var result = auth.authorizeMessageTypeBody(msg, "SISO-STD-C2SIM", "1.0.1");
    assertSame(UNAUTHORIZED, result.code);
  }

  @Test
  void testAuthorizeMessageTypeBody()
      throws IOException, LoxException, ValidationException, AuthorisationException {
    var xml =
        ResourceHelper.readResourceAsString(
            TestAuthorizeMessageTypeBody.class, "/lox-xml-samples/position-report.xml");
    var validator = LoxXsdValidator.doValidation(xml);
    assertTrue(validator.isValid(), "XML test report is invalid (XSD validator)");
    var msg = MessageTypeHelper.readMessage(new StringReader(xml));
    assertNotNull(msg, "Failed to load XML sample data");

    // Mock claims
    C2SimClaims jwtClaims = mock(C2SimClaims.class);
    // Service is allowed to:
    var classifications =
        ClaimValueList.createWithTextNotation(
            C2SimClaims.SECURITY_CLASSIFICATION_CODE, "Unclassified");

    var act =
        ClaimValueList.createWithTextNotation(
            C2SimClaims.COMMUNICATIVE_ACT_TYPE_CODE, "INFORM;ACCEPT");

    var msgType = ClaimValueList.createWithTextNotation(C2SimClaims.MESSAGE_TYPE, "ANY");

    when(jwtClaims.getSecurityClassificationCode()).thenReturn(classifications);
    when(jwtClaims.getReplyToSystem()).thenReturn(ClaimValueList.CLAIM_IS_EMPTY_STRING);
    when(jwtClaims.getFromSendingSystem()).thenReturn(ClaimValueList.CLAIM_IS_EMPTY_STRING);
    when(jwtClaims.getMessageType()).thenReturn(msgType);
    when(jwtClaims.getCommunicativeActTypeCode()).thenReturn(act);
    when(jwtClaims.getToReceivingSystem()).thenReturn(ClaimValueList.CLAIM_IS_EMPTY_STRING);

    var auth = new C2SimAuthorizerImpl(jwtClaims);
    var result = auth.authorizeMessageTypeBody(msg, "SISO-STD-C2SIM", "1.0.1");
    assertSame(UNAUTHORIZED, result.code);
  }

  @Test
  void testAuthorizeMessageTypeBody2()
      throws IOException, LoxException, ValidationException, AuthorisationException {
    var xml =
        ResourceHelper.readResourceAsString(
            TestAuthorizeMessageTypeBody.class, "/lox-xml-samples/position-report2.xml");
    var validator = LoxXsdValidator.doValidation(xml);
    assertTrue(validator.isValid(), "XML test report is invalid (XSD validator)");
    var msg = MessageTypeHelper.readMessage(new StringReader(xml));
    assertNotNull(msg, "Failed to load XML sample data");

    // Mock claims
    C2SimClaims jwtClaims = mock(C2SimClaims.class);
    // Service is allowed to:

    var classifications =
        ClaimValueList.createWithTextNotation(C2SimClaims.SECURITY_CLASSIFICATION_CODE, "");

    var act =
        ClaimValueList.createWithTextNotation(
            C2SimClaims.COMMUNICATIVE_ACT_TYPE_CODE, "INFORM;ACCEPT");

    var msgType = ClaimValueList.createWithTextNotation(C2SimClaims.MESSAGE_TYPE, "SystemMessage");

    when(jwtClaims.getSecurityClassificationCode()).thenReturn(classifications);
    when(jwtClaims.getReplyToSystem()).thenReturn(ClaimValueList.CLAIM_IS_EMPTY_STRING);
    when(jwtClaims.getFromSendingSystem()).thenReturn(ClaimValueList.CLAIM_IS_EMPTY_STRING);
    when(jwtClaims.getMessageType()).thenReturn(msgType);
    when(jwtClaims.getCommunicativeActTypeCode()).thenReturn(act);
    when(jwtClaims.getToReceivingSystem()).thenReturn(ClaimValueList.CLAIM_IS_EMPTY_STRING);

    // <CommunicativeActTypeCode>Inform</CommunicativeActTypeCode>
    //                <ConversationID>30124b8c-6e89-4745-b2a8-75d4d07084b6</ConversationID>
    //                <FromSendingSystem>NLD-TNO</FromSendingSystem>
    //                <MessageID>1e0e82d4-d9c0-4515-912b-373942e21912</MessageID>
    //                <Protocol>SISO-STD-C2SIM</Protocol>

    var auth = new C2SimAuthorizerImpl(jwtClaims);
    var result = auth.authorizeMessageTypeBody(msg, "SISO-STD-C2SIM", "1.0.1");
    assertSame(UNAUTHORIZED, result.code);
  }

  @Test
  void testAuthorizeMessageTypeBody3()
      throws IOException, LoxException, ValidationException, AuthorisationException {
    var xml =
        ResourceHelper.readResourceAsString(
            TestAuthorizeMessageTypeBody.class, "/lox-xml-samples/position-report.xml");
    var validator = LoxXsdValidator.doValidation(xml);
    assertTrue(validator.isValid(), "XML test report is invalid (XSD validator)");
    var msg = MessageTypeHelper.readMessage(new StringReader(xml));
    assertNotNull(msg, "Failed to load XML sample data");

    // Mock claims
    C2SimClaims jwtClaims = mock(C2SimClaims.class);
    // Service is allowed to:
    var classifications =
        ClaimValueList.createWithTextNotation(C2SimClaims.SECURITY_CLASSIFICATION_CODE, "ANY");

    var act =
        ClaimValueList.createWithTextNotation(
            C2SimClaims.COMMUNICATIVE_ACT_TYPE_CODE, "Inform;Accept");

    var msgType = ClaimValueList.createWithTextNotation(C2SimClaims.MESSAGE_TYPE, "DomainMessage");

    when(jwtClaims.getSecurityClassificationCode()).thenReturn(classifications);
    when(jwtClaims.getReplyToSystem()).thenReturn(ClaimValueList.ANY);
    when(jwtClaims.getFromSendingSystem()).thenReturn(ClaimValueList.ANY);
    when(jwtClaims.getMessageType()).thenReturn(msgType);
    when(jwtClaims.getCommunicativeActTypeCode()).thenReturn(act);
    when(jwtClaims.getToReceivingSystem()).thenReturn(ClaimValueList.ANY);

    // <CommunicativeActTypeCode>Inform</CommunicativeActTypeCode>
    //                <ConversationID>30124b8c-6e89-4745-b2a8-75d4d07084b6</ConversationID>
    //                <FromSendingSystem>NLD-TNO</FromSendingSystem>
    //                <MessageID>1e0e82d4-d9c0-4515-912b-373942e21912</MessageID>
    //                <Protocol>SISO-STD-C2SIM</Protocol>

    var auth = new C2SimAuthorizerImpl(jwtClaims);
    var result = auth.authorizeMessageTypeBody(msg, "SISO-STD-C2SIM", "1.0.2");
    assertSame(AUTHORIZED, result.code);
  }
}
