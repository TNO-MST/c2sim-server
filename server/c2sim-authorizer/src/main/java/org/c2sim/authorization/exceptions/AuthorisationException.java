package org.c2sim.authorization.exceptions;

public class AuthorisationException extends RuntimeException {

  private final AuthErrorCode ErrorCode;
  public enum AuthErrorCode {
      ACCESS_TOKEN_FETCH_FAILED,
      INVALID_CLAIM_DATATYPE,
      INVALID_JWT,
      SIGNATURE_VERIFICATION_FAILED,
      AUTHORISATION_HEADER_MISSING,
      CLAIM_CHECK
  }

  public AuthorisationException(AuthErrorCode code, String message) {
    super(message);
    ErrorCode = code;
  }

  public AuthorisationException(AuthErrorCode code, String message, Throwable cause) {
    super(message, cause);
    ErrorCode = code;
  }

  public AuthorisationException(AuthErrorCode code, Throwable cause) {
    super(cause);
    ErrorCode = code;
  }

  /**
   * Get the error code for auth failure
   * @return errorCode
   */
  public AuthErrorCode getErrorCode() {
    return ErrorCode;
  }
}
