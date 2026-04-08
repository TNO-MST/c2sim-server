package org.c2sim.lox;

import static org.c2sim.lox.helpers.ResourceHelper.readResourceAsString;
import static org.c2sim.lox.helpers.ResourceHelper.toText;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.io.IOException;
import java.io.InputStream;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.exceptions.ValidationException;
import org.c2sim.lox.helpers.MessageBodyTypeHelper;
import org.c2sim.lox.validation.LoxXsdValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM LOX module")
@Story("C2SIM initialization")
class C2simInitializationTest {

  @Disabled("Uses CWIX2025 lox file (not releasable)")
  @Test()
  void c2simInitializationTest() throws ValidationException, LoxException, IOException {

    String xmlResourcePath = "lox/xml/CWIX2025-6jun2025.xml";
    var xml = readResourceAsString(C2simInitializationTest.class, xmlResourcePath);
    try (InputStream inputStream =
        C2simInitializationTest.class.getClassLoader().getResourceAsStream(xmlResourcePath)) {
      // Test against reference XSD
      xml = toText(inputStream);
    } catch (final Exception e) {
      Assertions.fail("Failed to read XML test file");
    }
    var validation = LoxXsdValidator.doValidation(xml);
    Assertions.assertTrue(validation.isValid());
    var msg =
        MessageBodyTypeHelper.readMessageBody(
            C2simInitializationTest.class.getClassLoader().getResourceAsStream(xmlResourcePath));
    Assertions.assertNotNull(msg);
    var init = new C2simInitialization(msg.getC2SIMInitializationBody());
    Assertions.assertEquals(
        "NOR-VRFORCES", init.lookupOwnerSystemByUUID("18b714e2-c7bf-47e6-bf6d-1c11bba38e8f"));
    Assertions.assertEquals(
        "Unit 15H.MechBnHq", init.lookupEntityByUUID("af020185-ece2-4c0a-83e9-65b0c4d6349b"));
  }
}
