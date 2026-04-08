package org.c2sim.lox.helpers;

import jakarta.xml.bind.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.xml.transform.stream.StreamSource;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.schema.MessageType;
import org.c2sim.lox.schema.ObjectFactory;

/**
 * Utility methods for marshaling and unmarshaling top-level {@link MessageType} objects to and from
 * XML.
 *
 * <p>A shared {@link JAXBContext} is created lazily on the first call and reused for all subsequent
 * operations. Individual {@link Marshaller} / {@link Unmarshaller} instances are created per call
 * because they are not thread-safe.
 *
 * <p>Most {@code writeMessage} overloads accept an {@code omitXmlHeaderDeclarationHeader} flag that
 * controls whether the {@code <?xml ... ?>} processing instruction is included in the output.
 */
public final class MessageTypeHelper {

  // Prevent instantiation
  private MessageTypeHelper() {
    throw new AssertionError("Only static functions");
  }

  // static factory for creating objects
  private static final ObjectFactory objectFactory = new ObjectFactory();

  // static context for creating JAXB marshallers
  private static JAXBContext jaxbContext;

  private static synchronized Unmarshaller createUnmarshaller() throws JAXBException {
    if (jaxbContext == null) {
      jaxbContext = JAXBContext.newInstance(MessageType.class);
    }

    return jaxbContext.createUnmarshaller();
  }

