package org.c2sim.lox.helpers;

import static org.c2sim.lox.helpers.XmlFactoryHelper.createC2SimHeader;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.io.IOException;
import java.io.InputStream;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.schema.MessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM LOX module")
@Story("C2SIM message serialization/deserialization")
class MessageTypeHelperTest {

  @Test()
  @Description("Deserialize XML C2SIM message")
  void serializeDeserializeStartScenarioText() throws LoxException {
    // Create StartScenario message
    var msg = XmlFactoryHelper.createStartScenario(createC2SimHeader("CHECK"));
    // Serialize message
    String xml = MessageTypeHelper.writeMessage(msg, false, true);
    // Deserialize message
    MessageType decoded = MessageTypeHelper.readMessage(xml);
    // Should single element
    Assertions.assertNotNull(decoded);
    Assertions.assertEquals("CHECK", decoded.getC2SIMHeader().getFromSendingSystem());
    // Compare complete xml
    String xmlClone = MessageTypeHelper.writeMessage(decoded, false, true);
    Assertions.assertEquals(xml, xmlClone);
  }

  @Description("Serialize to XML C2SIM message")
  @Test()
  void deserializeC2SimInit() throws IOException, LoxException {
    String resourcePath = "lox/xml/MessageWithC2SimInit.xml";
    try (InputStream inputStream =
        MessageTypeHelperTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
      MessageType decoded = MessageTypeHelper.readMessage(inputStream);
      Assertions.assertNotNull(decoded);
      Assertions.assertNotNull(decoded.getMessageBody());
      Assertions.assertNotNull(decoded.getMessageBody().getC2SIMInitializationBody());
      var c2simBody = decoded.getMessageBody().getC2SIMInitializationBody();
      var systems = c2simBody.getSystemEntityList();
      Assertions.assertNotNull(systems);
      Assertions.assertEquals(6, (long) systems.size());
    }
  }
}
