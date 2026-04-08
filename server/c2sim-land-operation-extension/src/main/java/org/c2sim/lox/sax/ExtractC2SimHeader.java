package org.c2sim.lox.sax;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.c2sim.lox.schema.C2SIMHeaderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Only get C2SIM header from XML without parsing complete XML */
public final class ExtractC2SimHeader {

  private static final Logger logger = LoggerFactory.getLogger(ExtractC2SimHeader.class);
  private static final JAXBContext CONTEXT;

  private ExtractC2SimHeader() {
    throw new AssertionError("Only static functions");
  }

  static {
    JAXBContext context = null;
    try {
      context = JAXBContext.newInstance(C2SIMHeaderType.class);
    } catch (JAXBException e) {
      logger.error("JAXB context for C2SIMHeaderType failed: {}", e.getMessage());
    }
    CONTEXT = context;
  }

  public static C2SIMHeaderType extract(InputStream inputStream) {
    XMLInputFactory factory = XMLInputFactory.newFactory();
    // Disable external references for security
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
    factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
    // create factory
    XMLStreamReader reader = null;
    try {
      reader = factory.createXMLStreamReader(inputStream);
      Unmarshaller unmarshaller = CONTEXT.createUnmarshaller();
      while (reader.hasNext()) {
        if (reader.isStartElement() && reader.getLocalName().equals("C2SIMHeader")) {
          return unmarshaller.unmarshal(reader, C2SIMHeaderType.class).getValue();
        }
        reader.next();
      }
    } catch (XMLStreamException | JAXBException e) {
      logger.error("XMLStreamException while reading C2SIMHeaderType: {}", e.getMessage());
    }
    return null;
  }
}
