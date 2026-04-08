package org.c2sim.lox.validation;

import java.io.InputStream;
import java.util.Locale;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * {@link LSResourceResolver} that resolves XSD namespace imports for the JAXP {@link
 * javax.xml.validation.SchemaFactory}.
 *
 * <p>Resolution is attempted in the following order:
 *
 * <ol>
 *   <li>A caller-supplied {@code nsToStream} function (if provided).
 *   <li>A built-in classpath fallback for the C2SIM namespace ({@code
 *       http(s)://www.sisostds.org/schemas/c2sim/…}).
 *   <li>If still unresolved, {@code null} is returned so the parser may attempt its own default
 *       resolution.
 * </ol>
 */
public class ResourceResolver implements LSResourceResolver {

  private static final Logger logger = LoggerFactory.getLogger(ResourceResolver.class);

  private final Function<String, InputStream> nsToStream; // may be null

  /**
   * Creates a resolver with no custom namespace mapping (uses only the built-in C2SIM fallback).
   */
  public ResourceResolver() {
    this.nsToStream = null; // no custom mapping
  }

  /**
   * Creates a resolver with the given namespace-to-stream mapping function.
   *
   * @param resolver a function that maps a namespace URI to an {@link InputStream} of the
   *     corresponding XSD, or {@code null} to use only the built-in fallback
   */
  public ResourceResolver(Function<String, InputStream> resolver) {
    this.nsToStream = resolver; // may be null
  }

  /**
   * {@inheritDoc}
   *
   * <p>Attempts to resolve the XSD for the given namespace URI using the configured resolver chain.
   * Returns {@code null} when the namespace cannot be resolved, allowing the parser to fall back to
   * its default mechanism.
   */
  @Override
  public LSInput resolveResource(
      String type, String namespaceURI, String publicId, String systemId, String baseURI) {
    if (namespaceURI == null) {
      logger.error(
          "Failed to get XSD: null namespace (type={})", type != null ? type : "<<unknown>>");
      return null;
    }

    InputStream in = null;

    // 1) Try user-supplied mapping
    if (nsToStream != null) {
      try {
        in = nsToStream.apply(namespaceURI);
      } catch (RuntimeException ex) {
        logger.warn("Namespace resolver threw for '{}': {}", namespaceURI, ex.toString());
      }
    }

    // 2) Built-in fallback for C2SIM namespace
    if (in == null && isC2SimNamespace(namespaceURI)) {
      final String path = "lox/xsd/2025/C2SIM_SMX_LOX_CWIX2025.xsd";
      in = LoxXsdValidator.class.getClassLoader().getResourceAsStream(path);
      if (in == null) {
        logger.error("Classpath resource not found at '{}'", path);
      }
    }

    // 3) If still null, give up so the parser can try its default resolution
    if (in == null) {
      logger.error(
          "Failed to get XSD for namespace '{}' and type {}",
          namespaceURI,
          (type != null ? type : "<<unknown>>"));
      return null;
    }

    // 4) Produce LSInput
    LSInputImpl input = new LSInputImpl(publicId, systemId, in);
    // Optional but helpful for resolving relative includes:
    input.setBaseURI("classpath:/lox/xsd/2025/");
    return input;
  }

  private static boolean isC2SimNamespace(String ns) {
    String n = ns.toLowerCase(Locale.ROOT);
    return n.startsWith("http://www.sisostds.org/schemas/c2sim/")
        || n.startsWith("https://www.sisostds.org/schemas/c2sim/");
  }
}
