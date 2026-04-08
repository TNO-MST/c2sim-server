package org.c2sim.client.exceptions;

import java.io.IOException;

/**
 * Thrown when an authentication failure occurs during a C2SIM REST call.
 *
 * <p>Extends {@link IOException} so it can propagate through OkHttp's {@code Interceptor}
 * interface, which only allows {@code IOException} to be thrown.
 */
public final class C2SimRestAuthException extends IOException {

  /** The cause to the REST auth error */
  private final ErrorType errorType;

  /** Enum for authentication failures. */
  public enum ErrorType {
    /** Failed to fetch endpoint info from IDP provider */
    ENDPOINT_DISCOVERY_ACCESS_TOKEN_FAILED,
    /** Failed to fetch access token from IDP provider */
    ACCESS_TOKEN_RETRIEVAL_FAILED,
    /** Unknown error */
    UNKNOWN_ERROR
  }

  /**
   * Creates a new exception with the given error type and detail message.
   *
   * @param errorType the authentication failure category
   * @param message the detail message
   */
  public C2SimRestAuthException(ErrorType errorType, String message) {
    super(message);
    this.errorType = errorType;
  }

  /**
   * Creates a new exception with the given error type, detail message, and cause.
   *
   * @param errorType the authentication failure category
   * @param message the detail message
   * @param cause the underlying cause
   */
  public C2SimRestAuthException(ErrorType errorType, String message, Throwable cause) {
    super(message, cause);
    this.errorType = errorType;
  }

  /**
   * Returns the authentication failure category.
   *
   * @return the error type
   */
  public ErrorType getErrorType() {
    return errorType;
  }
}
