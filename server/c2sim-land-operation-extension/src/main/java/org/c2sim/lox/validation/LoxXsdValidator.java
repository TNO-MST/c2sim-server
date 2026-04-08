package org.c2sim.lox.validation;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.c2sim.lox.exceptions.ValidationException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Validates LOX XML documents against a W3C XML Schema (XSD).
 *
 * <p>Compiled {@link Schema} instances are cached by a caller-supplied {@code cacheId} key so that
 * schema compilation (which is expensive) happens only once per distinct schema. The cache is a
 * static {@link HashMap} and is therefore shared across all instances in the same class-loader.
 *
 * <p>External DTD and schema access is disabled at the {@link SchemaFactory} level to prevent XXE
 * and SSRF attacks ({@link XMLConstants#ACCESS_EXTERNAL_DTD} and {@link
 * XMLConstants#ACCESS_EXTERNAL_SCHEMA} are both set to {@code ""}).
 *
 * <p>This class implements {@link ErrorHandler} so that SAX warnings, errors, and fatal errors are
 * collected into separate lists rather than aborting validation on the first problem. Callers
 * inspect results via {@link #getValidationsIssues(boolean)}, {@link #isValid()}, or the individual
 * getter methods.
 *
 * <p>Typical usage:
 *
 * <pre>{@code
 * LoxXsdValidator result = LoxXsdValidator.doValidation(xmlString);
 * if (!result.isValid()) {
 *     result.getValidationsIssues(false).forEach(e -> log.warn(e.getMessage()));
 * }
 * }</pre>
 */
public class LoxXsdValidator implements ErrorHandler {

  private static final String DEFAULT_XSD_SCHEMA = "lox/xsd/2025/C2SIM_SMX_LOX_CWIX2025.xsd";

  private final List<SAXParseException> validationFatalErrors = new ArrayList<>();
  private final List<SAXParseException> validationErrors = new ArrayList<>();
  private final List<SAXParseException> validationWarnings = new ArrayList<>();

  private static final Map<String, Schema> cacheXsdSchemas = new HashMap<>();

  private final Schema schema;

  private LoxXsdValidator(
      String cacheId, URL urlXsdFile, Function<String, InputStream> nsToStream) {
    schema =
        cacheXsdSchemas.computeIfAbsent(
            cacheId,
            id -> {
              try {
                var xsdStream =
                    new StreamSource(urlXsdFile.openStream(), urlXsdFile.toExternalForm());
                return buildSchema(xsdStream, nsToStream);
              } catch (Exception e) {
                return null;
              }
            });
  }

  /**
   * Returns the non-fatal SAX errors collected during the last validation run.
   *
   * @return mutable list of {@link SAXParseException} instances; never {@code null}
   */
  public List<SAXParseException> getValidationsErrors() {
    return validationErrors;
  }

  /**
   * Returns the fatal SAX errors collected during the last validation run.
   *
   * @return mutable list of {@link SAXParseException} instances; never {@code null}
   */
  public List<SAXParseException> getValidationsFatalErrors() {
    return validationFatalErrors;
  }

  /**
   * Returns the SAX warnings collected during the last validation run.
   *
   * @return mutable list of {@link SAXParseException} instances; never {@code null}
   */
  public List<SAXParseException> getValidationsWarnings() {
    return validationWarnings;
  }

  /**
   * Return if XML is valid according XSD
   *
   * @return if XML content contains errors (warnings are ignored)
   */
  public boolean isValid() {
    return validationErrors.isEmpty() && validationFatalErrors.isEmpty();
  }

  /**
   * Returns all validation issues collected during the last validation run.
   *
   * <p>Fatal errors and non-fatal errors are always included. Warnings are included only when
   * {@code includeWarnings} is {@code true}.
   *
   * @param includeWarnings {@code true} to append SAX warnings to the returned list
   * @return combined list of issues in the order: fatal errors, errors, (warnings); never {@code
   *     null}
   */
  public List<SAXParseException> getValidationsIssues(boolean includeWarnings) {
    List<SAXParseException> allErrors = new ArrayList<>();
    allErrors.addAll(validationFatalErrors);
    allErrors.addAll(validationErrors);
    if (includeWarnings) {
      allErrors.addAll(validationWarnings);
    }
    return allErrors;
  }

  /**
   * Validate XML against XSD
   *
   * @param cacheId Optimize validation process by cache xsd validator
   * @param xsdFile The XSD file to be used
   * @param nsToStream Namespace resolving
   * @param inputStreamXml The XML stream to be validated
   * @throws ValidationException XSD validation errors
   * @return throws exception when XML cannot be parsed
   */
  public static LoxXsdValidator doValidation(
      String cacheId,
      URL xsdFile,
      Function<String, InputStream> nsToStream,
      InputStream inputStreamXml)
      throws ValidationException {
    var validator = new LoxXsdValidator(cacheId, xsdFile, nsToStream);
    validator.runValidator(inputStreamXml);
    return validator;
  }

  /**
   * Validates an XML input stream against the default built-in XSD schema.
   *
   * @param inputStreamXml the XML content to validate
   * @return a {@link LoxXsdValidator} whose results can be inspected
   * @throws ValidationException if the XML cannot be parsed
   */
  public static LoxXsdValidator doValidation(InputStream inputStreamXml)
      throws ValidationException {
    return doValidation("default", getDefaultXsdSchema(), ns -> null, inputStreamXml);
  }

  /**
   * Validate XML against XSD
   *
   * @param xml XML to be validated
   * @return throws exception when XML cannot be parsed
   */
  public static LoxXsdValidator doValidation(String xml) throws ValidationException {
    return doValidation(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * Returns a {@link URL} pointing to the given XSD file on the local filesystem.
   *
   * @param xsdFile path to the XSD file
   * @return a file-protocol {@link URL}, or {@code null} if the path cannot be converted
   */
  public static URL getXsdFromFile(Path xsdFile) {
    try {
      return xsdFile.toUri().toURL();
    } catch (MalformedURLException e) {
      return null;
    }
  }

  /**
   * Returns a {@link URL} for the default built-in XSD schema bundled with this module.
   *
   * @return classpath resource URL for {@code lox/xsd/2025/C2SIM_SMX_LOX_CWIX2025.xsd}
   */
  public static URL getDefaultXsdSchema() {
    return getXsdFromResource(DEFAULT_XSD_SCHEMA);
  }

  /**
   * Looks up an XSD schema by classpath resource name.
   *
   * @param resourceName the classpath-relative resource path (e.g. {@code "lox/xsd/my.xsd"})
   * @return the resource {@link URL}, or {@code null} if not found on the classpath
   */
  public static URL getXsdFromResource(String resourceName) {
    return LoxXsdValidator.class.getClassLoader().getResource(resourceName);
  }

  private static Schema buildSchema(
      StreamSource xsdSchema, Function<String, InputStream> nsToStream) {
    try {
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

      // Lock down external access (assume there is no internet connection)
      // XXE / SSRF  possible when ACCESS_EXTERNAL_SCHEMA is file,jar,http,https
      factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

      factory.setResourceResolver(new ResourceResolver(nsToStream));

      return factory.newSchema(new Source[] {xsdSchema});
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  /*
  Validate XML text against XSD.
   */
  /**
   * Runs the validator against the supplied XML input stream, populating the internal error and
   * warning lists.
   *
   * @param xml the XML content to validate
   * @return {@code this} instance, for chaining
   * @throws ValidationException if the validator itself throws an unexpected exception
   */
  public LoxXsdValidator runValidator(InputStream xml) throws ValidationException {
    try {
      validationWarnings.clear();
      validationErrors.clear();
      validationFatalErrors.clear();
      Validator validator = schema.newValidator();
      validator.setErrorHandler(this);
      validator.validate(new StreamSource(xml));

    } catch (Exception ex) {
      throw new ValidationException(ex);
    }
    return this;
  }

  /* Only for internal use */
  /** {@inheritDoc} */
  @Override
  public void warning(SAXParseException exception) {
    validationWarnings.add(exception);
  }

  /* Only for internal use */
  /** {@inheritDoc} */
  @Override
  public void error(SAXParseException exception) {
    validationErrors.add(exception);
  }

  /* Only for internal use */
  /** {@inheritDoc} */
  @Override
  public void fatalError(SAXParseException exception) {
    validationFatalErrors.add(exception);
  }
}
