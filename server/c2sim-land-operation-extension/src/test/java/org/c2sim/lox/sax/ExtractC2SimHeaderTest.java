package org.c2sim.lox.sax;

import static org.c2sim.lox.helpers.ResourceHelper.readResourceAsString;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM LOX module")
@Story("Extract C2SIM header from C2SIM message")
class ExtractC2SimHeaderTest {
  @Test()
  @Description("Get C2SIM header from XML text")
  void c2simHeaderExtraction() throws IOException {

    String xml =
        readResourceAsString(ExtractC2SimHeaderTest.class, "/lox/xml/c2simheader-auth.xml");
    Assertions.assertNotNull(xml);
    InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    var header = ExtractC2SimHeader.extract(inputStream);
    Assertions.assertNotNull(header);
    Assertions.assertEquals("1e0e82d4-d9c0-4515-912b-373942e21912", header.getMessageID());
    Assertions.assertNotNull(header.getSendingTime());
    Assertions.assertEquals("2025-04-28T20:20:35Z", header.getSendingTime().getIsoDateTime());
  }

  @Test()
  @Description("No C2SIM header found")
  void noC2simHeaderExtraction() throws IOException {
    String xml =
        readResourceAsString(ExtractC2SimHeaderTest.class, "/lox/xml/MessageWithOrder.xml");
    Assertions.assertNotNull(xml);
    InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    var header = ExtractC2SimHeader.extract(inputStream);
    Assertions.assertNull(header);
  }
}
