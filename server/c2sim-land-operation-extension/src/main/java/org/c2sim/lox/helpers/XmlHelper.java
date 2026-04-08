package org.c2sim.lox.helpers;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Generic JAXB XML serialization utilities.
 *
 * <p>Unlike {@link MessageTypeHelper} and {@link MessageBodyTypeHelper} which are specific to C2SIM
 * schema types, this class works with any JAXB-annotated object by creating a new {@link
 * JAXBContext} per call from the object's runtime type.
 */
public final class XmlHelper {

  // Prevent instantiation
  private XmlHelper() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Serialize an object to XML and write to an OutputStream.
   *
   * @param object Object to serialize
   * @param output OutputStream to write XML to
   * @param includeHeader Whether to include the XML declaration
   * @param indent Whether to pretty-print (indent) the XML
   * @throws JAXBException if the object cannot be serialized
   */
  public static void toXmlStream(
      Object object, OutputStream output, boolean includeHeader, boolean indent)
      throws JAXBException {
    if (object == null) {
      throw new IllegalArgumentException("Object to serialize cannot be null");
    }
    if (output == null) {
      throw new IllegalArgumentException("OutputStream cannot be null");
    }

    Marshaller marshaller = createMarshaller(object, includeHeader, indent);

    try (Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
      marshaller.marshal(object, writer);
      writer.flush();
    } catch (Exception e) {
      throw new JAXBException("Failed to write XML to stream", e);
    }
  }

  /**
   * Serialize an object to an XML string.
   *
   * @param object Object to serialize
   * @param includeHeader Whether to include the XML declaration
   * @param indent Whether to pretty-print (indent) the XML
   * @return The XML as a String
   * @throws JAXBException if the object cannot be serialized
   */
  public static String toXmlString(Object object, boolean includeHeader, boolean indent)
      throws JAXBException {
    if (object == null) {
      throw new IllegalArgumentException("Object to serialize cannot be null");
    }

    Marshaller marshaller = createMarshaller(object, includeHeader, indent);

    try (StringWriter writer = new StringWriter()) {
      marshaller.marshal(object, writer);
      return writer.toString();
    } catch (Exception e) {
      throw new JAXBException("Failed to write XML to string", e);
    }
  }

  /** Internal helper to configure JAXB Marshaller. */
  private static Marshaller createMarshaller(Object object, boolean includeHeader, boolean indent)
      throws JAXBException {
    JAXBContext context = JAXBContext.newInstance(object.getClass());
    Marshaller marshaller = context.createMarshaller();

    marshaller.setProperty(Marshaller.JAXB_FRAGMENT, !includeHeader);
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, indent);

    return marshaller;
  }
}
