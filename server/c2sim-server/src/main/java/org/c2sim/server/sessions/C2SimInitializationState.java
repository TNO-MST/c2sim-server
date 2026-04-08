package org.c2sim.server.sessions;

import static org.c2sim.lox.helpers.MessageTypeHelper.writeMessageAsString;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.helpers.C2SIMInitializationBodyTypeHelper;
import org.c2sim.lox.helpers.MessageTypeHelper;
import org.c2sim.lox.helpers.XmlFactoryHelper;
import org.c2sim.lox.schema.C2SIMHeaderType;
import org.c2sim.lox.schema.C2SIMInitializationBodyType;
import org.c2sim.server.exceptions.C2SimException;
import org.c2sim.server.exceptions.SharedSessionExceptionFactory;
import org.c2sim.server.services.ConfigService;
import org.c2sim.statemachine.State;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks the C2SIM initialization lifecycle for a single {@link SharedSession}.
 *
 * <p>Encapsulates:
 *
 * <ul>
 *   <li>The parsed {@link C2SIMInitializationBodyType} body
 *   <li>A cached XML representation (with server C2SIM header) for distribution
 *   <li>The set of federates declared in the scenario
 *   <li>The set of federates that have reported Initialization Complete
 * </ul>
 */
class C2SimInitializationState {

  private static final Logger logger = LoggerFactory.getLogger(C2SimInitializationState.class);

  private final String sessionName;
  private final ConfigService configService;

  private C2SIMInitializationBodyType c2simInitialization = null;
  private String c2simInitializationAsXml = null;
  private Set<String> federatesDefinedInScenario = Set.of();
  private Set<String> federatesInitialized = new HashSet<>();

  C2SimInitializationState(@NotNull String sessionName, @NotNull ConfigService configService) {
    this.sessionName = sessionName;
    this.configService = configService;
  }

  /**
   * Processes a C2SIM initialization body from a received XML message.
   *
   * <p>Must only be called when in {@link State#INITIALIZING}.
   *
   * @param xmlStream the raw XML bytes of the C2SIM message
   * @param currentState the current state-machine state
   * @throws C2SimException if not in INITIALIZING state, or if the body cannot be decoded
   */
  void receive(ByteArrayInputStream xmlStream, State currentState) {
    if (currentState != State.INITIALIZING) {
      throw new C2SimException(
          C2SimException.ErrorCode.C2SIM_INITIALIZATION_MSG_INVALID_STATE,
          String.format(
              "C2SIM initialization is only processed in state INITIALIZING, current state is %s",
              currentState));
    }
    try {
      var msg = MessageTypeHelper.readMessage(xmlStream);
      set(msg.getMessageBody().getC2SIMInitializationBody());
    } catch (LoxException e) {
      throw new C2SimException(
          C2SimException.ErrorCode.C2SIM_INITIALIZATION_MSG_DECODE_FAILURE,
          String.format(
              "Failed to convert C2SIM initialization message to object, error: %s",
              e.getMessage()));
    }
  }

  /**
   * Records that the given system has completed initialization.
   *
   * <p>Must only be called when in {@link State#INITIALIZING}.
   *
   * @param header the C2SIM header from the Initialization Complete message
   * @param currentState the current state-machine state
   * @throws C2SimException if not in INITIALIZING state
   */
  void recordInitializationComplete(C2SIMHeaderType header, State currentState) {
    if (currentState != State.INITIALIZING) {
      SharedSessionExceptionFactory.throwInitializationCompleteNotAllowedInState(
          sessionName, header.getFromSendingSystem(), currentState);
    }
    var systemName = header.getFromSendingSystem();
    logger.info(
        "Session '{}': System '{}' reported 'Initialization Complete'.", sessionName, systemName);
    federatesInitialized.add(systemName);
  }

  /**
   * Returns {@code true} if a C2SIM initialization body has been received.
   *
   * @return {@code true} when an initialization body is present
   */
  boolean hasReceivedInitialization() {
    return c2simInitialization != null;
  }

