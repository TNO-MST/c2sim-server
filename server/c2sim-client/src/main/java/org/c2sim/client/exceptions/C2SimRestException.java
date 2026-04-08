package org.c2sim.client.exceptions;

import org.c2sim.client.invoker.ApiException;
import org.c2sim.client.model.C2SimError;

/**
 * Wraps a C2SIM REST API error response.
 *
 * <p>Thrown when an {@link ApiException} with HTTP&nbsp;400 is received and its body can be
 * deserialized into a {@link C2SimError}. Callers can inspect the structured error via {@link
 * #getError()} or use {@link #getErrorStatusCode()} for enum-based branching.
 */
public final class C2SimRestException extends Exception {

  /** Typed status codes returned by the C2SIM REST API. */
  public enum ErrorStatusCode {
    /** Shared session not found */
    SHARED_SESSION_NOT_FOUND,
    /** Not specified error code */
    UNKNOWN;

    /**
     * Parses a status-code string into the corresponding enum constant.
     *
     * @param value the raw status code string, or {@code null}
     * @return the matching constant, or {@link #UNKNOWN} if the value is {@code null} or unknown
     */
    public static ErrorStatusCode fromString(String value) {
      if (value == null) {
        return UNKNOWN;
      }

      try {
        return ErrorStatusCode.valueOf(value.trim().toUpperCase());
      } catch (IllegalArgumentException e) {
        return UNKNOWN;
      }
    }
  }

  private final transient C2SimError error; // transient => don't serialize

  /**
   * Creates a new exception that wraps the given API exception and structured error body.
   *
   * @param apiException the original API exception
   * @param error the deserialized C2SIM error response body
   */
  public C2SimRestException(ApiException apiException, C2SimError error) {
    super(apiException);
    this.error = error;
  }

  /**
   * Returns the structured C2SIM error response body.
   *
   * @return the error object
   */
  public C2SimError getError() {
    return error;
  }

  /**
   * Returns the typed status code from the C2SIM error response.
   *
   * @return the parsed {@link ErrorStatusCode}, or {@link ErrorStatusCode#UNKNOWN} if the code
   *     cannot be mapped
   */
  public ErrorStatusCode getErrorStatusCode() {
    return ErrorStatusCode.fromString(error.getCode());
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the error message and code from the C2SIM error body when available, otherwise falls
   * back to the standard exception message.
   */
  @Override
  public String toString() {
    return (error != null) ? error.getMessage() + " (" + error.getCode() + ")" : getMessage();
  }
}
