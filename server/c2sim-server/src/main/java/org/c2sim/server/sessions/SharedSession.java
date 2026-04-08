package org.c2sim.server.sessions;

import static org.c2sim.authorization.impl.AuthorizationResult.OK;
import static org.c2sim.lox.C2SimMsgKind.*;
import static org.c2sim.lox.sax.DetectMsgKind.determineMsgKind;

import com.github.oxo42.stateless4j.delegates.FuncBoolean;
import com.github.oxo42.stateless4j.transitions.Transition;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.c2sim.authorization.interfaces.C2SimAuthorizer;
import org.c2sim.lox.C2SimMsgKind;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.exceptions.ValidationException;
import org.c2sim.lox.helpers.MessageTypeHelper;
import org.c2sim.lox.sax.ExtractC2SimHeader;
import org.c2sim.lox.schema.C2SIMHeaderType;
import org.c2sim.lox.schema.C2SIMInitializationBodyType;
import org.c2sim.lox.validation.LoxXsdValidator;
import org.c2sim.server.api.models.DynamicSessionInfo;
import org.c2sim.server.api.models.RequestJoinSession;
import org.c2sim.server.api.models.SessionInfo;
import org.c2sim.server.exceptions.C2SimException;
import org.c2sim.server.exceptions.SharedSessionExceptionFactory;
import org.c2sim.server.exceptions.XsdValidatorExceptionHelper;
import org.c2sim.server.services.C2SimSchemaService;
import org.c2sim.server.services.ConfigService;
import org.c2sim.server.services.MetricService;
import org.c2sim.server.utils.StateHelper;
import org.c2sim.statemachine.C2SimStateMachine;
import org.c2sim.statemachine.State;
import org.c2sim.statemachine.Trigger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages one shared C2SIM session.
 *
 * <p>A shared session:
 *
 * <ul>
 *   <li>is bound to a single C2SIM XSD schema version
 *   <li>tracks all connected {@link SharedSessionClient}s via a {@link SharedSessionClientManager}
 *   <li>drives the C2SIM protocol state machine ({@link C2SimStateMachine})
 *   <li>validates, routes, and distributes published C2SIM XML documents
 * </ul>
 */
public class SharedSession {

  private static final Logger logger = LoggerFactory.getLogger(SharedSession.class);

  private final String schemaVersion;
  private final String sharedSessionName;
  private final MetricService metricService;
  private final C2SimSchemaService c2SimSchemaService;
  private final SharedSessionClientManager clients;
  private final C2SimStateMachine stateMachine;
  private final C2SimInitializationState initState;
  private final OffsetDateTime created;
  private final String description;
  // Can also solve this with switch, but styling doesn't allow short notation
  private final Map<C2SimMsgKind, MetricService.MetricMsgType> metricTypeLookup =
      Map.ofEntries(
          Map.entry(ORDER, MetricService.MetricMsgType.REPORT),
          Map.entry(REPORT, MetricService.MetricMsgType.REPORT),
          Map.entry(C2SIM_INITIALIZATION, MetricService.MetricMsgType.INIT));
  private String displayName;
  private boolean xsdValidationEnabled;

  /**
   * Creates a new shared session.
   *
   * @param configService the configuration service
   * @param c2simSchemaService the schema service used to validate the schema version and XML
   * @param sharedSessionName the unique session name
   * @param schemaVersion the C2SIM XSD schema version for this session
   * @param displayName the human-readable display name
   * @param description optional description (may be {@code null})
   * @param systemSession {@code true} if this is a system (persistent) session
   * @throws C2SimException if {@code schemaVersion} is not supported
   */
  public SharedSession(
      @NotNull MetricService metricService,
      @NotNull ConfigService configService,
      @NotNull C2SimSchemaService c2simSchemaService,
      @NotNull String sharedSessionName,
      @NotNull String schemaVersion,
      @NotNull String displayName,
      String description,
      boolean systemSession) {
    this.clients = new SharedSessionClientManager(this);
    this.created = OffsetDateTime.now();
    this.metricService = Objects.requireNonNull(metricService, "MetricService is null.");
    this.c2SimSchemaService =
        Objects.requireNonNull(c2simSchemaService, "C2SimSchemaService is null.");
    this.sharedSessionName =
        Objects.requireNonNull(sharedSessionName, "Shared session name is null.");
    this.schemaVersion = Objects.requireNonNull(schemaVersion, "Schema version is null");
    this.displayName = Objects.requireNonNull(displayName, "Display name is null");
    this.description = description != null ? description : "";
    Objects.requireNonNull(configService, "ConfigService is null.");
    this.xsdValidationEnabled = configService.getXsdValidationEnabled();
    this.initState = new C2SimInitializationState(sharedSessionName, configService);

    c2simSchemaService.checkIfSchemaVersionIsSupported(schemaVersion, true);

    stateMachine = new C2SimStateMachine.Builder().listener(buildStateMachineListener()).build();
  }