  /**
   * Returns {@code true} when all federates declared in the scenario have reported Initialization
   * Complete.
   *
   * @return {@code true} if all required federates have confirmed
   */
  boolean areAllFederatesInitialized() {
    return !federatesDefinedInScenario.isEmpty()
        && federatesInitialized.containsAll(federatesDefinedInScenario);
  }

  /**
   * Returns the cached C2SIM initialization XML string (with server header).
   *
   * @param currentState the current state-machine state
   * @throws C2SimException if the session has not yet been initialized
   */
  String getAsXml(State currentState) {
    if (currentState == State.UNINITIALIZED
        || currentState == State.INITIALIZING
        || c2simInitializationAsXml == null) {
      throw new C2SimException(
          C2SimException.ErrorCode.NO_C2SIM_INITIALIZATION_BODY,
          String.format(
              "Shared session '%s' is initializing, there is no C2SIMInitialization.",
              sessionName));
    }
    return c2simInitializationAsXml;
  }

  /**
   * Returns the cached initialization XML as a fresh {@link ByteArrayInputStream}.
   *
   * @return the initialization XML stream
   */
  ByteArrayInputStream getAsXmlStream() {
    return new ByteArrayInputStream(c2simInitializationAsXml.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Returns the parsed {@link C2SIMInitializationBodyType}, or {@code null} if none received.
   *
   * @return the initialization body, or {@code null}
   */
  C2SIMInitializationBodyType getC2SIMInitialization() {
    return c2simInitialization;
  }

  /**
   * Returns the set of system names that have reported Initialization Complete.
   *
   * @return the set of initialized federate names
   */
  Set<String> getFederatesInitialized() {
    return federatesInitialized;
  }

  /**
   * Returns the set of federate system names required to initialize (from the scenario document).
   *
   * @return the set of required federate names
   */
  Set<String> getFederatesDefinedInScenario() {
    return federatesDefinedInScenario;
  }

  /** Resets all initialization state (called on entering {@link State#UNINITIALIZED}). */
  void clear() {
    logger.info("Session '{}': Resetting C2SIM initialization state.", sessionName);
    c2simInitialization = null;
    c2simInitializationAsXml = "";
    federatesDefinedInScenario = Set.of();
    federatesInitialized = new HashSet<>();
  }

  /*
   * Stores the received initialization body, extracts required federates, and caches
   * the XML string (with server C2SIM header) for later distribution.
   */
  private void set(C2SIMInitializationBodyType body) {
    if (this.c2simInitialization != null && body != null) {
      logger.warn(
          "Session '{}': There was already a C2SIM initialization — overwriting.", sessionName);
    }
    this.c2simInitialization = body;
    if (body != null) {
      this.federatesDefinedInScenario =
          C2SIMInitializationBodyTypeHelper.getRequiredFederates(body, Set.of());
      logger.info(
          "Session '{}': Assigned C2SIMInitializationBody, system(s) in scenario [{}]",
          sessionName,
          String.join(",", this.federatesDefinedInScenario));
      try {
        var header = XmlFactoryHelper.createC2SimHeader(configService.getSystemNameServer());
        var msg = XmlFactoryHelper.createC2SIMInitialization(header, body);
        c2simInitializationAsXml = writeMessageAsString(msg, true, true);
      } catch (LoxException ex) {
        logger.error(
            "Session '{}': Failed to create XML representation of C2SIMInitializationBodyType: {}",
            sessionName,
            ex.getMessage(),
            ex);
        federatesDefinedInScenario = Set.of();
        c2simInitializationAsXml = null;
      }
    } else {
      federatesDefinedInScenario = Set.of();
      federatesInitialized = new HashSet<>();
      c2simInitializationAsXml = null;
      logger.info("Session '{}': C2SIM initialization was cleared (removed).", sessionName);
    }
  }
}
