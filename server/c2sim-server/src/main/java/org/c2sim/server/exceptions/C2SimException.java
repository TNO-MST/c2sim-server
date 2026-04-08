package org.c2sim.server.exceptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.c2sim.server.api.models.C2SimError;

/**
 * Generic server-side exception that carries a structured {@link C2SimError} response body.
 *
 * <p>Extends {@link RuntimeException} because the generated Javalin server-stub code only allows
 * unchecked exceptions to propagate through handler methods. Each instance wraps an {@link
 * ErrorCode} and an optional property map that is serialised into the HTTP 400 response.
 *
 * <p>Well-known property-name constants (e.g. {@link #PROP_ACTIVE_SESSIONS}, {@link
 * #PROP_CURRENT_STATE}) allow callers to attach structured diagnostic data via {@link
 * #addProperty(String, String)}.
 */
public class C2SimException extends RuntimeException {

  /** Property key: comma-separated list of all active session names. */
  public static final String PROP_ACTIVE_SESSIONS = "PROP_ACTIVE_SESSIONS";

  /** Property key: name of the active shared session. */
  public static final String PROP_ACTIVE_SESSION = "ACTIVE_SESSION";

  /** Property key: name of the system. */
  public static final String PROP_SYSTEM_NAME = "SYSTEM_NAME";

  /** Property key: the C2SIM schema version of the session. */
  public static final String PROP_SCHEMA_VERSION = "SCHEMA_VERSION";

  /** Property key: comma-separated list of supported schema versions. */
  public static final String PROP_SUPPORTED_SCHEMA_VERSIONS = "SUPPORTED_SCHEMA_VERSIONS";

  /** Property key: the publish tracking ID. */
  public static final String PROP_TRACKING_ID = "PROP_TRACKING_ID";

  /** Property key: the current state-machine state. */
  public static final String PROP_CURRENT_STATE = "CURRENT_STATE";

  /** Property key: comma-separated list of triggers permitted in the current state. */
  public static final String PROP_ALLOWED_TRIGGERS = "ALLOWED_TRIGGERS";

  /** Property key: comma-separated list of federates required to complete initialization. */
  public static final String PROP_REQUIRED_FEDERATES = "PROP_REQUIRED_FEDERATES";

  /** Property key: comma-separated list of federates that have confirmed initialization. */
  public static final String PROP_INIT_COMPLETED_FEDERATES = "PROP_INIT_COMPLETED_FEDERATES";