  // -------------------------------------------------------------------------
  // Publish pipeline
  // -------------------------------------------------------------------------

  /** See {@link #publishC2SimDoc(String, String, InputStream, C2SimAuthorizer)}; no authorizer. */
  public void publishC2SimDoc(String clientId, String trackingId, InputStream xmlDoc) {
    publishC2SimDoc(clientId, trackingId, xmlDoc, null);
  }

  /**
   * Publishes a C2SIM XML document to this session.
   *
   * <p>Processing steps:
   *
   * <ol>
   *   <li>Verify the client has joined the session.
   *   <li>Parse bytes, determine message kind, extract C2SIM header.
   *   <li>Optionally validate the XML against the session's XSD schema.
   *   <li>Authorize the sender (when an authorizer is present).
   *   <li>Dispatch kind-specific handling (state machine, initialization, etc.).
   *   <li>Record metrics.
   *   <li>Distribute the document to all subscribed clients (unless suppressed).
   * </ol>
   *
   * @param clientId the publishing client identifier
   * @param trackingId a correlation identifier for tracing
   * @param xmlDoc the C2SIM XML content as an {@link InputStream}
   * @param authorizer optional sender authorizer ({@code null} to skip authorization)
   * @throws C2SimException if any validation or state rule is violated
   */
  public void publishC2SimDoc(
      String clientId, String trackingId, InputStream xmlDoc, C2SimAuthorizer authorizer)
      throws C2SimException {
    Objects.requireNonNull(clientId, "clientId is null.");
    Objects.requireNonNull(trackingId, "trackingId is null.");
    Objects.requireNonNull(xmlDoc, "xmlDoc is null.");

    var client = clients.getOrCreateClientById(clientId);
    logger.info(
        "Session '{}': Start processing C2SIM XML document '{}' published by C2SIM client '{}'.",
        getSharedSessionName(),
        trackingId,
        client.getClientNameForDebug());

    checkIfJoinedSession(client);
    var ctx = buildMessageContext(xmlDoc);
    validateMessage(ctx);
    if (authorizer != null) {
      checkAuthorizer(authorizer, ctx.header());
    }
    dispatchByKind(ctx);

    metricService.incValidMessagesSendByC2SimClient(
        getSharedSessionName(), ctx.header().getFromSendingSystem(), resolveMetricType(ctx.kind()));

    if (shouldDistribute(ctx.kind())) {
      clients.distributeMessage(client, trackingId, ctx.kind(), ctx.toStream());
    }
  }

  // -------------------------------------------------------------------------
  // Pipeline steps
  // -------------------------------------------------------------------------

  /** Holds the pre-parsed content of an incoming C2SIM message. */
  private record C2SimMessageContext(byte[] xml, C2SimMsgKind kind, C2SIMHeaderType header) {
    ByteArrayInputStream toStream() {
      return new ByteArrayInputStream(xml);
    }
  }

  private C2SimMessageContext buildMessageContext(InputStream xmlDoc) {
    var xml = readXmlBytes(xmlDoc);
    var kind = determineMsgKind(new ByteArrayInputStream(xml));
    if (kind == C2SimMsgKind.MESSAGE_BODY_NOT_WRAPPED) {
      throw new C2SimException(
          C2SimException.ErrorCode.C2SIM_ROOT_ELEMENT_MUST_BE_MESSAGE,
          "The C2SIM message must have MESSAGE as root element.");
    }
    if (kind == C2SimMsgKind.ERROR) {
      logger.warn("C2SIM message type was not recognised in XML message.");
    }
    var header = extractC2SimHeader(new ByteArrayInputStream(xml));
    return new C2SimMessageContext(xml, kind, header);
  }

  private void validateMessage(C2SimMessageContext ctx) {
    boolean forceValidation = (ctx.kind() == C2SIM_INITIALIZATION);
    if (forceValidation || xsdValidationEnabled) {
      c2SimSchemaService.validate(getSchemaVersion(), ctx.toStream());
      try {
        var validator = LoxXsdValidator.doValidation(ctx.toStream());
        if (!validator.isValid()) {
          throw XsdValidatorExceptionHelper.convert(validator);
        }
      } catch (ValidationException ve) {
        throw new C2SimException(
            C2SimException.ErrorCode.XSD_VALIDATION_FAILURE,
            "XSD validation failed: " + ve.getMessage());
      }
    }
  }

