package org.c2sim.server;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.stream.Collectors;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.helpers.MessageTypeHelper;
import org.c2sim.lox.helpers.XmlFactoryHelper;
import org.c2sim.server.api.models.RequestJoinSession;
import org.c2sim.server.exceptions.C2SimException;
import org.c2sim.server.utils.StringHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Server module")
@Story("XML message validation")
class XsdMessageValidationTest extends BaseTest {
  @Test
  void xsdValidationByServer() throws LoxException {

    var submitInitializationXml = XmlFactoryHelper.createSubmitInitialization(createHeader());
    submitInitializationXml.getC2SIMHeader().setMessageID("FORCE VALIDATION ERROR; NO UUID");
    var xml = MessageTypeHelper.writeMessage(submitInitializationXml, true, true);
    logger.info("Create C2SIM submitInitialization with error in C2SIM header MessageID field");

    var sharedSession = getSession();
    sharedSession.joinSharedSession(
        CLIENT_ID_A,
        "joining A",
        new RequestJoinSession(CLIENT_ID_A_SYSTEM_NAME, CLIENT_ID_A_SYSTEM_NAME));

    C2SimException invalidXml =
        Assertions.assertThrowsExactly(
            C2SimException.class,
            () ->
                sharedSession.publishC2SimDoc(
                    CLIENT_ID_A, "A:InvalidXmlFile", StringHelper.toStream(xml)));
    Assertions.assertEquals(
        C2SimException.ErrorCode.XSD_VALIDATION_ERROR.toString(), invalidXml.getError().getCode());
    logger.info(
        "JUNIT: Expected ERROR[{}]: '{}'",
        invalidXml.getError().getCode(),
        invalidXml.getMessage());
    String xsdValidationErrors =
        invalidXml.getError().getDetails().entrySet().stream()
            .map(entry -> "- " + entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("\n"));
    logger.info("XSD errors: \n{}", xsdValidationErrors);
  }
}