  private static synchronized Marshaller createMarshaller(boolean omitHeader, boolean formatted)
      throws JAXBException {
    if (jaxbContext == null) {
      jaxbContext = JAXBContext.newInstance(MessageType.class);
    }

    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);
    jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, omitHeader);
    return jaxbMarshaller;
  }

  /**
   * Unmarshals a {@link MessageType} from the given XML string.
   *
   * @param xml the XML document as a string
   * @return the deserialized {@link MessageType}
   * @throws LoxException if JAXB unmarshaling fails
   */
  public static MessageType readMessage(String xml) throws LoxException {
    Objects.requireNonNull(xml, "MessageTypeHelper::readMessage -> xml is null");
    return readMessage(new ByteArrayInputStream(xml.getBytes()));
  }

  /**
   * Unmarshals a {@link MessageType} from the given input stream.
   *
   * @param input the XML input stream
   * @return the deserialized {@link MessageType}
   * @throws LoxException if JAXB unmarshaling fails
   */
  public static MessageType readMessage(InputStream input) throws LoxException {
    try {
      JAXBElement<MessageType> root =
          createUnmarshaller().unmarshal(new StreamSource(input), MessageType.class);
      return root.getValue();
    } catch (JAXBException ex) {
      throw new LoxException(ex);
    }
  }

  /**
   * Unmarshals a {@link MessageType} from the given URL.
   *
   * @param input the URL pointing to an XML document
   * @return the deserialized {@link MessageType}
   * @throws LoxException if the URL cannot be opened or JAXB unmarshaling fails
   */
  public static MessageType readMessage(URL input) throws LoxException {
    try {
      try (InputStream stream = input.openStream()) {
        return MessageTypeHelper.readMessage(stream);
      }
    } catch (IOException ex) {
      throw new LoxException(ex);
    }
  }

  private static InputStream getInputStream(Reader reader) throws IOException {
    try (reader) {
      char[] charBuffer = new char[8 * 1024];
      StringBuilder builder = new StringBuilder();
      int numCharsRead;
      while ((numCharsRead = reader.read(charBuffer, 0, charBuffer.length)) != -1) {
        builder.append(charBuffer, 0, numCharsRead);
      }

      return new ByteArrayInputStream(builder.toString().getBytes(StandardCharsets.UTF_8));
    }
  }

  /**
   * Unmarshals a {@link MessageType} from the given character reader.
   *
   * @param input the reader providing XML content
   * @return the deserialized {@link MessageType}
   * @throws LoxException if reading or JAXB unmarshaling fails
   */
  public static MessageType readMessage(Reader input) throws LoxException {
    try {
      return readMessage(getInputStream(input));
    } catch (IOException ex) {
      throw new LoxException(ex);
    }
  }

  /**
   * Marshals {@code msg} as an XML string, returning a fallback value instead of throwing when
   * marshaling fails.
   *
   * @param msg the message to serialize
   * @param omitXmlHeaderDeclarationHeader {@code true} to omit the {@code <?xml ...?>} declaration
   * @param formatted {@code true} to enable pretty-printing
   * @param valueWhenFailedEncoding the string to return if marshaling fails
   * @return the XML string, or {@code valueWhenFailedEncoding} on error
   */
  // When not interested in errors.....
  public static String writeMessage(
      MessageType msg,
      boolean omitXmlHeaderDeclarationHeader,
      boolean formatted,
      String valueWhenFailedEncoding) {
    try {
      return writeMessage(msg, omitXmlHeaderDeclarationHeader, formatted);
    } catch (LoxException ex) {
      return valueWhenFailedEncoding;
    }
  }

  /**
   * Marshals {@code msg} as an XML string.
   *
   * @param msg the message to serialize
   * @param omitXmlHeaderDeclarationHeader {@code true} to omit the {@code <?xml ...?>} declaration
   * @param formatted {@code true} to enable pretty-printing
   * @return the XML representation
   * @throws LoxException if marshaling fails
   */
  public static String writeMessage(
      MessageType msg, boolean omitXmlHeaderDeclarationHeader, boolean formatted)
      throws LoxException {
    StringWriter writer = new StringWriter();
    return writeMessage(msg, writer, omitXmlHeaderDeclarationHeader, formatted).toString();
  }

  /**
   * Marshals {@code msg} as XML to the given writer.
   *
   * @param msg the message to serialize
   * @param output the writer to write to
   * @param omitXmlHeaderDeclarationHeader {@code true} to omit the {@code <?xml ...?>} declaration
   * @param formatted {@code true} to enable pretty-printing
   * @return the same {@code output} writer, for chaining
   * @throws LoxException if marshaling fails
   */
  public static Writer writeMessage(
      MessageType msg, Writer output, boolean omitXmlHeaderDeclarationHeader, boolean formatted)
      throws LoxException {
    try {
      createMarshaller(omitXmlHeaderDeclarationHeader, formatted)
          .marshal(objectFactory.createMessage(msg), output);
      return output;
    } catch (JAXBException ex) {
      throw new LoxException(ex);
    }
  }

  /**
   * Marshals {@code msg} as compact XML (no header, not formatted) to the given writer.
   *
   * @param msg the message to serialize
   * @param output the writer to write to
   * @return the same {@code output} writer, for chaining
   * @throws LoxException if marshaling fails
   */
  public static Writer writeMessage(MessageType msg, Writer output) throws LoxException {
    return writeMessage(msg, output, true, false);
  }

  /**
   * Marshals {@code msg} as XML to the given file.
   *
   * @param msg the message to serialize
   * @param output the target file
   * @param omitXmlHeaderDeclarationHeader {@code true} to omit the {@code <?xml ...?>} declaration
   * @param formatted {@code true} to enable pretty-printing
   * @return the same {@code output} file, for chaining
   * @throws LoxException if marshaling fails
   */
  public static File writeMessage(
      MessageType msg, File output, boolean omitXmlHeaderDeclarationHeader, boolean formatted)
      throws LoxException {
    try {
      createMarshaller(omitXmlHeaderDeclarationHeader, formatted)
          .marshal(objectFactory.createMessage(msg), output);
      return output;
    } catch (JAXBException ex) {
      throw new LoxException(ex);
    }
  }

  /**
   * Marshals {@code msg} as compact XML (no header, not formatted) to the given file.
   *
   * @param msg the message to serialize
   * @param output the target file
   * @return the same {@code output} file, for chaining
   * @throws LoxException if marshaling fails
   */
  public static File writeMessage(MessageType msg, File output) throws LoxException {
    return writeMessage(msg, output, true, false);
  }

  /**
   * Marshals {@code msg} as XML to the given output stream.
   *
   * @param msg the message to serialize
   * @param output the output stream
   * @param omitXmlHeaderDeclarationHeader {@code true} to omit the {@code <?xml ...?>} declaration
   * @param formatted {@code true} to enable pretty-printing
   * @return the same {@code output} stream, for chaining
   * @throws LoxException if marshaling fails
   */
  public static OutputStream writeMessage(
      MessageType msg,
      OutputStream output,
      boolean omitXmlHeaderDeclarationHeader,
      boolean formatted)
      throws LoxException {
    try {
      createMarshaller(omitXmlHeaderDeclarationHeader, formatted)
          .marshal(objectFactory.createMessage(msg), output);
      return output;
    } catch (JAXBException ex) {
      throw new LoxException(ex);
    }
  }

  /**
   * Marshals {@code msg} as compact XML (no header, not formatted) to the given output stream.
   *
   * @param msg the message to serialize
   * @param output the output stream
   * @return the same {@code output} stream, for chaining
   * @throws LoxException if marshaling fails
   */
  public static OutputStream writeMessage(MessageType msg, OutputStream output)
      throws LoxException {
    return writeMessage(msg, output, true, false);
  }

  /**
   * Marshals {@code msg} as a UTF-8 XML string, with optional header and formatting control.
   *
   * @param msg the message to serialize
   * @param omitXmlHeaderDeclarationHeader {@code true} to omit the {@code <?xml ...?>} declaration
   * @param formatted {@code true} to enable pretty-printing
   * @return the XML document as a UTF-8 string
   * @throws LoxException if marshaling fails
   */
  public static String writeMessageAsString(
      MessageType msg, boolean omitXmlHeaderDeclarationHeader, boolean formatted)
      throws LoxException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    writeMessage(msg, out, omitXmlHeaderDeclarationHeader, formatted);
    return out.toString(StandardCharsets.UTF_8);
  }
}
