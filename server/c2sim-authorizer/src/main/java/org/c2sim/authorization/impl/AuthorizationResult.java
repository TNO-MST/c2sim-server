package org.c2sim.authorization.impl;

/**
 * Immutable value object returned by the C2SIM authorizer describing the outcome of an
 * authorization check.
 *
 * <p>A result carries a {@link Code} that classifies the outcome and an optional human-readable
 * {@link #message} with additional context (e.g. which claim failed). The singleton {@link #OK}
 * constant should be used for successful checks to avoid unnecessary object allocation.
 *
 * <p>Failure results are created via the {@link #create(Code, String)} factory method.
 */
public class AuthorizationResult {

  /**
   * Shared singleton representing a successful authorization check.
   *
   * <p>Callers should return this constant rather than constructing a new instance when
   * authorization passes.
   */
  public static final AuthorizationResult OK =
      new AuthorizationResult(AuthorizationResult.Code.AUTHORIZED);

  /** Classifies the outcome of an authorization check. */
  public enum Code {
    /** Request is authorized */
    AUTHORIZED,
    /** Request is un-authorized */
    UNAUTHORIZED,
    /** Request has invalid credentials */
    INVALID_CREDENTIALS,
    /** Request could not be handled */
    ERROR
  }

  /** The outcome code for this result. */
  public final Code code;

  /** Optional human-readable description of the outcome; empty string when not provided. */
  public final String message;

  /**
   * Creates an {@link AuthorizationResult} with the given code and message.
   *
   * @param code the authorization outcome
   * @param message a human-readable description; may be empty but not {@code null}
   * @return a new {@link AuthorizationResult}
   */
  public static AuthorizationResult create(Code code, String message) {
    return new AuthorizationResult(code, message);
  }

  /**
   * Constructs a result with an explicit outcome code and message.
   *
   * @param code the authorization outcome
   * @param message a human-readable description; may be empty but not {@code null}
   */
  public AuthorizationResult(Code code, String message) {
    this.code = code;
    this.message = message;
  }

  /**
   * Constructs a result with an explicit outcome code and an empty message.
   *
   * @param code the authorization outcome
   */
  public AuthorizationResult(Code code) {
    this.code = code;
    message = "";
  }
}