  private void dispatchByKind(C2SimMessageContext ctx) {
    if (ctx.kind() == C2SIM_INITIALIZATION) {
      initState.receive(ctx.toStream(), stateMachine.getCurrentState());
    } else if (ctx.kind() == INITIALIZATION_COMPLETE) {
      initState.recordInitializationComplete(ctx.header(), stateMachine.getCurrentState());
    } else if (C2SimStateMachine.isStateMachineMessage(ctx.kind())) {
      handleStateMachineMessage(ctx.kind(), ctx.toStream());
    } else {
      checkIfMessageIsAllowedInState(ctx.kind());
    }
  }

  private boolean shouldDistribute(C2SimMsgKind kind) {
    if (kind == C2SIM_INITIALIZATION && stateMachine.getCurrentState() == State.INITIALIZING) {
      logger.info(
          "Session '{}': Received C2SIMInitialization, distribute this at INITIALIZED state.",
          getSharedSessionName());
      return false;
    }
    return true;
  }

  // -------------------------------------------------------------------------
  // Validation helpers
  // -------------------------------------------------------------------------

  private void checkIfJoinedSession(SharedSessionClient client) {
    if (!client.hasJoinedSharedSession()) {
      throw new C2SimException(
          C2SimException.ErrorCode.CLIENT_NOT_JOINED_SHARED_SESSION,
          String.format(
              "%s has not joined shared session '%s', " + "not allowed to publish C2SIM messages.",
              client.getClientNameForDebug(), getSharedSessionName()),
          new HashMap<>(Map.of(C2SimException.PROP_ACTIVE_SESSION, getSharedSessionName())));
    }
  }

  private void checkAuthorizer(C2SimAuthorizer authorizer, C2SIMHeaderType header) {
    Objects.requireNonNull(authorizer, "authorizer is null.");
    Objects.requireNonNull(header, "header is null.");
    logger.debug("Authorizer -> '{}'.", authorizer);
    var result = authorizer.authorizeFromSendingSystem(header.getFromSendingSystem());
    if (result != OK) {
      logger.info("Sender '{}' not auth ", header.getFromSendingSystem());
    }
  }

  private C2SIMHeaderType extractC2SimHeader(InputStream xmlStream) {
    var header = ExtractC2SimHeader.extract(xmlStream);
    if (header == null) {
      throw new C2SimException(
          C2SimException.ErrorCode.C2SIM_INVALID_HEADER, "No valid C2SIM header in XML");
    }
    return header;
  }

  private void handleStateMachineMessage(C2SimMsgKind kind, ByteArrayInputStream xmlMessage) {
    if (!stateMachine.isC2SimMsgAllowedInCurrentState(kind)) {
      if (kind == C2SimMsgKind.SHARE_SCENARIO && !initState.hasReceivedInitialization()) {
        throw SharedSessionExceptionFactory.createInitializationNotCompleted(
            getSharedSessionName(),
            kind,
            initState.getFederatesDefinedInScenario(),
            initState.getFederatesInitialized());
      }
      var permittedTriggers =
          stateMachine.getPermittedTriggers().stream()
              .map(Enum::name)
              .collect(Collectors.joining(";"));

      throw new C2SimException(
          C2SimException.ErrorCode.STATE_TRANSITION_NOT_ALLOWED,
          String.format(
              "The trigger '%s' is not allowed in state '%s' (permitted triggers '%s').",
              kind, stateMachine.getCurrentState(), permittedTriggers),
          new HashMap<>(
              Map.of(
                  C2SimException.PROP_ACTIVE_SESSION,
                  getSharedSessionName(),
                  C2SimException.PROP_ALLOWED_TRIGGERS,
                  permittedTriggers)));
    }
    try {
      MessageTypeHelper.readMessage(xmlMessage);
    } catch (LoxException e) {
      throw new C2SimException(
          C2SimException.ErrorCode.C2SIM_MSG_DECODING_ERROR,
          String.format("Failed to decode C2SIM XML message: error '%s'", e.getMessage()),
          new HashMap<>(Map.of(C2SimException.PROP_ACTIVE_SESSION, getSharedSessionName())));
    }
    stateMachine.fireTrigger(kind);
  }

  private void checkIfMessageIsAllowedInState(C2SimMsgKind kind) {
    if (!stateMachine.isC2SimMsgAllowedInCurrentState(kind)) {
      throw new C2SimException(
          C2SimException.ErrorCode.C2SIM_MSG_NOT_ALLOWED_IN_STATE,
          String.format(
              "C2SIM message of type '%s' is not allowed in state '%s'",
              kind, stateMachine.getCurrentState()),
          new HashMap<>(Map.of(C2SimException.PROP_CURRENT_STATE, stateMachine.getCurrentState())));
    }
  }

