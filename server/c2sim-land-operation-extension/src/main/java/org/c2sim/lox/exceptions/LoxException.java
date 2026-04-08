package org.c2sim.lox.exceptions;

/** Exception related to LOX / C2SIM XML operations (wrapper around Exceptions). */
public class LoxException extends Exception {

  /** The original causing exception, or {@code null} if not caused by another exception. */
  public final Exception orginalException;

  /** Human-readable error description. */
  public final String message;

  /**
   * Creates a new exception with the given message and no cause.
   *
   * @param message the error description
   */
  public LoxException(String message) {
    this(message, null);
  }

  /**
   * Creates a new exception wrapping an existing exception. The message is taken from {@code
   * ex.getMessage()}.
   *
   * @param ex the causing exception
   */
  public LoxException(Exception ex) {
    this(ex.getMessage(), ex);
  }

  /**
   * Creates a new exception with the given message and cause.
   *
   * @param message the error description
   * @param ex the original causing exception, or {@code null}
   */
  public LoxException(String message, Exception ex) {
    this.orginalException = ex;
    this.message = message;
  }
}
