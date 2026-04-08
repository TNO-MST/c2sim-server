package org.c2sim.lox;

import static org.c2sim.lox.helpers.ResourceHelper.readResourceAsString;
import static org.junit.jupiter.api.Assertions.*;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.io.IOException;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.exceptions.ValidationException;
import org.c2sim.lox.helpers.MessageTypeHelper;
import org.c2sim.lox.validation.LoxXsdValidator;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM LOX module")
@Story("C2SIM auth header information")
class AuthorizationHeaderInC2SimHeaderTest {
  /* The AuthorizationHeader in C2SIM header was added in 2025, test if supported */
  @Test
  @Description("Authorization header in C2SIM header")
  void testAuthorizationHeaderInC2SimHeader()
      throws IOException, ValidationException, LoxException {
    String xml =
        readResourceAsString(
            AuthorizationHeaderInC2SimHeaderTest.class, "/lox/xml/c2simheader-auth.xml");
    var validator = LoxXsdValidator.doValidation(xml);
    assertTrue(validator.isValid(), "XML test report is invalid (XSD validator)");
    var msg = MessageTypeHelper.readMessage(xml);
    assertNotNull(msg, "Failed to load XML sample data");
    assertNotNull(msg.getC2SIMHeader().getAuthorizationHeader());
    assertEquals(
        "{{token}}", msg.getC2SIMHeader().getAuthorizationHeader().getAuthorizationCredentials());
  }
}