  private byte[] readXmlBytes(InputStream stream) throws C2SimException {
    try {
      return stream.readAllBytes();
    } catch (IOException ex) {
      throw new C2SimException(
          C2SimException.ErrorCode.IO_ERROR,
          "Failed to get content C2SIM message from REST request");
    }
  }

  private MetricService.MetricMsgType resolveMetricType(C2SimMsgKind kind) {
    return metricTypeLookup.getOrDefault(kind, MetricService.MetricMsgType.OTHER);
  }

  // -------------------------------------------------------------------------
  // State machine listener (replaces inner class)
  // -------------------------------------------------------------------------

  private C2SimStateMachine.StateMachineListener buildStateMachineListener() {
    return new C2SimStateMachine.StateMachineListener() {
      @Override
      public void onEnterStateUninitialized(Transition<State, Trigger> transition) {
        initState.clear();
        logTransition(transition);
      }

      @Override
      public void onEnterStateInitializing(Transition<State, Trigger> transition) {
        logTransition(transition);
      }

      @Override
      public void onEnterStateInitialized(Transition<State, Trigger> transition) {
        logTransition(transition);
        if (transition.getSource() == State.INITIALIZING) {
          distributeC2SimInit();
        }
      }

      @Override
      public void onEnterStateRunning(Transition<State, Trigger> transition) {
        logTransition(transition);
      }

      @Override
      public void onEnterStatePaused(Transition<State, Trigger> transition) {
        logTransition(transition);
      }

      @Override
      public FuncBoolean allSystemsAreInitialized() {
        return initState::hasReceivedInitialization;
      }
    };
  }

  private void logTransition(Transition<State, Trigger> transition) {
    logger.info(
        "Session '{}': Trigger '{}' triggered state transition from '{}' to '{}' "
            + "(state machine notification).",
        getSharedSessionName(),
        transition.getTrigger(),
        transition.getSource(),
        transition.getDestination());
    logger.info(
        "Session '{}': Current shared session state '{}'.",
        getSharedSessionName(),
        getCurrentState());
  }

  private void distributeC2SimInit() {
    logger.info(
        "Session '{}': Distribute C2SIMInitialization to C2SIM clients.", getSharedSessionName());
    clients.distributeMessage(null, "c2siminit", C2SIM_INITIALIZATION, initState.getAsXmlStream());
  }

  // -------------------------------------------------------------------------
  // Session operations
  // -------------------------------------------------------------------------

  /**
   * Deletes this session.
   *
   * <p>Not yet fully implemented; always throws {@link C2SimException} with {@link
   * C2SimException.ErrorCode#NOT_IMPLEMENTED}.
   *
   * @param requestClientId the client requesting the deletion
   * @param force {@code true} to force deletion even if clients are connected
   */
  public void deleteSession(String requestClientId, Boolean force) {
    throw new C2SimException(
        C2SimException.ErrorCode.NOT_IMPLEMENTED, "Delete session not supported yet (demo 3)");
  }

  /**
   * Returns the existing {@link SharedSessionClient} for the given ID or creates a new one.
   *
   * @param clientId the client identifier
   * @return the client (never {@code null})
   */
  public SharedSessionClient getOrCreateClientById(String clientId) {
    return clients.getOrCreateClientById(clientId);
  }

  /**
   * Returns the {@link SharedSessionClientManager} for this session.
   *
   * @return the client manager
   */
  public SharedSessionClientManager getClientsManager() {
    return clients;
  }

  /**
   * Processes a client's request to join this session.
   *
   * <p>If the client has already joined, the request is logged and ignored. Otherwise the client's
   * display name and system name are updated and it is marked as joined.
   *
   * @param clientId the client identifier
   * @param trackingId a correlation identifier for tracing
   * @param requestJoinSession the join request body
   */
  public void joinSharedSession(
      @NotNull String clientId, String trackingId, @NotNull RequestJoinSession requestJoinSession) {
    var sessionClient = getOrCreateClientById(clientId);

    if (sessionClient.hasJoinedSharedSession()) {
      logger.warn(
          "Session '{}': Joining request '{}' for shared session '{}' "
              + "ignored for {} (is already joined).').",
          getSharedSessionName(),
          trackingId,
          getSharedSessionName(),
          sessionClient.getClientNameForDebug());
      return;
    }
    sessionClient.setClientIdDisplayName(requestJoinSession.getClientIdDisplayName());
    sessionClient.setSystemName(
        Objects.requireNonNullElse(requestJoinSession.getSystemName(), "UNKNOWN"));
    sessionClient.setHasJoinedSharedSession(true);
    logger.info(
        "Session '{}': Joining request '{}' for shared session '{}' granted for {}.",
        getSharedSessionName(),
        trackingId,
        getSharedSessionName(),
        sessionClient.getClientNameForDebug());
    clients.logC2SimClientsInfo();
  }

