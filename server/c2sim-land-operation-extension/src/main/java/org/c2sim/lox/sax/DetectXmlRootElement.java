package org.c2sim.lox.sax;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/** For C2SIM the root should always be Message But MessageBody is also valid according XSD */
public class DetectXmlRootElement {

  private DetectXmlRootElement() {}

  /**
   * Get Root element name
   *
   * @param xmlPath The XML file
   * @return The root node (without namespace)
   */
  public static String getRootElementName(Path xmlPath) {
    try {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
      factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);

      try (InputStream is = Files.newInputStream(xmlPath)) {
        XMLStreamReader reader = factory.createXMLStreamReader(is);

        while (reader.hasNext()) {
          int event = reader.next();
          if (event == XMLStreamConstants.START_ELEMENT) {
            return reader.getLocalName(); // root element name
          }
        }
      }
    } catch (IOException | XMLStreamException e) {
      return null;
    }
    return null;
  }
}
