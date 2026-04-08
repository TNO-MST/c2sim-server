package org.c2sim.lox.exceptions;

import java.util.Objects;
import org.c2sim.lox.validation.LoxXsdValidator;

/**
 * Thrown when C2SIM XML fails XSD validation or XML cannot be parsed.
 *
 * <p>Two distinct failure modes are represented:
 *
 * <ul>
 *   <li>XSD validation errors — {@link #validator} is populated with the collected errors; {@link
 *       #exception} is {@code null}.
 *   <li>XML parse failure (malformed XML) — {@link #exception} holds the underlying parse error;
 *       {@link #validator} is {@code null}.
 * </ul>
 */
public class ValidationException extends Exception {

  /**
   * The underlying parse exception when the XML is syntactically invalid, or {@code null} when the
   * failure is due to XSD constraint violations.
   */
  public final Exception exception;

  /**
   * The validator instance containing the collected XSD errors and warnings, or {@code null} when
   * the failure is a parse error.
   */
  public final transient LoxXsdValidator validator; // transient => don't serialize this field

  /**
   * Creates an exception representing XSD validation errors.
   *
   * @param validator the validator that collected the errors
   */
  // There are XSD validation errors
  public ValidationException(LoxXsdValidator validator) {
    super("XSD validation error");
    Objects.requireNonNull(validator);
    this.validator = validator;
    this.exception = null;
  }

  /**
   * Creates an exception representing an XML parse failure.
   *
   * @param ex the underlying parse exception
   */
  public ValidationException(Exception ex) {
    super(ex.getMessage());
    Objects.requireNonNull(ex);
    exception = ex;
    validator = null;
  }
}