  /**
   * Removes the client from this session and closes any open streaming connection.
   *
   * @param clientId the client identifier
   * @param trackingId a correlation identifier for tracing
   * @param reason the reason for resigning
   */
  public void resignSession(
      @NotNull String clientId, @NotNull String trackingId, @NotNull String reason) {
    clients.resignClient(clientId, trackingId, reason);
  }

  // -------------------------------------------------------------------------
  // Accessors (delegating to initState where appropriate)
  // -------------------------------------------------------------------------

  /**
   * Returns whether XSD validation is enabled for this session.
   *
   * @return {@code true} if XSD validation is active
   */
  public boolean getIsXsdValidationEnabled() {
    return xsdValidationEnabled;
  }

  /**
   * Enables or disables XSD validation for this session.
   *
   * @param value {@code true} to enable, {@code false} to disable
   */
  public void setIsXsdValidationEnabled(boolean value) {
    xsdValidationEnabled = value;
  }

  /**
   * Returns the unique name of this shared session.
   *
   * @return the session name
   */
  public String getSharedSessionName() {
    return sharedSessionName;
  }

  /**
   * Returns the C2SIM XSD schema version used by this session.
   *
   * @return the schema version string
   */
  public String getSchemaVersion() {
    return schemaVersion;
  }

  /**
   * Returns the human-readable display name for this session.
   *
   * @return the display name
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Sets the human-readable display name for this session.
   *
   * @param name the new display name
   */
  public void setDisplayName(String name) {
    displayName = name;
  }

  /**
   * Creates a {@link DynamicSessionInfo} snapshot of this session's current state.
   *
   * @return a dynamic session info object
   */
  public DynamicSessionInfo createDynamicInfo() {
    return new DynamicSessionInfo(
        getSharedSessionName(),
        created,
        new SessionInfo(getSchemaVersion(), getDisplayName(), description),
        StateHelper.convert(getCurrentState()));
  }

  /**
   * Returns the parsed C2SIM initialization body, or {@code null} if none has been received.
   *
   * @return the initialization body, or {@code null}
   */
  public C2SIMInitializationBodyType getC2SIMInitialization() {
    return initState.getC2SIMInitialization();
  }

  /**
   * Returns the cached C2SIM initialization XML (with server header) as a string.
   *
   * @return the XML string
   * @throws C2SimException with {@link C2SimException.ErrorCode#NO_C2SIM_INITIALIZATION_BODY} if
   *     the session has not yet been initialized or no initialization has been received
   */
  public String getC2SIMInitializationAsTextXml() {
    return initState.getAsXml(stateMachine.getCurrentState());
  }

  /**
   * Returns the current state-machine state of this session.
   *
   * @return the current {@link State}
   */
  public State getCurrentState() {
    return stateMachine.getCurrentState();
  }

  /**
   * Returns {@code true} when a C2SIM initialization body has been received.
   *
   * <p>Note: this checks that an initialization document was received, not that all individual
   * federates have reported Initialization Complete. Use {@link #getFederatesInitialized()} and
   * {@link #getFederatesDefinedInScenario()} to assess per-federate readiness.
   *
   * @return {@code true} if an initialization body is present
   */
  public boolean areAllSystemsReady() {
    return initState.hasReceivedInitialization();
  }

  /**
   * Returns the set of system names that have sent an Initialization Complete message.
   *
   * @return the set of initialized federate names
   */
  public Set<String> getFederatesInitialized() {
    return initState.getFederatesInitialized();
  }

  /**
   * Returns the set of federate system names that are required to initialize (as defined in the
   * C2SIM initialization document).
   *
   * @return the set of required federate names
   */
  public Set<String> getFederatesDefinedInScenario() {
    return initState.getFederatesDefinedInScenario();
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return """
            Shared Session info:
            - Shared session id: '%s'
            - Display name: '%s'
            - Description: '%s'
            - C2SIM Schema: '%s'
            - Schema validation enabled: '%s'
            - State: %s
            """
        .formatted(
            getSharedSessionName(),
            getDisplayName(),
            description,
            getSchemaVersion(),
            xsdValidationEnabled,
            getCurrentState());
  }
}
