package org.c2sim.authorizer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.io.IOException;
import org.c2sim.authorization.datatypes.ClaimValueList;
import org.c2sim.authorization.exceptions.AuthorisationException;
import org.c2sim.authorization.impl.C2SimClaimsBuilder;
import org.c2sim.authorization.interfaces.C2SimClaims;
import org.c2sim.authorization.utils.ResourceHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Auth module")
@Story("Claim builder")
class TestC2SimClaimsBuilder {

  private static C2SimClaims claims;

  @BeforeAll
  static void setup() throws IOException, AuthorisationException {
    var testJwtToken =
        ResourceHelper.readUntilFirstSpace(
            ResourceHelper.readResourceAsString(
                TestC2SimClaimsBuilder.class, "/jwt-token-pep.txt"));
    // Public key: keycloak => Realm settings => keys => RS256
    claims =
        C2SimClaimsBuilder.createWithPublicKey(
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtunfZ9ENCJC/8FA6Z5I/1bNlfCbJpu3ySHtT8gDIIjL/SezNhXQUc609Khp/IBxYMBUX4am01rFdFe8B+zpah5Iy/6wNRiIF2u4XXXgNxbIJU2AE67GNipqVoJBYumyz6/Cd/8pdP51vClobSwrqgMFZ62mDXmVxSSvPych6ZoLyqNONS+O7oDJ0hr0Y52jMOrXNmCOOuybT/CGHj8xom4BOkJwmHqO4Qws2afng+UP6tVq69M6siAgFnKK3PAgySimO+2N4I9lpo6b2CHNmlk1qX7uNIbRtIs0Me/Dl7B7Ym65BoPQvNmjI8MRmrLXK6W8PoXAVFq30owxHrQ9tRwIDAQAB")
            .addAudience("c2sim")
            .disableValidation()
            .build(testJwtToken);
  }

  @Test
  void testClaimBuilderMessageType() throws AuthorisationException {
    var actual = claims.getMessageType();
    // C2SIMInitialization;DomainMessage;SystemAcknowledgement;SystemMessage
    var expected =
        ClaimValueList.createWithTextNotation(
            C2SimClaims.MESSAGE_TYPE,
            "DomainMessage;SystemMessage;C2SIMInitialization;SystemAcknowledgement");
    assertEquals(expected, actual, "Claims message type was not good parsed");
  }

  @Test
  void testClaimBuilderSecurityClassificationCode() throws AuthorisationException {
    var actual = claims.getSecurityClassificationCode();
    var expected =
        ClaimValueList.createWithTextNotation(
            C2SimClaims.COMMUNICATIVE_ACT_TYPE_CODE, "Unclassified");
    assertEquals(expected, actual, "Claims SecurityClassificationCode was not good parsed");
  }

  @Test
  void testClaimBuilderCommunicativeActTypeCode() throws AuthorisationException {
    var actual = claims.getCommunicativeActTypeCode();
    var expected =
        ClaimValueList.createWithTextNotation(
            C2SimClaims.COMMUNICATIVE_ACT_TYPE_CODE, "Accept;Agree;Confirm;Inform;Propose;Request");
    assertEquals(expected, actual, "Claims CommunicativeActTypeCode was not good parsed");
  }

  @Test
  void testClaimBuilderFromSendingSystem() {
    var actual = claims.getFromSendingSystem();
    ClaimValueList expected = new ClaimValueList();
    expected.add("LOX_NLD");
    assertEquals(expected, actual, "Claims FromSendingSystem was not good parsed");
  }

  @Test
  void testClaimBuilderReplyToSystem() {
    var actual = claims.getReplyToSystem();
    // ANY
    ClaimValueList expected = new ClaimValueList();
    assertEquals(expected, actual, "Claims ReplyToSystem ANY not properly implemented");
  }

  @Test
  void testClaimBuilderSystemMessageType() {
    var actual = claims.getSystemMessageType();
    // ANY
    ClaimValueList expected = new ClaimValueList();
    assertEquals(expected, actual, "Claims SystemMessage ANY not properly implemented");
  }
}
