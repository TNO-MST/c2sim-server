package org.c2sim.authorizer;

import static org.c2sim.authorization.impl.AuthorizationResult.Code.AUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.io.IOException;
import org.c2sim.authorization.exceptions.AuthorisationException;
import org.c2sim.authorization.impl.C2SimAuthorizerImpl;
import org.c2sim.authorization.impl.C2SimClaimsBuilder;
import org.c2sim.authorization.interfaces.C2SimClaims;
import org.c2sim.authorization.utils.ResourceHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Auth module")
@Story("Claim reply to")
class TestAuthorizeReplyToSystem {

  private static C2SimClaims claimsAny;

  @BeforeAll
  static void setup() throws IOException, AuthorisationException {
    var testJwtToken =
        ResourceHelper.readUntilFirstSpace(
            ResourceHelper.readResourceAsString(
                TestAuthorizeReplyToSystem.class, "/jwt-token-pep.txt"));
    // Public key: keycloak => Realm settings => keys => RS256
    claimsAny =
        C2SimClaimsBuilder.createWithPublicKey(
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtunfZ9ENCJC/8FA6Z5I/1bNlfCbJpu3ySHtT8gDIIjL/SezNhXQUc609Khp/IBxYMBUX4am01rFdFe8B+zpah5Iy/6wNRiIF2u4XXXgNxbIJU2AE67GNipqVoJBYumyz6/Cd/8pdP51vClobSwrqgMFZ62mDXmVxSSvPych6ZoLyqNONS+O7oDJ0hr0Y52jMOrXNmCOOuybT/CGHj8xom4BOkJwmHqO4Qws2afng+UP6tVq69M6siAgFnKK3PAgySimO+2N4I9lpo6b2CHNmlk1qX7uNIbRtIs0Me/Dl7B7Ym65BoPQvNmjI8MRmrLXK6W8PoXAVFq30owxHrQ9tRwIDAQAB")
            .addAudience("c2sim")
            .disableValidation()
            .build(testJwtToken);
  }

  @Test
  void testClaimReplyToSystemAny() {
    var auth = new C2SimAuthorizerImpl(claimsAny);
    assertSame(AUTHORIZED, auth.authorizeReplyToSystem("Random String").code);
    assertSame(AUTHORIZED, auth.authorizeReplyToSystem("").code);
    assertSame(AUTHORIZED, auth.authorizeReplyToSystem("INVALID_COMMAND").code);
  }
}
