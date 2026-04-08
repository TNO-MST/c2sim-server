package org.c2sim.lox.sax;

import static org.c2sim.lox.helpers.MeasureHelper.measureMs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.c2sim.lox.C2SimMsgKind;
import org.c2sim.lox.helpers.MeasureHelper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Detect the root message kind from XML message without complete parsing the message */
public final class DetectMsgKind {
  private static final String XML_PATH_DELIMITER = "/";

  private DetectMsgKind() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Extract message kind from XML message
   *
   * @param xmlInput the XML messages
   * @return The message kind
   */
  public static MeasureHelper.MeasurementResult<C2SimMsgKind> determineMsgKindMeasured(
      InputStream xmlInput) {
    return measureMs(() -> determineMsgKind(xmlInput));
  }

  /**
   * Extract message kind from XML message
   *
   * @param xmlInput the XML messages
   * @return The message kind
   */
  public static C2SimMsgKind determineMsgKind(String xmlInput) {
    return determineMsgKind(new ByteArrayInputStream(xmlInput.getBytes()));
  }

  /**
   * Extract message kind from XML message
   *
   * @param xmlInput the XML messages
   * @return The message kind
   */
  public static C2SimMsgKind determineMsgKind(InputStream xmlInput) {

    try {

      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

      factory.setNamespaceAware(true); // so localName works
      factory.setValidating(false);
      SAXParser parser = factory.newSAXParser();

      SaxPathMatcherHandler handler = new SaxPathMatcherHandler();
      parser.parse(xmlInput, handler);
      return handler.getMatchedEnum();

    } catch (Exception ex) {
      return C2SimMsgKind.ERROR;
    }
  }

  /** Helper to parse XML with SAX parser */
  // --- Step 2: Handler that matches paths to enum ---
  public static class SaxPathMatcherHandler extends DefaultHandler {
    private final Deque<String> stack = new ArrayDeque<>();

    private C2SimMsgKind matched = C2SimMsgKind.UNKNOWN;

    private static String stripPrefix(String qName) {
      int i = qName.indexOf(':');
      return i >= 0 ? qName.substring(i + 1) : qName;
    }

    /**
     * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if
     *     Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace processing
     *     is not being performed.
     * @param qName The qualified name (with prefix), or the empty string if qualified names are not
     *     available.
     * @param attributes The attributes attached to the element. If there are no attributes, it
     *     shall be an empty Attributes object.
     * @throws SAXException XML failed to parse
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException {
      if (matched != C2SimMsgKind.UNKNOWN) {
        return; // stop processing early
      }
      String name = (localName != null && !localName.isEmpty()) ? localName : stripPrefix(qName);
      stack.addLast(name);
    }

    /**
     * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if
     *     Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace processing
     *     is not being performed.
     * @param qName The qualified name (with prefix), or the empty string if qualified names are not
     *     available.
     * @throws SAXException failed to parse XML
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if (matched != C2SimMsgKind.UNKNOWN) {
        return;
      }

      String currentPath = XML_PATH_DELIMITER + String.join(XML_PATH_DELIMITER, stack);
      for (C2SimMsgKind t : C2SimMsgKind.values()) {
        if (t != C2SimMsgKind.UNKNOWN && currentPath.equals(C2SimMsgKind.getPath(t))) {
          matched = t;
          break;
        }
      }

      stack.removeLast();
    }

    /**
     * Return the matched message kind
     *
     * @return Returns the found message kind
     */
    public C2SimMsgKind getMatchedEnum() {
      return matched;
    }
  }
}
