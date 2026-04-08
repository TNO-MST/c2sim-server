package org.c2sim.server.exceptions;

import java.util.HashMap;
import org.c2sim.lox.validation.LoxXsdValidator;

/**
 * Utility for converting a {@link LoxXsdValidator} result into a {@link C2SimException}.
 *
 * <p>Each validation error is added to the exception's property map under the key {@code
 * XSD_ERROR_<n>} (1-based), formatted as {@code "Line <line>: <message>"}.
 */
public class XsdValidatorExceptionHelper {

  private XsdValidatorExceptionHelper() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Converts the validation errors from a {@link LoxXsdValidator} into a {@link C2SimException}
   * with code {@link C2SimException.ErrorCode#XSD_VALIDATION_ERROR}.
   *
   * @param validator the validator whose {@link LoxXsdValidator#getValidationsErrors()} list is
   *     non-empty
   * @return a {@link C2SimException} whose property map contains one entry per validation error
   */
  public static C2SimException convert(LoxXsdValidator validator) {
    HashMap<String, Object> prop = new HashMap<>();
    short index = 1;
    for (var err : validator.getValidationsErrors()) {
      prop.put(
          "XSD_ERROR_" + index,
          String.format("Line %d: %s", err.getLineNumber(), err.getMessage()));
      index++;
    }
    return new C2SimException(
        C2SimException.ErrorCode.XSD_VALIDATION_ERROR,
        "XSD validation errors on XML document",
        prop);
  }
}
