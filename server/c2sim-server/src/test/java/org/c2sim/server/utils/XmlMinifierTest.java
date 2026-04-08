package org.c2sim.server.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.c2sim.lox.exceptions.ValidationException;
import org.c2sim.lox.helpers.XmlFactoryHelper;
import org.c2sim.lox.validation.LoxXsdValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Server module")
@Story("One line XML")
class XmlMinifierTest {

  @Test
  void minifyXml() throws ValidationException {
    var xml =
        XmlFactoryHelper.createC2SIMInitialization(
            XmlFactoryHelper.createC2SimHeader("xxxx"), "yyyyy", true);
    var validatorBefore = LoxXsdValidator.doValidation(xml);
    Assertions.assertTrue(validatorBefore.isValid());
    var compactXml = XmlMinifier.minifyXml(xml);
    var validatorAfter = LoxXsdValidator.doValidation(compactXml);
    Assertions.assertTrue(validatorAfter.isValid());
    assertFalse(
        compactXml.contains("\n"),
        "The compacted XML should not contain enters (this is used as seperator in stream)");
  }
}
