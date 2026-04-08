package org.c2sim.lox.validation;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.c2sim.lox.exceptions.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM LOX module")
@Story("C2SIM message validation")
class LoxXsdValidatorCustomXsdTest {

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
  void validateC2SIMInitialization_extra_field_own_namespace() {

    String xsdResourcePath = "xsd/C2SIM_SMX_LOX_CWIX2025_WITH_MODIFICATION.xsd";
    String xmlResourcePath = "lox/C2SIMInitialization_extra_field.xml";

    String xml = null;
    try (InputStream inputStream =
        LoxXsdValidatorCustomXsdTest.class.getClassLoader().getResourceAsStream(xmlResourcePath)) {
      // Test against reference XSD
      xml = toText(inputStream);
    } catch (final Exception e) {
      Assertions.fail("Failed to read XML test file");
    }
    URL urlXsd = LoxXsdValidatorCustomXsdTest.class.getClassLoader().getResource(xsdResourcePath);

    try {
      var validation =
          LoxXsdValidator.doValidation(
              "custom_xsd_1",
              urlXsd,
              ns -> {
                System.out.println("Unknown namespace " + ns);
                return null;
              },
              new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

      Assertions.assertTrue(validation.isValid());
      Assertions.assertTrue(validation.getValidationsErrors().isEmpty());
      Assertions.assertTrue(validation.getValidationsWarnings().isEmpty());
      Assertions.assertTrue(validation.getValidationsIssues(true).isEmpty());

      // Test against reference XSD
      var validationOrg = LoxXsdValidator.doValidation(xml);
      Assertions.assertFalse(validationOrg.isValid());
      Assertions.assertEquals(1, (long) validationOrg.getValidationsErrors().size());
      Assertions.assertEquals(
          "cvc-elt.1.a: Cannot find the declaration of element 'c2sim:Message'.",
          validationOrg.getValidationsErrors().getFirst().getMessage());
    } catch (ValidationException ex) {
      Assertions.fail("Failed to create validator instance");
    }
  }

  @Test()
  void validateC2SIMInitialization_extra_field_same_namespace() {
    String xsdResourcePath = "xsd/C2SIM_SMX_LOX_CWIX2025_WITH_MODIFICATION_1_1.xsd";
    String xmlResourcePath = "lox/C2SIMInitialization_extra_field_1_1.xml";
    String xml = null;
    try (InputStream inputStream =
        LoxXsdValidatorCustomXsdTest.class.getClassLoader().getResourceAsStream(xmlResourcePath)) {
      // Test against reference XSD
      xml = toText(inputStream);
    } catch (final Exception e) {
      Assertions.fail("Failed to read XML test file");
    }
    URL urlXsd = LoxXsdValidatorCustomXsdTest.class.getClassLoader().getResource(xsdResourcePath);

    try {
      var validation =
          LoxXsdValidator.doValidation(
              "custom_xsd_2",
              urlXsd,
              ns -> {
                System.out.println("Unknown namespace " + ns);
                return null;
              },
              new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

      Assertions.assertTrue(validation.isValid());
      Assertions.assertTrue(validation.getValidationsErrors().isEmpty());
      Assertions.assertTrue(validation.getValidationsWarnings().isEmpty());
      Assertions.assertTrue(validation.getValidationsIssues(true).isEmpty());

      // Test against reference XSD
      var validationOrg = LoxXsdValidator.doValidation(xml);
      Assertions.assertFalse(validationOrg.isValid());
      Assertions.assertEquals(1, (long) validationOrg.getValidationsErrors().size());
      Assertions.assertEquals(
          "cvc-complex-type.2.4.d: Invalid content was found starting with element 'c2sim:CheckingXsdSchema'. No child element is expected at this point.",
          validationOrg.getValidationsErrors().getFirst().getMessage());

    } catch (ValidationException ex) {
      Assertions.fail("Failed to create validator instance");
    }
  }
}
