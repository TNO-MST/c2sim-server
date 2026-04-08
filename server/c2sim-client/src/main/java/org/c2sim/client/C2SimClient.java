package org.c2sim.client;

import static com.aventrix.jnanoid.jnanoid.NanoIdUtils.DEFAULT_NUMBER_GENERATOR;
import static com.aventrix.jnanoid.jnanoid.NanoIdUtils.randomNanoId;
import static org.c2sim.client.helpers.StringHelper.isNullOrEmpty;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.c2sim.client.api.NotificationsApi;
import org.c2sim.client.api.PublishApi;
import org.c2sim.client.api.SessionApi;
import org.c2sim.client.exceptions.C2ClientException;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.helpers.ExceptionHelper;
import org.c2sim.client.helpers.MessageQueue;
import org.c2sim.client.invoker.ApiClient;
import org.c2sim.client.invoker.ApiException;
import org.c2sim.client.model.*;
import org.c2sim.client.security.AuthInterceptor;
import org.c2sim.client.security.OidcTokenProvider;
import org.c2sim.client.websockets.OkHttpWebSocketManager;
import org.c2sim.client.websockets.StreamMsgSplitter;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.exceptions.ValidationException;
import org.c2sim.lox.helpers.MessageTypeHelper;
import org.c2sim.lox.helpers.XmlFactoryHelper;
import org.c2sim.lox.schema.C2SIMHeaderType;
import org.c2sim.lox.schema.C2SIMInitializationBodyType;
import org.c2sim.lox.schema.MessageType;
import org.c2sim.lox.validation.LoxXsdValidator;
import org.c2sim.statemachine.C2SimStateMachine;
import org.c2sim.statemachine.Trigger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * High-level C2SIM client that manages the full lifecycle of a C2SIM shared session.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Creates or reuses a shared session on the C2SIM server.
 *   <li>Connects to the server-sent event / WebSocket streaming endpoint and automatically
 *       reconnects on failure.
 *   <li>Joins the shared session so the server starts forwarding messages.
 *   <li>Processes incoming XML messages (optional XSD validation and JAXB decoding) through a
 *       bounded {@link MessageQueue}.
 *   <li>Publishes C2SIM XML documents to the server (with optional validation and pretty-printing).
 *   <li>Sends state-machine triggers via {@link #sendTrigger(Trigger)}.
 * </ul>
 *
 * <p>Obtain an instance through the fluent {@link Builder}:
 *
 * <pre>{@code
 * C2SimClient client = C2SimClient.create()
 *     .url("http://localhost:8080")
 *     .systemName("MySim")
 *     .build();
 * client.connect();
 * }</pre>
 */
public class C2SimClient {

  /** Letter used when generating an identifier */
  protected static final char[] DEFAULT_LETTERS =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

  private static final String DEFAULT_SHARED_SESSION_NAME = "default";

  private static final Logger logger = LoggerFactory.getLogger(C2SimClient.class);

  private final OidcTokenProvider oidcProvider;
  private final String basePathUrl;
  private final String clientId;
  private final ApiClient apiClient;
  private final SessionApi sessionApi;
  private final PublishApi publishApi;
  private final NotificationsApi notificationApi;
  private final StreamMsgSplitter streamMsgSplitter; // Split the XML stream on /n
  private final MessageQueue
      messageQueue; // Don't block client when receiving msg (validation / decode in thread)
  private final String clientIdDisplayName;
  private C2SimClientListener c2simClientListener;
  private final String systemName;
  private final boolean sendMsgValidationEnabled;
  private final boolean receivedMsgDecodingEnabled;
  private final boolean receivedMsgValidationEnabled;
  private final boolean beautifySendEnabled;
  private static final boolean CREATE_SESSION_ENABLED = true;
  private String sharedSessionName = DEFAULT_SHARED_SESSION_NAME;
  private OkHttpWebSocketManager wsManager;
  private StateType c2SimServerState = StateType.UNINITIALIZED;
  private boolean hasJoinedSharedSession = false;
  private boolean hasStreamToSharedSession = false;
  private SharedSessionInfoProvider createSharedSessionProvider;
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  /**
   * Returns a new {@link Builder} for constructing a {@code C2SimClient}.
   *
   * @return a new builder instance
   */
  public static Builder create() {
    return new C2SimClient.Builder();
  }

  private C2SimClient(C2SimClient.Builder builder) {

    var httpClientBuilder = new OkHttpClient.Builder();
    // Add OIDC to interceptor loop if defined
    if (builder.oidcProvider != null) {
      httpClientBuilder.addInterceptor(new AuthInterceptor(builder.oidcProvider));
    }
    var httpClient = httpClientBuilder.build();
    if ((builder.sharedSessionName != null) && (!sharedSessionName.isEmpty())) {
      this.sharedSessionName = builder.sharedSessionName;
    }
    this.oidcProvider = builder.oidcProvider;
    this.basePathUrl = builder.url;
    this.beautifySendEnabled = builder.beautifySendEnabled;
    this.sendMsgValidationEnabled = builder.sendMsgValidationEnabled;
    this.receivedMsgDecodingEnabled = builder.receivedMsgDecodingEnabled;
    this.receivedMsgValidationEnabled = builder.receivedMsgValidationEnabled;
    this.systemName = builder.systemName;
    this.apiClient = new ApiClient(httpClient);
    this.apiClient.setBasePath(builder.url);
    this.sessionApi = new SessionApi(apiClient);
    this.publishApi = new PublishApi(apiClient);
    this.notificationApi = new NotificationsApi(apiClient);
    this.streamMsgSplitter = new StreamMsgSplitter(this::handleSingleXmlMessage);
    this.sharedSessionName =
        isNullOrEmpty(sharedSessionName) ? DEFAULT_SHARED_SESSION_NAME : sharedSessionName;
    this.c2simClientListener = builder.listener;
    this.messageQueue =
        new MessageQueue(
            this,
            builder.receivedMsgDecodingEnabled,
            builder.receivedMsgValidationEnabled,
            this::beforeSendToClient);
    this.clientId =
        isNullOrEmpty(builder.clientId)
            ? randomNanoId(DEFAULT_NUMBER_GENERATOR, DEFAULT_LETTERS, 5)
            : builder.clientId;
    this.clientIdDisplayName =
        isNullOrEmpty(builder.clientDisplayName) ? clientId : builder.clientDisplayName;
  }

  /**
   * Returns the human-readable display name of this client, used in log messages.
   *
   * @return the client display name
   */
  public String getClientIdDisplayName() {
    return this.clientIdDisplayName;
  }

  /**
   * Returns the unique client identifier.
   *
   * @return the client ID
   */
  public String getClientId() {
    return this.clientId;
  }

  /**
   * Returns the C2SIM system name registered when joining the shared session.
   *
   * @return the system name
   */
  public String getSystemName() {
    return this.systemName;
  }

  /**
   * Returns the name of the shared session this client is targeting.
   *
   * @return the shared session name
   */
  public String getSharedSessionName() {
    return sharedSessionName;
  }

  /**
   * Returns whether JAXB decoding of received C2SIM XML messages is enabled.
   *
   * <p>When this option is enabled, incoming C2SIM message XML is automatically converted
   * (unmarshalled) into corresponding {@code MessageType} POJO instances. When disabled, the raw
   * XML payload is delivered as-is without object conversion. Uses the XSD embedded in the library
   *
   * @return {@code true} if received C2SIM messages should be decoded into POJOs, {@code false} to
   *     provide raw XML only
   */
  public boolean isReceivedMsgDecodeEnabled() {
    return this.receivedMsgDecodingEnabled;
  }

  /**
   * Returns whether XSD validation of received C2SIM XML messages is enabled.
   *
   * <p>When enabled, incoming XML messages are validated against the C2SIM XML Schema Definition
   * (XSD) before being further processed. Messages that do not conform to the schema will trigger a
   * validation error. When disabled, the client skips schema validation and accepts the raw XML
   * as-is. The C2SIM server does XSD validation before sending.
   *
   * @return {@code true} if received C2SIM XML should be validated against the XSD, {@code false}
   *     to skip validation
   */
  public boolean isReceivedMsgValidationEnabled() {
    return this.receivedMsgValidationEnabled;
  }

  /**
   * Returns whether XSD validation is enabled for outgoing C2SIM XML messages.
   *
   * <p>When this option is enabled, the client validates the XML message against the C2SIM XML
   * Schema Definition (XSD) before sending it. This ensures that the outgoing XML is structurally
   * correct and contains all required (mandatory) fields as defined by the schema. If the XML does
   * not conform to the XSD—such as when mandatory elements are missing or the structure is
   * invalid—the message will not be sent and a validation error will be raised.
   *
   * @return {@code true} if outgoing XML should be validated against the XSD before sending, {@code
   *     false} to skip validation
   */
  public boolean isSendMsgValidationEnabled() {
    return this.sendMsgValidationEnabled;
  }

  /**
   * Returns whether pretty-printing is enabled for outgoing C2SIM XML.
   *
   * <p>When enabled, the client formats the XML with indentation and line breaks before sending it,
   * making it more readable for logging or debugging. When disabled, the XML is sent as a single
   * unformatted line.
   *
   * @return {@code true} if outgoing XML should be beautified, {@code false} to send it as a
   *     single-line raw XML string
   */
  public boolean isBeautifySendEnabled() {
    return this.beautifySendEnabled;
  }

  /**
   * Replaces the current {@link C2SimClientListener}.
   *
   * @param listener the new listener; must not be {@code null} — use {@link
   *     C2SimClientListener#DEFAULT} for a no-op implementation
   * @throws IllegalArgumentException if {@code listener} is {@code null}
   */
  public void setC2simClientListener(C2SimClientListener listener) {
    if (listener == null) {
      throw new IllegalArgumentException(
          "Listener not allowed to be used, " + "use C2SimClientListener.DEFAULT");
    }
    c2simClientListener = listener;
  }

  /**
   * Returns the configured {@link OidcTokenProvider} used for obtaining an OpenID Connect (OIDC)
   * access token.
   *
   * <p>The access token provided by this component is included as a Bearer token in outbound HTTP
   * requests to authenticate the client with the C2SIM server. If no provider is configured, the
   * client operates without OIDC-based authentication.
   *
   * @return the {@code OidcTokenProvider} responsible for supplying OIDC access tokens, or {@code
   *     null} if none is configured
   */
  public OidcTokenProvider getOidcTokenProvider() {
    return this.oidcProvider;
  }

  /**
   * Returns the {@link C2SimClientListener} associated with this client.
   *
   * <p>The listener provides the callback mechanism used to deliver client notifications, such as
   * connection events, incoming messages, errors, or other lifecycle-related updates.
   * Implementations can use this callback to react to server activity or integrate C2SIM events
   * into the application's workflow.
   *
   * @return the configured {@code C2SimClientListener} instance
   */
  public C2SimClientListener getC2SimClientListener() {
    return c2simClientListener;
  }

  /**
   * Retrieves the list of all shared sessions from the C2SIM server.
   *
   * @return the list of dynamic session info objects
   * @throws ApiException on HTTP or network errors
   * @throws C2SimRestException if the server returns a structured C2SIM error
   */
  public List<DynamicSessionInfo> getSharedSessionsFromC2SimServer()
      throws ApiException, C2SimRestException {
    return ExceptionHelper.executeWithExceptionWrapping(() -> sessionApi.getSessions(clientId));
  }

  /**
   * Retrieves detailed info for the given shared session from the C2SIM server.
   *
   * @param sessionId the session identifier
   * @return the session info
   * @throws ApiException on HTTP or network errors
   * @throws C2SimRestException if the server returns a structured C2SIM error
   */
  public DynamicSessionInfo getSessionInfoFromC2SimServer(String sessionId)
      throws ApiException, C2SimRestException {
    return ExceptionHelper.executeWithExceptionWrapping(
        () -> sessionApi.getSessionInfo(clientId, sessionId));
  }

  /**
   * Retrieves the streaming endpoints (WebSocket URL, etc.) for the given session.
   *
   * @param sessionId the session identifier
   * @return the available stream endpoints
   * @throws ApiException on HTTP or network errors
   * @throws C2SimRestException if the server returns a structured C2SIM error
   */
  public ResponseStreamEndpoints getStreamEndpointsFromC2SimServer(@NotNull String sessionId)
      throws ApiException, C2SimRestException {
    return ExceptionHelper.executeWithExceptionWrapping(
        () -> notificationApi.getStreamEndpoints(clientId, sessionId));
  }

  /**
   * Retrieves the streaming endpoints for the currently active shared session.
   *
   * @return the available stream endpoints
   * @throws ApiException on HTTP or network errors
   * @throws C2SimRestException if the server returns a structured C2SIM error
   */
  public ResponseStreamEndpoints getActiveStreamEndpointsFromC2SimServer()
      throws ApiException, C2SimRestException {
    return getStreamEndpointsFromC2SimServer(sharedSessionName);
  }

  /**
   * Returns the initialization XML for the given session.
   *
   * @param sessionId the session identifier
   * @return the initialization XML string
   * @throws ApiException on HTTP or network errors
   * @throws C2SimRestException if the server returns a structured C2SIM error
   */
  public String getSessionInitializationFromC2SimServer(@NotNull String sessionId)
      throws ApiException, C2SimRestException {
    return ExceptionHelper.executeWithExceptionWrapping(
        () -> sessionApi.getSessionInitialization(clientId, sessionId));
  }

  /**
   * Returns the initialization XML for the currently active shared session.
   *
   * @return the initialization XML string
   * @throws ApiException on HTTP or network errors
   * @throws C2SimRestException if the server returns a structured C2SIM error
   */
  public String getSessionInitializationFromC2SimServer() throws C2SimRestException, ApiException {
    return getSessionInitializationFromC2SimServer(sharedSessionName);
  }

  /**
   * Serializes and publishes a {@link MessageType} to the active shared session.
   *
   * @param message the C2SIM message to publish
   * @throws C2SimRestException if the server returns a structured C2SIM error
   * @throws ApiException on HTTP or network errors
   * @throws LoxException if the message cannot be serialized
   * @throws ValidationException if send-message validation is enabled and the XML is invalid
   */
  public void publishC2SimDocument(MessageType message)
      throws C2SimRestException, ApiException, LoxException, ValidationException {
    publishC2SimDocument(MessageTypeHelper.writeMessage(message, true, beautifySendEnabled));
  }

  /**
   * Publishes raw C2SIM XML to the active shared session.
   *
   * <p>If send-message validation is enabled ({@link Builder#enableSendMessageValidation()}), the
   * XML is validated against the LOX XSD schema before publishing.
   *
   * @param xml the C2SIM XML string (root element must be Message)
   * @throws C2SimRestException if the server returns a structured C2SIM error
   * @throws ApiException on HTTP or network errors
   * @throws ValidationException if send-message validation is enabled and the XML is invalid
   */
  public void publishC2SimDocument(String xml)
      throws C2SimRestException, ApiException, ValidationException {
    Objects.requireNonNull(xml, "xml cannot be null");

    try {
      if (sendMsgValidationEnabled) {
        // Check against XSD schema
        var validation = LoxXsdValidator.doValidation(xml);
        if (!validation.isValid()) {
          throw new ValidationException(validation);
        }
      }
      // TODO Not write to file, use in memory?
      @SuppressWarnings(
          "java:S5443") // Make sure publicly writable directories are used safely here
      Path tmpFile = Files.createTempFile("c2sim-", ".xml");
      Files.writeString(tmpFile, xml);
      var response =
          ExceptionHelper.executeWithExceptionWrapping(
              () -> publishApi.send(clientId, sharedSessionName, tmpFile.toFile()));

      logger.debug(
          "{}Published C2SIM XML document '{}' on C2SIM server.",
          getDebugPrefix(),
          response != null && response.getTrackingId() != null ? response.getTrackingId() : "-");
    } catch (IOException io) {
      throw new ApiException("IO Exception: " + io.getMessage());
    }
  }

  /**
   * Optionally validates, then publishes the given XML.
   *
   * @param xml the C2SIM XML string
   * @param doXsdValidation {@code true} to validate against the LOX XSD schema before publishing
   * @return the {@link LoxXsdValidator} result if validation was performed and the XML is invalid;
   *     {@code null} if the document was successfully published
   * @throws ValidationException if the XML cannot be parsed for validation
   * @throws C2SimRestException if the server returns a structured C2SIM error
   * @throws ApiException on HTTP or network errors
   */
  public LoxXsdValidator publishC2SimDocument(String xml, boolean doXsdValidation)
      throws ValidationException, C2SimRestException, ApiException {
    if (doXsdValidation) {
      var validation = LoxXsdValidator.doValidation(xml);
      if (!validation.isValid()) {
        return validation;
      }
    }
    publishC2SimDocument(xml);
    return null;
  }

  /**
   * Synchronously connects to the C2SIM server: creates the session if needed, establishes the
   * WebSocket stream, and joins the shared session.
   *
   * @throws C2SimRestException if the server returns a structured C2SIM error
   * @throws ApiException on HTTP or network errors
   * @throws C2ClientException if the WebSocket URL returned by the server is invalid
   */
  public void connect() throws C2SimRestException, ApiException, C2ClientException {
    joinSharedSession();
  }

  /** Asynchronously connects to the C2SIM server, retrying every 5 seconds on failure. */
  public void connectAsync() {
    scheduler.execute(this::attemptConnection);
  }

  private void attemptConnection() {
    try {
      joinSharedSession();
    } catch (Exception e) {
      logger.error("{}Failed to join to C2SIM server: {}", getDebugPrefix(), e.getMessage(), e);
      var retry = c2simClientListener.onJoinFailed(this, e);
      if (retry) {
        logger.debug("{}Schedule retry join process in 5 sec.", getDebugPrefix());
        scheduler.schedule(this::attemptConnection, 5, TimeUnit.SECONDS);
      } else {
        logger.debug("{}Aborted joining process", getDebugPrefix());
      }
    }
  }

  /**
   * Disconnects from the C2SIM server: stops the WebSocket and resigns from the shared session.
   *
   * @throws C2SimRestException if the server returns a structured C2SIM error
   * @throws ApiException on HTTP or network errors
   */
  public void resignAndDisconnect() throws C2SimRestException, ApiException {

    if (hasJoinedSharedSession) {
      logger.debug(
          "C2SIM Client '{}({})' initiate resign Shared Session '{}' "
              + "as SYSTEM '{}' with clientId '{}({})' (this disconnect WebSocket)",
          clientIdDisplayName,
          clientId,
          sharedSessionName,
          systemName,
          clientId,
          clientIdDisplayName);
      if (wsManager != null) {
        wsManager.stop();
        wsManager = null;
      }
      ExceptionHelper.executeWithExceptionWrapping(
          () ->
              sessionApi.resignFromSession(
                  clientId, sharedSessionName, new RequestResignSession().reason("Normal resign")));
    }
    c2simClientListener.onResigned(this);
    hasJoinedSharedSession = false;
    hasStreamToSharedSession = false;
  }

  /**
   * Changes the target shared session name.
   *
   * <p>Throws {@link C2ClientException} if the client is already connected, because changing the
   * session while connected is not allowed.
   *
   * @param name the new shared session name; {@code null} resets to the default
   * @throws C2ClientException if already joined or streaming to a session
   */
  public void setSharedSessionName(String name) throws C2ClientException {
    if (name == null) {
      name = DEFAULT_SHARED_SESSION_NAME;
    }
    if (!name.contentEquals(sharedSessionName)) {
      if (hasJoinedSharedSession || hasStreamToSharedSession) {
        var sharedSessionChanged =
            new C2ClientException(
                C2ClientException.ErrorCode.SHARED_SESSION_NAME_CHANGED_WHILE_CONNECTED,
                String.format(
                    "Already connected to C2SIm server, "
                        + "cannot change from Shared Session '%s' to '%s'.",
                    sharedSessionName, name));
        logger.error(sharedSessionChanged.getLogError());
        throw sharedSessionChanged;
      }
      logger.debug("Change from Shared Session '{}' to '{}'.", sharedSessionName, name);
      sharedSessionName = name;
    }
  }

  /**
   * Registers the application callback that receives processed incoming C2SIM messages.
   *
   * @param handler the consumer invoked for each {@link MessageQueue.C2SimMessage}
   */
  public void onReceivedMessage(java.util.function.Consumer<MessageQueue.C2SimMessage> handler) {
    // Forward to consumer
    messageQueue.onReceivedMessage(handler);
  }

  private void syncStateMachine() {
    if (!isJoined()) {
      // Need state only when joined
      return;
    }
    try {
      var session = getSessionInfoFromC2SimServer(sharedSessionName);
      setState(session.getState());
    } catch (ApiException | C2SimRestException ex) {
      // TODO retry....
    }
  }

  // If the C2SIM REST server cannot be reached an exception is thrown
  // Once the C2SIM REST responds a background thread is started to keep WebSocket stream alive
  private void joinSharedSession() throws C2SimRestException, ApiException, C2ClientException {
    logger.debug("{}Initiate joining shared session '{}'.", getDebugPrefix(), sharedSessionName);
    if (CREATE_SESSION_ENABLED) {
      createSharedSession();
    }

    logger.debug(
        "{}Requesting streaming end point (WebSocket) from C2SIM server (REST).", getDebugPrefix());
    var streamingEndPoints =
        getActiveStreamEndpointsFromC2SimServer(); // throws exception when session not exist
    var webSocketUrl = streamingEndPoints.getWebsocket().getUrl();
    logger.debug("{}WebSocket URL is '{}' ", getDebugPrefix(), webSocketUrl);
    // Validate the WebSocket url
    try {
      URI.create(webSocketUrl);
    } catch (Exception ex) {
      var inValidUrl =
          new C2ClientException(
              C2ClientException.ErrorCode.STREAMING_CONNECT_ERROR,
              String.format(
                  "The C2SIM server returned and invalid WebSocket URL '%s'", webSocketUrl));
      logger.error(inValidUrl.getLogError());
      throw inValidUrl;
    }

    logger.debug("{}Connecting to C2SIM streaming endpoint '{}'.", getDebugPrefix(), webSocketUrl);
    logger.debug(
        "Note: A streaming connection with C2SIM-SEVER is established, "
            + "the server only streams data when the C2SIM client has joined the shared session.");
    // Setup WebSocket manager in separate thread
    wsManager =
        OkHttpWebSocketManager.newBuilder()
            .client(apiClient.getHttpClient())
            .url(webSocketUrl)
            .listener(new C2SimSocketListener()) // Callback
            .initialBackoffMs(500) // 0.5, 1, 2, 4, 8, 16, 30 sec (retry interval)
            .maxBackoffMs(30_000)
            .multiplier(2.0)
            .pingInterval(Duration.ofSeconds(30))
            .build();

    wsManager.start(); // Running in separate thread....

    logger.debug(
        "{}Initiate join Shared Session '{}' " + "as SYSTEM '{}'",
        getDebugPrefix(),
        sharedSessionName,
        systemName);
    var response =
        ExceptionHelper.executeWithExceptionWrapping(
            () ->
                sessionApi.joinSession(
                    clientId,
                    sharedSessionName,
                    new RequestJoinSession()
                        .systemName(systemName)
                        .clientIdDisplayName(clientIdDisplayName)));
    var info = response.getSession();

    hasJoinedSharedSession = true;

    // Notify client that has joined
    c2simClientListener.onJoined(this, info);

    logger.debug("{}Joined Shared Session '{}' successfully.", getDebugPrefix(), sharedSessionName);
    logger.debug(
        "{}C2SIM server shared session '{}' information:\n- Name: {}"
            + "\n- Description: {} \n- Schema: {}\n- Current state: {}\n",
        getDebugPrefix(),
        sharedSessionName,
        info.getInfo().getDisplayName(),
        info.getInfo().getDescription(),
        info.getInfo().getC2simSchemaVersion(),
        info.getState().getValue());
    // Initial state
    c2SimServerState = info.getState();
    c2simClientListener.onStateChanged(C2SimClient.this, c2SimServerState, c2SimServerState);

    // Check for late join
    if ((this.c2SimServerState == StateType.UNINITIALIZED)
        || (c2SimServerState == StateType.INITIALIZING)) {
      logger.debug(
          "{}C2SIM Server is in initializing phase ({}), no late join needed.",
          getDebugPrefix(),
          c2SimServerState);
    } else {
      logger.debug(
          "{}C2SIM server in state '{}', initiate late join, request C2Initialization body from C2SIM server.",
          getDebugPrefix(),
          c2SimServerState);
      var initialization = getSessionInitializationFromC2SimServer();
      if ((initialization != null) && (!initialization.isBlank())) {
        handleC2SIMInitialization(initialization);
      } else {
        var initEmpty =
            new C2ClientException(
                C2ClientException.ErrorCode.EMPTY_INITIALIZATION_BODY,
                String.format(
                    "C2SIM server state is %s (is initialised), "
                        + "but C2SIM server returned an empty C2SIM Initialization body.",
                    c2SimServerState));
        logger.error(initEmpty.getLogError());
        throw initEmpty;
      }
    }
  }

  private void setState(StateType state) {
    if (state != c2SimServerState) {
      var currentState = c2SimServerState;
      logger.debug(
          "{}}State changed from '{}' to '{}'. ", getDebugPrefix(), c2SimServerState, state);
      c2SimServerState = state;
      c2simClientListener.onStateChanged(C2SimClient.this, currentState, c2SimServerState);
    }
  }

  /**
   * Returns the base URL of the C2SIM server REST API.
   *
   * @return the base path URL string
   */
  public String getBasePathUrl() {
    return basePathUrl;
  }

  /**
   * Returns {@code true} if this client has successfully joined a shared session.
   *
   * @return {@code true} when joined
   */
  public boolean isJoined() {
    return hasJoinedSharedSession;
  }

  /**
   * Returns {@code true} if the WebSocket stream to the shared session is active.
   *
   * @return {@code true} when the stream is connected
   */
  public boolean hasStreamToSharedSession() {
    return hasStreamToSharedSession;
  }

  /**
   * Returns the last known C2SIM server state (cached; not a live query).
   *
   * @return the cached server state
   */
  public StateType getCachedC2SimServerState() {
    return c2SimServerState;
  }

  /**
   * Registers a provider that supplies the {@link RequestCreateSession} data when the client needs
   * to create a new shared session.
   *
   * @param provider the shared-session info provider
   */
  public void whenCreatingSharedSession(SharedSessionInfoProvider provider) {
    this.createSharedSessionProvider = provider;
  }

  private void createSharedSession() throws C2SimRestException, ApiException, C2ClientException {
    logger.debug(
        "{}Check if shared session '{}' exist on C2SIM server",
        getDebugPrefix(),
        sharedSessionName);
    var allSessions = getSharedSessionsFromC2SimServer();
    var session =
        allSessions.stream()
            .filter(s -> sharedSessionName.equalsIgnoreCase(s.getSessionName()))
            .findFirst();

    if (session.isEmpty()) {

      if (createSharedSessionProvider != null) {
        var sessionInfo = createSharedSessionProvider.provideSharedSessionInfo();
        logger.debug(
            """
                        {}Creating Shared Session '{}' with:
                         \
                        -Display name: {}
                        -Description: {}
                        - Schema version: {}
                        \s""",
            getDebugPrefix(),
            sharedSessionName,
            sessionInfo.getData().getDisplayName(),
            sessionInfo.getData().getDescription(),
            sessionInfo.getData().getC2simSchemaVersion());
        var response =
            ExceptionHelper.executeWithExceptionWrapping(
                () -> sessionApi.createSession(clientId, sharedSessionName, sessionInfo));
        logger.debug(
            "{}Shared session '{}' is created on C2SIM server.",
            getDebugPrefix(),
            response.getSession().getSessionName());

      } else {
        var noProviderException =
            new C2ClientException(
                C2ClientException.ErrorCode.NO_SHARED_SESSION_PROVIDER,
                "No provider attached, use method whenCreatingSharedSession");
        logger.error(noProviderException.getLogError());
        throw noProviderException;
      }
    } else {
      logger.debug(
          "{}Shared session '{}' already exist on C2SIM server (schema version is '{}').",
          getDebugPrefix(),
          sharedSessionName,
          session.get().getInfo().getC2simSchemaVersion());
    }
  }

  /*
  Single XML message received from web socket
   */
  private void handleSingleXmlMessage(CharSequence xmlMsg) {
    if ((xmlMsg != null) && (!xmlMsg.isEmpty())) {
      try {

        messageQueue.publish(xmlMsg.toString());
      } catch (InterruptedException e) {
        logger.error("{}Failed to queue message: {} ", getDebugPrefix(), e.getMessage(), e);
        Thread.currentThread().interrupt(); // always reset interrupt flag
      }
    }
  }

  /**
   * Sends a C2SIM state-machine trigger to the server and synchronises the cached state.
   *
   * @param trigger the trigger to send
   * @throws ValidationException if send-message validation is enabled and the message is invalid
   * @throws C2SimRestException if the server returns a structured C2SIM error
   * @throws ApiException on HTTP or network errors
   * @throws LoxException if the trigger message cannot be serialized
   */
  public void sendTrigger(Trigger trigger)
      throws ValidationException, C2SimRestException, ApiException, LoxException {
    logger.debug(
        "{}Send state machine trigger '{}' (C2SIM message) to C2SIM server",
        getDebugPrefix(),
        trigger);
    // Create C2SIM XML message for trigger
    MessageType msg = C2SimStateMachine.createMessageForTrigger(trigger, createHeader());
    publishC2SimDocument(msg);
    syncStateMachine(); // Trigger can create state change
  }

  private C2SIMHeaderType createHeader() {
    return XmlFactoryHelper.createC2SimHeader(systemName);
  }

  private void beforeSendToClient(MessageQueue.C2SimMessage msg) {
    switch (msg.kind()) {
      case RESET_SCENARIO,
              PAUSE_SCENARIO,
              START_SCENARIO,
              SHARE_SCENARIO,
              RESUME_SCENARIO,
              SUBMIT_INITIALIZATION ->
          // These messages are state change events
          syncStateMachine(); // Just fetch the new state from the C2SIM server

      case C2SIM_INITIALIZATION ->
          // TODO When decoding is enabled the message is already decode, don't decode twice
          handleC2SIMInitialization(msg.xmlMessage());
      default -> {
        // No extra handling needed
      }
    }
  }

  private String getDebugPrefix() {
    return String.format("C2SIM client '%s(%s)': ", getClientId(), getClientIdDisplayName());
  }

  private void handleC2SIMInitialization(String initializationXml) {
    // initializationXml => Root element is must be Message!
    Objects.requireNonNull(initializationXml, "initializationXml is null");
    try {
      MessageType initMsg = MessageTypeHelper.readMessage(initializationXml);
      if ((initMsg != null)
          && (initMsg.getMessageBody() != null)
          && (initMsg.getMessageBody().getC2SIMInitializationBody() != null)) {
        logger.debug("{}Received C2SIMInitializationBody from C2SIM server ", getDebugPrefix());
        var init = initMsg.getMessageBody().getC2SIMInitializationBody();
        c2simClientListener.onC2SIMInitialization(this, init);
      } else {
        logger.error("{}No C2SimInitializationBody found in XML", getDebugPrefix());
      }

    } catch (LoxException e) {
      logger.error(
          "{}Failed to decode C2SIMInitialization: {}", getDebugPrefix(), e.getMessage(), e);
    }
  }

  /**
   * Provides the {@link RequestCreateSession} data used when automatically creating a shared
   * session that does not yet exist on the server.
   */
  @FunctionalInterface
  public interface SharedSessionInfoProvider {
    /**
     * Returns the session creation request to use when the target shared session does not exist on
     * the server.
     *
     * @return the session creation request
     */
    RequestCreateSession provideSharedSessionInfo();
  }

  /**
   * Callback interface for C2SIM client lifecycle events.
   *
   * <p>All methods have default no-op implementations; override only the events you need. {@link
   * #DEFAULT} is a no-op instance that can be used as a safe placeholder.
   */
  public interface C2SimClientListener {
    /** No-op default implementation. */
    C2SimClientListener DEFAULT = new C2SimClientListener() {};

    /**
     * Called after the client has successfully joined the shared session.
     *
     * @param client the client that joined
     * @param info the session information returned by the server
     */
    default void onJoined(C2SimClient client, DynamicSessionInfo info) {}

    /**
     * Called after the client has resigned from the shared session.
     *
     * @param client the client that resigned
     */
    default void onResigned(C2SimClient client) {}

    /**
     * Called when a C2SIM initialization message is received (on join or as a broadcast).
     *
     * @param client the receiving client
     * @param init the initialization body
     */
    default void onC2SIMInitialization(C2SimClient client, C2SIMInitializationBodyType init) {}

    /**
     * Called when the WebSocket stream to the server is connected.
     *
     * @param client the client whose stream connected
     */
    default void onStreamConnected(C2SimClient client) {}

    /**
     * Called when the WebSocket stream to the server is disconnected.
     *
     * @param client the client whose stream disconnected
     * @param code the WebSocket close code
     * @param reason the human-readable close reason
     */
    default void onStreamDisconnected(C2SimClient client, int code, String reason) {}

    /**
     * Called when the WebSocket stream to the server faulted.
     *
     * @param client the client whose stream disconnected
     * @param reason the human-readable close reason
     */
    default void onStreamFault(C2SimClient client, String reason) {}

    /**
     * Called whenever the C2SIM server state changes.
     *
     * @param client the client that detected the change
     * @param oldState the previous server state
     * @param newState the new server state
     */
    default void onStateChanged(C2SimClient client, StateType oldState, StateType newState) {}

    /***
     * An error  in joining process
     * @param client
     * @return if true a retry is scheduled
     */
    default boolean onJoinFailed(C2SimClient client, Exception e) {
      return true;
    }
  }

  /**
   * Fluent builder for {@link C2SimClient}.
   *
   * <p>At minimum {@link #url(String)} and {@link #systemName(String)} must be set before calling
   * {@link #build()}.
   */
  public static class Builder {
    private boolean receivedMsgDecodingEnabled = false;
    private boolean receivedMsgValidationEnabled = false;
    private boolean sendMsgValidationEnabled = false;
    private boolean beautifySendEnabled = false;
    private OidcTokenProvider oidcProvider = null;

    private String clientId = null;
    private String clientDisplayName = null;
    private String url = null;
    private String sharedSessionName = null;
    private String systemName = null;
    private C2SimClientListener listener = C2SimClientListener.DEFAULT;

    /**
     * Sets the C2SIM server base URL from a {@link URI}.
     *
     * @param url the server URL
     * @return this builder
     */
    public Builder url(URI url) {
      this.url = url.toString();
      return this;
    }

    /**
     * Sets the C2SIM server base URL.
     *
     * @param url the server URL string
     * @return this builder
     */
    public Builder url(String url) {
      this.url = url;
      return this;
    }

    /**
     * Sets a human-readable display name for log messages.
     *
     * @param name the display name
     * @return this builder
     */
    public Builder clientIdDisplayName(String name) {
      this.clientDisplayName = name;
      return this;
    }

    /**
     * Sets a fixed client ID. If not set, a random NanoID is generated.
     *
     * @param id the client ID
     * @return this builder
     */
    public Builder clientId(String id) {
      this.clientId = id;
      return this;
    }

    /**
     * Sets the C2SIM system name used when joining the shared session.
     *
     * @param name the system name
     * @return this builder
     */
    public Builder systemName(String name) {
      this.systemName = name;
      return this;
    }

    /**
     * Sets the shared session name to join. Defaults to {@code "default"}.
     *
     * @param sharedSessionName the session name
     * @return this builder
     */
    public Builder sharedSessionName(String sharedSessionName) {
      this.sharedSessionName = sharedSessionName;
      return this;
    }

    /**
     * Sets the client lifecycle event listener.
     *
     * @param listener the listener implementation
     * @return this builder
     */
    public Builder listener(C2SimClientListener listener) {
      this.listener = listener;
      return this;
    }

    /**
     * Enables pretty-printing of outgoing C2SIM XML.
     *
     * @return this builder
     */
    public Builder beautifyXml() {
      this.beautifySendEnabled = true;
      return this;
    }

    /**
     * Enables JAXB decoding of received XML messages into {@link MessageType}.
     *
     * @return this builder
     */
    public Builder enableReceivedMessageDecode() {
      this.receivedMsgDecodingEnabled = true;
      return this;
    }

    /**
     * Disables JAXB decoding of received XML messages.
     *
     * @return this builder
     */
    public Builder disableReceivedMessageDecode() {
      this.receivedMsgDecodingEnabled = false;
      return this;
    }

    /**
     * Enables XSD validation of received XML messages.
     *
     * @return this builder
     */
    public Builder enableReceivedMessageValidation() {
      this.receivedMsgValidationEnabled = true;
      return this;
    }

    /**
     * Disables XSD validation of received XML messages.
     *
     * @return this builder
     */
    public Builder disableReceivedMessageValidation() {
      this.receivedMsgValidationEnabled = false;
      return this;
    }

    /**
     * Enables XSD validation of outgoing XML before publishing.
     *
     * @return this builder
     */
    public Builder enableSendMessageValidation() {
      this.sendMsgValidationEnabled = true;
      return this;
    }

    /**
     * Sets the OIDC token provider for Bearer-token authentication.
     *
     * @param provider the token provider
     * @return this builder
     */
    public Builder oidcProvider(OidcTokenProvider provider) {
      this.oidcProvider = provider;
      return this;
    }

    /**
     * Builds and returns the configured {@link C2SimClient}.
     *
     * @return the new client
     * @throws IllegalArgumentException if {@code url} or {@code systemName} is not set
     */
    public C2SimClient build() {
      if (url == null) {
        throw new IllegalArgumentException("url required");
      }
      if (systemName == null) {
        throw new IllegalArgumentException("systemName required");
      }
      return new C2SimClient(this);
    }
  }

  private class C2SimSocketListener implements OkHttpWebSocketManager.WebSocketListener {

    @Override
    public void onOpen(okhttp3.Response response) {
      logger.debug("{}WebSocket connected to C2SIM server", getDebugPrefix());
      hasStreamToSharedSession = true;
      c2simClientListener.onStreamConnected(C2SimClient.this);
      syncStateMachine();
    }

    @Override
    public void onMessage(String text) {
      streamMsgSplitter.accept(text);
    }

    @Override
    public void onScheduleReconnecting(int attempt, long delayMs) {
      logger.debug(
          "{}WebSocket is reconnecting (#{}) in {} ms", getDebugPrefix(), attempt, delayMs);
    }

    @Override
    public void onFailure(Throwable t, okhttp3.Response response) {
      String message = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
      logger.error("{}WebSocket failure '{}'.", getDebugPrefix(), message);
      c2simClientListener.onStreamFault(C2SimClient.this, message);
    }

    @Override
    public void onClosing(int code, String reason) {
      logger.debug(
          "{}WebSocket is closing, code '{}' with reason '{}'",
          getDebugPrefix(),
          code == 1000 ? "CLOSE_NORMAL" : code,
          reason);
      hasStreamToSharedSession = false;
    }

    @Override
    public void onClosed(int code, String reason) {
      logger.debug(
          "{}WebSocket closed with code '{}' and reason '{}'.", getDebugPrefix(), code, reason);
      hasStreamToSharedSession = false;
      c2simClientListener.onStreamDisconnected(C2SimClient.this, code, reason);
    }
  }
}
