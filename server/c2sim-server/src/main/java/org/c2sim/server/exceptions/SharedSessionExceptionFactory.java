package org.c2sim.server.exceptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.c2sim.lox.C2SimMsgKind;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.statemachine.State;

/**
 * Factory for creating {@link C2SimException}s related to shared-session failures.
 *
 * <p>Centralises error construction so that consistent error codes and property maps are used
 * throughout the service layer.
 */
public class SharedSessionExceptionFactory {

  private SharedSessionExceptionFactory() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Creates a {@link C2SimException} indicating that an XML message could not be decoded.
   *
   * @param sessionName the name of the shared session in which the failure occurred
   * @param error the underlying LOX decoding exception
   * @return a {@link C2SimException} with code {@link
   *     C2SimException.ErrorCode#C2SIM_MSG_DECODING_ERROR}
   */
  public static C2SimException createDecodingError(String sessionName, LoxException error) {
    return new C2SimException(
        C2SimException.ErrorCode.C2SIM_MSG_DECODING_ERROR,
        String.format(
            "Failed to convert XML message to POJO, error: %s (shared session: %s)",
            error.getMessage(), sessionName));
  }

  /**
   * Creates a {@link C2SimException} indicating that a trigger was rejected because not all
   * required federates have completed initialization.
   *
   * <p>The returned exception includes the following properties in its detail map:
   *
   * <ul>
   *   <li>{@link C2SimException#PROP_REQUIRED_FEDERATES} — federates that must initialize
   *   <li>{@link C2SimException#PROP_INIT_COMPLETED_FEDERATES} — federates that have confirmed
   * </ul>
   *
   * @param sessionName the name of the shared session that has not reached full initialization
   * @param kind the message kind whose trigger was rejected
   * @param federatesRequired the set of federates that must initialize
   * @param federatesCompleted the set of federates that have confirmed initialization
   * @return a {@link C2SimException} with code {@link
   *     C2SimException.ErrorCode#INITIALIZATION_NOT_COMPLETED}
   */
  public static C2SimException createInitializationNotCompleted(
      String sessionName,
      C2SimMsgKind kind,
      Set<String> federatesRequired,
      Set<String> federatesCompleted) {
    return new C2SimException(
        C2SimException.ErrorCode.INITIALIZATION_NOT_COMPLETED,
        String.format(
            "Trigger '%s' not granted, shared session '%s' is not initialized (yet).",
            kind, sessionName),
        new HashMap<>(
            Map.of(
                C2SimException.PROP_REQUIRED_FEDERATES,
                String.join(",", federatesRequired),
                C2SimException.PROP_INIT_COMPLETED_FEDERATES,
                String.join(",", federatesCompleted))));
  }

  /**
   * Throws a {@link C2SimException} indicating that an Initialization Complete message arrived
   * outside of the {@link org.c2sim.statemachine.State#INITIALIZING} state.
   *
   * @param sessionName the name of the shared session
   * @param systemName the sending system name
   * @param currentState the current state-machine state
   * @throws C2SimException always
   */
  public static void throwInitializationCompleteNotAllowedInState(
      String sessionName, String systemName, State currentState) {
    throw new C2SimException(
        C2SimException.ErrorCode.C2SIM_MSG_NOT_ALLOWED_IN_STATE,
        String.format(
            "Initialization Complete message is only processed in state INITIALIZING, "
                + "current state is %s",
            currentState));
  }
}
