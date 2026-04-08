package org.c2sim.server.utils;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.c2sim.server.exceptions.C2SimException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Minifies XML strings by removing insignificant whitespace and inter-element spacing.
 *
 * <p>The minified output is used as the over-the-wire format for the WebSocket streaming channel,
 * where a trailing newline character ({@code \n}) acts as the message delimiter between consecutive
 * C2SIM documents.
 *
 * <p>The {@link DocumentBuilderFactory} and {@link TransformerFactory} instances are shared across
 * calls (they are thread-safe), while the per-call {@code DocumentBuilder} and {@code Transformer}
 * objects are created fresh for each invocation.
 *
 * <p>This is a utility class; instantiation is not allowed.
 */
public class XmlMinifier {

  // Prevent instantiation
  private XmlMinifier() {
    throw new AssertionError("Only static functions");
  }

  // Safe to reuse these factories
  private static final DocumentBuilderFactory DOCUMENT_FACTORY;
  private static final TransformerFactory TRANSFORMER_FACTORY;

  static {
    DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();
    DOCUMENT_FACTORY.setIgnoringComments(true);
    DOCUMENT_FACTORY.setCoalescing(true);
    DOCUMENT_FACTORY.setIgnoringElementContentWhitespace(true);

    TRANSFORMER_FACTORY = TransformerFactory.newInstance();
  }

  /**
   * Parses the given XML string and returns a compact single-line representation with all
   * inter-element whitespace removed.
   *
   * @param xml the well-formed XML input string
   * @return the minified XML string (no leading/trailing whitespace, no newlines between tags)
   * @throws C2SimException with {@link C2SimException.ErrorCode#IO_ERROR} if the XML cannot be
   *     parsed or serialised
   */
  public static String minifyXml(String xml) {
    try {
      // Builder is NOT thread-safe → create per call
      var builder = DOCUMENT_FACTORY.newDocumentBuilder();
      Document doc = builder.parse(new InputSource(new StringReader(xml)));

      // Transformer is NOT thread-safe → create per call
      Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "no");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(doc), new StreamResult(writer));

      // compact inter-tag whitespace
      return writer.toString().replaceAll(">\\s+<", "><").trim();

    } catch (Exception e) {
      throw new C2SimException(C2SimException.ErrorCode.IO_ERROR, "Failed to minify XML");
    }
  }
}
