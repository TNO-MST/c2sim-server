package org.c2sim.client.exceptions;

/**
 * General-purpose exception for the C2SIM client library.
 *
 * <p>Each instance carries an {@link ErrorCode} that identifies the category of the failure,
 * allowing callers to react programmatically without parsing the message string.
 */
public final class C2ClientException extends Exception {

  /** Enum for kinds of errors the C2SIM client can encounter. */
  public enum ErrorCode {
    /** Not allowed to change from session when connected */
    SHARED_SESSION_NAME_CHANGED_WHILE_CONNECTED,
    /** Failed to set up streaming connection */
    STREAMING_CONNECT_ERROR,
    /** Not enough information to set up shared session */
    NO_SHARED_SESSION_PROVIDER,
    /** C2SIM server return empty C2InitializationBody in non initializing state */
    EMPTY_INITIALIZATION_BODY,
    /** Unknown error */
    UNKNOWN_ERROR
  }

  /** The REST error code */
  private final ErrorCode errorCode;

  /**
   * Creates a new exception with the given error code and detail message.
   *
   * @param code the error category
   * @param message the detail message
   */
  public C2ClientException(ErrorCode code, String message) {
    super(message);
    errorCode = code;
  }

  /**
   * Get the error category
   *
   * @return return error code
   */
  public ErrorCode getErrorCode() {
    return errorCode;
  }

  /**
   * Returns a formatted log string that includes the error code and the detail message.
   *
   * @return a string of the form {@code ERROR[<code>]: <message>}
   */
  public String getLogError() {
    return String.format("ERROR[%s]: %s", errorCode.toString(), getMessage());
  }
}
