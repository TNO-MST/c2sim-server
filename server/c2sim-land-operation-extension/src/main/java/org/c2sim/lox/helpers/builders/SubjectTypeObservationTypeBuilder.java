package org.c2sim.lox.helpers.builders;

import java.util.UUID;
import java.util.regex.Pattern;
import org.c2sim.lox.schema.APP6CSymbolType;
import org.c2sim.lox.schema.SubjectTypeObservationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder for {@link SubjectTypeObservationType} objects.
 *
 * <p>A subject-type observation carries an APP-6C symbol identification code (SIDC) that classifies
 * the observed entity. The SIDC is validated against the APP-6C pattern before being stored;
 * invalid codes are silently dropped (the field is set to {@code null} and a message is printed to
 * stdout).
 */
public class SubjectTypeObservationTypeBuilder {
  private static final Logger logger =
      LoggerFactory.getLogger(SubjectTypeObservationTypeBuilder.class);

  private final SubjectTypeObservationType subject = new SubjectTypeObservationType();

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static SubjectTypeObservationTypeBuilder create() {
    return new SubjectTypeObservationTypeBuilder();
  }

  /**
   * Sets the UUID of the observed actor.
   *
   * @param actorReference the actor UUID
   * @return this builder
   */
  public SubjectTypeObservationTypeBuilder actorReference(UUID actorReference) {
    subject.setActorReference(actorReference.toString());
    return this;
  }

  /**
   * Sets the confidence level of the observation.
   *
   * @param confidenceLevel the confidence level (0.0–1.0), or {@code null}
   * @return this builder
   */
  public SubjectTypeObservationTypeBuilder confidenceLevel(Double confidenceLevel) {
    subject.setConfidenceLevel(confidenceLevel);
    return this;
  }

  /**
   * Sets the uncertainty interval of the observation.
   *
   * @param uncertaintyInterval the uncertainty interval, or {@code null}
   * @return this builder
   */
  public SubjectTypeObservationTypeBuilder uncertaintyInterval(Double uncertaintyInterval) {
    subject.setUncertaintyInterval(uncertaintyInterval);
    return this;
  }

  @SuppressWarnings("checkstyle:LineLength")
  private static final Pattern APP6C_PATTERN =
      Pattern.compile(
          "[SGIWMO][PUAFNSHJKO\\-][PAGMOSTUFVXLIZ\\-]"
              + "[AP\\-][A-Z\\-]{6}[A-Z\\-*]{2}-{2}[AECGNSX\\-*]");

  /**
   * Returns {@code true} if the given string matches the APP-6C SIDC pattern.
   *
   * @param code the string to validate
   * @return {@code true} when the code is a valid APP-6C SIDC
   */
  public static boolean isValidApp6Symbol(String code) {
    return APP6C_PATTERN.matcher(code).matches();
  }

  // <xs:pattern
  // value="[SGIWMO]{1}[PUAFNSHJKO\-]{1}[PAGMOSTUFVXLIZ\-]{1}[AP\-]{1}[A-Z\-]{6}[A-Z\-\*]{2}[\-]{2}[AECGNSX\-\*]{1}"/>

  /**
   * Sets the APP-6C symbol identification code.
   *
   * <p>If the code is invalid (as determined by {@link #isValidApp6Symbol}), the field is set to
   * {@code null} and a message is printed to stdout.
   *
   * @param app6CSymbol the APP-6C SIDC string
   * @return this builder
   */
  public SubjectTypeObservationTypeBuilder app6CSymbol(String app6CSymbol) {
    if (isValidApp6Symbol(app6CSymbol)) {
      var app = new APP6CSymbolType();
      app.setAPP6CSIDC(app6CSymbol);
      subject.setAPP6CSymbol(app);
    } else {
      logger.warn("Invalid APP6A {}", app6CSymbol);
      subject.setAPP6CSymbol(null);
    }
    return this;
  }

  /**
   * Builds and returns the {@link SubjectTypeObservationType}.
   *
   * @return the constructed subject-type observation
   */
  public SubjectTypeObservationType build() {
    return subject;
  }
}
