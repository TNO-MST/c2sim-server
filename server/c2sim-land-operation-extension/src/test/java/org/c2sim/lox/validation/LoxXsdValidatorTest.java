package org.c2sim.lox.validation;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM LOX module")
@Story("C2SIM message validator")
class LoxXsdValidatorTest {

  private static String toText(InputStream is) throws IOException {
    StringBuilder sb = new StringBuilder();
    String line;

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n"); // Append line and a newline character
      }
    }

    return sb.toString();
  }

  @Test()
  void validateLoxMain() {
    String resourcePath = "lox/C2SIMInitialization_small.xml";
    try (InputStream inputStream =
        LoxXsdValidatorTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
      var xml = toText(inputStream);

      var validation = LoxXsdValidator.doValidation(xml);
      Assertions.assertTrue(validation.isValid());
      Assertions.assertTrue(validation.getValidationsErrors().isEmpty());
      Assertions.assertTrue(validation.getValidationsWarnings().isEmpty());
      Assertions.assertTrue(validation.getValidationsIssues(true).isEmpty());
    } catch (final Exception e) {
      Assertions.fail();
    }
  }

  @SuppressWarnings("java:S125") // Comment out code samples
  @Test()
  void validateLox() {
    String resourcePath = "lox/C2SIMInitialization_error01.xml";
    try (InputStream inputStream =
        LoxXsdValidatorTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
      var xml = toText(inputStream);
      var validation = LoxXsdValidator.doValidation(xml);
      var isValid = validation.isValid();
      var errors = validation.getValidationsErrors();
      var warnings = validation.getValidationsWarnings();
      // Optional use getValidationsFatalErrors and getValidationsIssues
      Assertions.assertFalse(isValid);
      Assertions.assertEquals(2, errors.size());

      //            Assertions.assertEquals("cvc-pattern-valid: Value 'NO_GUID' is not facet-valid
      // with respect to pattern
      // '[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}' for
      // type 'ActorReferenceType'.", errors.getFirst().getMessage());
      Assertions.assertEquals(0, warnings.size());

    } catch (final Exception e) {
      Assertions.fail();
    }
  }

  @Test()
  void validateC2SIMInitialization() {
    String resourcePath = "lox/xml/MessageWithC2SimInit.xml";
    try (InputStream inputStream =
        LoxXsdValidatorTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
      var xml = toText(inputStream);

      var validation = LoxXsdValidator.doValidation(xml);
      Assertions.assertTrue(validation.isValid());
      Assertions.assertTrue(validation.getValidationsErrors().isEmpty());
      Assertions.assertTrue(validation.getValidationsWarnings().isEmpty());
      Assertions.assertTrue(validation.getValidationsIssues(true).isEmpty());
    } catch (final Exception e) {
      Assertions.fail();
    }
  }
}