  /**
   * Typed error codes for C2SIM server errors, serialised as the {@code code} field in the HTTP
   * response body.
   */
  public enum ErrorCode {
    /** The requested shared session does not exist. */
    SHARED_SESSION_NOT_FOUND("SHARED_SESSION_NOT_FOUND"),
    /** A session with the same name already exists. */
    SHARED_SESSION_ALREADY_EXIST("SHARED_SESSION_ALREADY_EXIST"),
    /** The C2SIM schema version of the session was changed while clients were connected. */
    SHARED_SESSION_SCHEMA_VERSION_CHANGED("SHARED_SESSION_SCHEMA_VERSION_CHANGED"),
    /** One or more XSD validation errors were found in the submitted XML document. */
    XSD_VALIDATION_ERROR("XSD_VALIDATION_ERROR"),
    /** The requested C2SIM schema version is not supported by the server. */
    C2SIM_SCHEMA_NOT_SUPPORTED("C2SIM_SCHEMA_NOT_SUPPORTED"),
    /** The XSD validator itself threw an exception (schema could not be loaded). */
    XSD_VALIDATION_FAILURE("XSD_VALIDATION_FAILURE"),
    /** No {@code clientId} was provided in the request. */
    NO_CLIENT_ID("NO_CLIENT_ID"),
    /** The supplied {@code clientId} is not registered in the session. */
    CLIENT_ID_NOT_EXIST("CLIENT_ID_NOT_EXIST"),
    /** The requested operation is not yet implemented. */
    NOT_IMPLEMENTED("NOT_IMPLEMENTED"),
    /** The submitted C2SIM message exceeds the maximum allowed size. */
    C2SIM_MSG_SIZE_EXCEEDED("C2SIM_MSG_SIZE_EXCEEDED"),
    /** The client has not joined the shared session. */
    CLIENT_NOT_JOINED_SHARED_SESSION("CLIENT_NOT_JOINED_SHARED_SESSION"),
    /** The submitted C2SIM message is not allowed in the current state. */
    C2SIM_MSG_NOT_ALLOWED_IN_STATE("C2SIM_MSG_NOT_ALLOWED_IN_STATE"),
    /** The submitted XML could not be decoded into a POJO. */
    C2SIM_MSG_DECODING_ERROR("C2SIM_MSG_DECODING_ERROR"),
    /** A C2SIM initialization message was received in an invalid state. */
    C2SIM_INITIALIZATION_MSG_INVALID_STATE("C2SIM_INITIALIZATION_MSG_INVALID_STATE"),
    /** The C2SIM initialization body could not be decoded. */
    C2SIM_INITIALIZATION_MSG_DECODE_FAILURE("C2SIM_INITIALIZATION_MSG_DECODE_FAILURE"),
    /** The message contained no C2SIM initialization body. */
    NO_C2SIM_INITIALIZATION_BODY("NO_C2SIM_INITIALIZATION_BODY"),
    /** The requested state-machine transition is not allowed from the current state. */
    STATE_TRANSITION_NOT_ALLOWED("STATE_TRANSITION_NOT_ALLOWED"),
    /**
     * The trigger was rejected because not all required federates have completed initialization.
     */
    INITIALIZATION_NOT_COMPLETED("INITIALIZATION_NOT_COMPLETED"),
    /** The root element of XML must be message */
    C2SIM_ROOT_ELEMENT_MUST_BE_MESSAGE("C2SIM_ROOT_ELEMENT_MUST_BE_MESSAGE"),
    /** The XML C2SIM header in XML is invalid */
    C2SIM_INVALID_HEADER("C2SIM_INVALID_HEADER"),
    /** Authorization failure (claims) */
    AUTHORIZATION_FAILURE("AUTHORIZATION_FAILURE"),
    /** An I/O error occurred on the server. */
    IO_ERROR("IO_ERROR");

    private final String code;

    private static final Map<String, ErrorCode> BY_CODE =
        Arrays.stream(values()).collect(Collectors.toMap(ErrorCode::getCode, e -> e));

    ErrorCode(String code) {
      this.code = code;
    }

    /**
     * Returns the string code that will appear in the HTTP response body.
     *
     * @return the error code string
     */
    public String getCode() {
      return code;
    }

    public ErrorCode getCodeEnum() {
      return BY_CODE.get(code);
    }

    public static ErrorCode fromCode(String code) {
      return BY_CODE.get(code);
    }
  }

  private final transient C2SimError error; // transient => don't serialize

  /**
   * Creates a new exception with the given error code, message, and additional properties.
   *
   * @param errorCode the error category
   * @param message the human-readable detail message
   * @param prop additional key-value diagnostic properties included in the response body
   */
  public C2SimException(ErrorCode errorCode, String message, Map<String, Object> prop) {
    super(message);
    error = new C2SimError(errorCode.getCode(), message, prop);
  }

  /**
   * Creates a new exception with the given error code and message, and an empty property map.
   *
   * @param errorCode the error category
   * @param message the human-readable detail message
   */
  public C2SimException(ErrorCode errorCode, String message) {
    super(message);
    error = new C2SimError(errorCode.getCode(), message, new HashMap<>());
  }

  /**
   * Returns the structured error object that will be serialised into the HTTP response body.
   *
   * @return the error object
   */
  public C2SimError getError() {
    return error;
  }

  /**
   * Adds a diagnostic key-value property to the error's detail map.
   *
   * @param key the property key (use one of the {@code PROP_*} constants)
   * @param value the property value
   */
  public void addProperty(String key, String value) {
    Objects.requireNonNull(error.getDetails()).put(key, value);
  }

  /**
   * Get property from property bag
   *
   * @param key the property key (use one of the {@code PROP_*} constants)
   * @param defaultValue Value when key not found
   * @return The value
   */
  public String getProperty(String key, String defaultValue) {
    if ((error == null) || (error.getDetails() == null)) {
      return defaultValue;
    }
    return (String) error.getDetails().getOrDefault(key, defaultValue);
  }
}
