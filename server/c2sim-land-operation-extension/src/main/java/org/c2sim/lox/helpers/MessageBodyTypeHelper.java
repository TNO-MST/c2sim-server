package org.c2sim.lox.helpers;

import jakarta.xml.bind.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.stream.StreamSource;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.schema.MessageBodyType;
import org.c2sim.lox.schema.ObjectFactory;

/**
 * Utility methods for marshaling and unmarshaling {@link MessageBodyType} objects to and from XML.
 *
 * <p>A shared {@link JAXBContext} is created lazily on the first call and reused for all subsequent
 * operations; individual {@link Marshaller} / {@link Unmarshaller} instances are created per call
 * because they are not thread-safe.
 */
public class MessageBodyTypeHelper {

  // Prevent instantiation
  private MessageBodyTypeHelper() {
    throw new AssertionError("Only static functions");
  }

  // static factory for creating objects
  private static final ObjectFactory objectFactory = new ObjectFactory();

  // static context for creating JAXB marshallers
  private static JAXBContext jaxbContext;

  private static synchronized Unmarshaller createUnmarshaller() throws JAXBException {
    if (jaxbContext == null) {
      jaxbContext = JAXBContext.newInstance(MessageBodyType.class);
    }
    return jaxbContext.createUnmarshaller();
  }

  private static synchronized Marshaller createMarshaller(boolean formatted) throws JAXBException {
    if (jaxbContext == null) {
      jaxbContext = JAXBContext.newInstance(MessageBodyType.class);
    }

    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
    if (formatted) {
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }
    return jaxbMarshaller;
  }

  /**
   * Unmarshals a {@link MessageBodyType} from the given input stream.
   *
   * @param input the XML input stream
   * @return the deserialized {@link MessageBodyType}
   * @throws LoxException if JAXB unmarshaling fails
   */
  public static MessageBodyType readMessageBody(InputStream input) throws LoxException {
    try {
      JAXBElement<MessageBodyType> root =
          createUnmarshaller().unmarshal(new StreamSource(input), MessageBodyType.class);
      return root.getValue();
    } catch (JAXBException ex) {
      throw new LoxException(ex);
    }
  }

  /**
   * Unmarshals a {@link MessageBodyType} from the given URL.
   *
   * @param input the URL pointing to an XML document
   * @return the deserialized {@link MessageBodyType}
   * @throws LoxException if the URL cannot be opened or JAXB unmarshaling fails
   */
  public static MessageBodyType readMessageBody(URL input) throws LoxException {
    try {
      try (InputStream stream = input.openStream()) {
        return readMessageBody(stream);
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
   * Unmarshals a {@link MessageBodyType} from the given character reader.
   *
   * @param input the reader providing XML content
   * @return the deserialized {@link MessageBodyType}
   * @throws LoxException if reading or JAXB unmarshaling fails
   */
  public static MessageBodyType readMessageBody(Reader input) throws LoxException {
    try {
      return readMessageBody(getInputStream(input));
    } catch (IOException ex) {
      throw new LoxException(ex);
    }
  }

  /**
   * Marshals {@code msg} as XML to the given writer.
   *
   * @param msg the message body to serialize
   * @param output the writer to write XML to
   * @param formatted {@code true} to enable pretty-printing
   * @return the same {@code output} writer, for chaining
   * @throws LoxException if marshaling fails
   */
  public static Writer writeMessageBody(MessageBodyType msg, Writer output, boolean formatted)
      throws LoxException {
    try {
      createMarshaller(formatted).marshal(objectFactory.createMessageBody(msg), output);
      return output;
    } catch (JAXBException ex) {
      throw new LoxException(ex);
    }
  }

  /**
   * Marshals {@code msg} as compact (non-formatted) XML to the given writer.
   *
   * @param msg the message body to serialize
   * @param output the writer to write XML to
   * @return the same {@code output} writer, for chaining
   * @throws LoxException if marshaling fails
   */
  public static Writer writeMessageBody(MessageBodyType msg, Writer output) throws LoxException {
    return writeMessageBody(msg, output, false);
  }

  /**
   * Marshals {@code msg} as XML to the given file.
   *
   * @param msg the message body to serialize
   * @param output the file to write to
   * @param formatted {@code true} to enable pretty-printing
   * @return the same {@code output} file, for chaining
   * @throws LoxException if marshaling fails
   */
  public static File writeMessageBody(MessageBodyType msg, File output, boolean formatted)
      throws LoxException {
    try {
      createMarshaller(formatted).marshal(objectFactory.createMessageBody(msg), output);
      return output;
    } catch (JAXBException ex) {
      throw new LoxException(ex);
    }
  }

  /**
   * Marshals {@code msg} as compact (non-formatted) XML to the given file.
   *
   * @param msg the message body to serialize
   * @param output the file to write to
   * @return the same {@code output} file, for chaining
   * @throws LoxException if marshaling fails
   */
  public static File writeMessageBody(MessageBodyType msg, File output) throws LoxException {
    return writeMessageBody(msg, output, false);
  }

  /**
   * Marshals {@code msg} as XML to the given output stream.
   *
   * @param msg the message body to serialize
   * @param output the output stream to write to
   * @param formatted {@code true} to enable pretty-printing
   * @return the same {@code output} stream, for chaining
   * @throws LoxException if marshaling fails
   */
  public static OutputStream writeMessageBody(
      MessageBodyType msg, OutputStream output, boolean formatted) throws LoxException {
    try {
      createMarshaller(formatted).marshal(objectFactory.createMessageBody(msg), output);
      return output;
    } catch (JAXBException ex) {
      throw new LoxException(ex);
    }
  }

  /**
   * Marshals {@code msg} as compact (non-formatted) XML to the given output stream.
   *
   * @param msg the message body to serialize
   * @param output the output stream to write to
   * @return the same {@code output} stream, for chaining
   * @throws LoxException if marshaling fails
   */
  public static OutputStream writeMessageBody(MessageBodyType msg, OutputStream output)
      throws LoxException {
    return writeMessageBody(msg, output, false);
  }

  /**
   * Marshals {@code msg} as an XML string.
   *
   * @param msg the message body to serialize
   * @param formatted {@code true} to enable pretty-printing
   * @return the XML representation of {@code msg}
   * @throws LoxException if marshaling fails
   */
  public static String writeMessageBody(MessageBodyType msg, boolean formatted)
      throws LoxException {
    var writer = writeMessageBody(msg, new StringWriter(), formatted);
    return writer.toString();
  }
}
