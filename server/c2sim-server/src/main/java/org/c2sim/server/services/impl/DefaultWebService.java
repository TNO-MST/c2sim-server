package org.c2sim.server.services.impl;

import static com.aventrix.jnanoid.jnanoid.NanoIdUtils.*;
import static io.javalin.http.HandlerType.*;
import static org.c2sim.server.utils.ContextHelper.ATTRIB_TRACKING_ID;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import io.javalin.micrometer.MicrometerPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.websocket.WsConfig;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Objects;
import org.c2sim.authorization.exceptions.AuthorisationException;
import org.c2sim.authorization.impl.C2SimClaimsBuilder;
import org.c2sim.server.api.apis.NotificationsApi;
import org.c2sim.server.api.apis.PublishApi;
import org.c2sim.server.api.apis.SessionApi;
import org.c2sim.server.exceptions.C2SimException;
import org.c2sim.server.rest.impl.NotificationsApiServiceImp;
import org.c2sim.server.rest.impl.PublishApiServiceImpl;
import org.c2sim.server.rest.impl.SessionApiServiceImpl;
import org.c2sim.server.security.JavalinAuthHandler;
import org.c2sim.server.services.*;
import org.c2sim.server.utils.ContextHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link WebService} implementation backed by a Javalin HTTP server.
 *
 * <p>On construction the service configures:
 *
 * <ul>
 *   <li>Forwarded-header support (proxy-aware IP resolution)
 *   <li>Jackson JSON mapper
 *   <li>Swagger UI at {@code /swagger} and the raw spec at {@code /openapi.yaml}
 *   <li>Micrometer / Prometheus metrics at {@code /metrics} (when enabled)
 *   <li>CORS — all hosts allowed (intended for trusted environments only)
 *   <li>Static docs served from the configured docs directory under {@code /docs}
 *   <li>Bearer-token authentication via {@link JavalinAuthHandler}
 *   <li>Per-request {@code clientId} enforcement and NanoID-based tracking IDs
 * </ul>
 */
public class DefaultWebService implements WebService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultWebService.class);
  private static final Logger loggerRest = LoggerFactory.getLogger("RestLogging");
  private final int webServerPort;
  private final ConfigService configService;
  private final SessionApi sessionApi;
  private final PublishApi publishApi;
  private final NotificationsApi notificationApi;
  private final ObjectMapper mapper;
  private final MetricService metricService;
  private final WebSocketService webSocketService;
  private final C2SimService c2SimService;
  private final SessionApiServiceImpl sessionApiServiceImpl;
  private Javalin appServer;
  private C2SimClaimsBuilder claimsBuilder = null;

  private static final String DOCUMENTATION_ENDPOINT = "/docs"; // mkdocs endpoint

  /**
   * Creates the service, wires all REST API implementations, configures the Javalin instance, and
   * sets up exception handling.
   *
   * @param mapper Jackson object mapper used for JSON serialisation
   * @param configService the configuration service
   * @param c2SimService the core C2SIM service
   */
  @Inject
  public DefaultWebService(
      ObjectMapper mapper,
      ConfigService configService,
      C2SimService c2SimService,
      MetricService metricService,
      WebSocketService webSocketService) {
    this.mapper = Objects.requireNonNull(mapper);
    this.configService = Objects.requireNonNull(configService);
    this.c2SimService = Objects.requireNonNull(c2SimService);
    this.metricService = Objects.requireNonNull(metricService);
    this.webSocketService = Objects.requireNonNull(webSocketService);
    this.webServerPort = configService.getWebServerPortNumber();
    this.sessionApiServiceImpl = new SessionApiServiceImpl(c2SimService);
    this.sessionApi = new SessionApi(this.sessionApiServiceImpl);
    this.publishApi =
        new PublishApi(new PublishApiServiceImpl(c2SimService, configService, metricService));
    this.notificationApi =
        new NotificationsApi(new NotificationsApiServiceImp(configService, c2SimService));

    setupJavalin();

    logger.info(
        "IMPORTANT: The latest chrome web browser blocks non secure (HTTP) REST calls; "
            + "disable with  chrome://flags/#block-insecure-private-network-requests");
  }




  private static void handleGlobalRestLogging(Context ctx, float executionTimeMs) {
    var execTime = String.format("%.2f", executionTimeMs);
    if (loggerRest.isTraceEnabled()) {
      loggerRest.trace(
          "Completed REST request: {} {} | Status: {} | Time: {} ms",
          ctx.method(),
          ctx.path(),
          ctx.status(),
          execTime);
    }
  }



  private static void setupMetrics(
      boolean isEnabled, JavalinConfig javalinConfig, MetricService metricService) {
    // Metric plugin
    if (isEnabled) {
      logger.info("Metrics endpoint enabled.");

      var registry = metricService.getRegistry();

      new JvmMemoryMetrics().bindTo(registry);
      new JvmThreadMetrics().bindTo(registry);
      new UptimeMetrics().bindTo(registry);
      new ProcessorMetrics().bindTo(registry);
      // new DiskSpaceMetrics(new File(System.getProperty("user.dir"))).bindTo(registry);
      javalinConfig.registerPlugin(
          new MicrometerPlugin(
              pluginCfg -> pluginCfg.registry = registry // adds HTTP metrics, etc.
              ));
    } else {
      logger.info("Metrics endpoint disabled.");
    }
  }

  /**
   * The number of tags for metrics must be very small, group error codes
   *
   * @param errorCode The C2SIM Exception error code
   * @return The metric error code group
   */
  private static MetricService.MetricInvalidMsgReasonType convertToMetricCategory(
      C2SimException.ErrorCode errorCode) {
    return switch (errorCode.getCodeEnum()) {
      case C2SIM_MSG_SIZE_EXCEEDED,
              SHARED_SESSION_NOT_FOUND,
              NO_CLIENT_ID,
              CLIENT_ID_NOT_EXIST,
              C2SIM_MSG_NOT_ALLOWED_IN_STATE,
              C2SIM_INITIALIZATION_MSG_INVALID_STATE,
              STATE_TRANSITION_NOT_ALLOWED,
              INITIALIZATION_NOT_COMPLETED,
              SHARED_SESSION_ALREADY_EXIST ->
          MetricService.MetricInvalidMsgReasonType.BAD_REQUEST;
      case C2SIM_MSG_DECODING_ERROR,
              XSD_VALIDATION_ERROR,
              XSD_VALIDATION_FAILURE,
              C2SIM_INITIALIZATION_MSG_DECODE_FAILURE,
              C2SIM_ROOT_ELEMENT_MUST_BE_MESSAGE,
              C2SIM_INVALID_HEADER,
              C2SIM_SCHEMA_NOT_SUPPORTED ->
          MetricService.MetricInvalidMsgReasonType.INVALID_FORMAT;
      case AUTHORIZATION_FAILURE -> MetricService.MetricInvalidMsgReasonType.UNAUTHORIZED;
      default -> MetricService.MetricInvalidMsgReasonType.OTHER;
    };
  }

  /**
   * Returns the {@link C2SimClaimsBuilder} used to validate Bearer tokens, constructing it lazily
   * on first call.
   *
   * <p>If {@link ConfigService#getBearerPublicKey()} is non-empty the public key is used directly;
   * otherwise the key is fetched from the OIDC discovery endpoint at {@link
   * ConfigService#getIdentityProviderUrl()}.
   *
   * @return the (possibly cached) claims builder
   * @throws AuthorisationException if the public key cannot be retrieved
   */
  // Use lazy loading (keycloak server can be offline)
  public C2SimClaimsBuilder getClaimsBuilder() throws AuthorisationException {
    // Is it already cached?
    if (this.claimsBuilder != null) {
      return this.claimsBuilder;
    }
    C2SimClaimsBuilder builder = null;
    try {
      if (configService.getBearerPublicKey() != null
          && !configService.getBearerPublicKey().isEmpty()) {
        // The public key is provided in config, use this key
        builder = C2SimClaimsBuilder.createWithPublicKey(configService.getBearerPublicKey());
      } else {
        // Get public key from OIDC discovery
        var discoveryUrl = URI.create(configService.getIdentityProviderUrl()).toURL();
        builder = C2SimClaimsBuilder.createWithKeycloakConfiguration(discoveryUrl);
      }
      builder.addAudience("c2sim");
      this.claimsBuilder = builder;
    } catch (Exception ex) {
      logger.error("Failed to retrieve public key of keycloak (needed for signature validation)");
      throw new AuthorisationException(ex.getMessage());
    }
    return this.claimsBuilder;
  }

  // javalin uses jetty as engine; disable all logging for jetty (only error and warnings)
  private void disableJettyLogging() {
    final Logger loggerInstance = LoggerFactory.getLogger("org.eclipse.jetty");

    if (loggerInstance instanceof ch.qos.logback.classic.Logger logbackLoggerJetty) {
      logbackLoggerJetty.setLevel(Level.WARN);
    }
  }

  private void setupJavalin() {
    disableJettyLogging(); // Javalin uses jetty as engine, jetty also uses logback logger
    this.appServer =
        Javalin.create(
            javalinConfig -> {
              // Forwarded headers (X-Forwarded-For, X-Forwarded-Proto)
              javalinConfig.jetty.modifyServer(
                  server -> {
                    // Enable forwarding support
                    for (var connector : server.getConnectors()) {
                      var cf =
                          connector.getConnectionFactory(
                              org.eclipse.jetty.server.HttpConnectionFactory.class);
                      if (cf != null) {
                        cf.getHttpConfiguration()
                            .addCustomizer(
                                new org.eclipse.jetty.server.ForwardedRequestCustomizer());
                      }
                    }
                  });

              // Note: the LOX C2SIM message in operation publish is ALWAYS (literal) XML
              javalinConfig.http.defaultContentType = "application/json";
              javalinConfig.startup.showJavalinBanner = false;
              javalinConfig.jsonMapper(new JavalinJackson(mapper, true));

              setupSwaggerUi(javalinConfig);
              setupMetrics(configService.getIsMetricsEnabled(), javalinConfig, metricService);

              // Set CORS
              javalinConfig.bundledPlugins.enableCors(
                  cors ->
                      cors.addRule(
                          it -> {
                            it.anyHost();
                            logger.warn(
                                "CORS: all host are allowed "
                                    + "(this should only be used in trusted environment)");
                          }));

              if (configService.getDocsDirectory() != null
                  && Files.exists(configService.getDocsDirectory(), LinkOption.NOFOLLOW_LINKS)
                  && (Files.isDirectory(
                      configService.getDocsDirectory(), LinkOption.NOFOLLOW_LINKS))) {
                javalinConfig.staticFiles.add(
                    files -> {
                      files.directory = configService.getDocsDirectory().toString();
                      files.hostedPath = DOCUMENTATION_ENDPOINT;
                      files.location = Location.EXTERNAL;
                      logger.info(
                          "Hosting the web application docs from path '{}'. ", files.directory);
                    });
              }
              logger.trace("Enable REST call logging.");
              javalinConfig.requestLogger.http(DefaultWebService::handleGlobalRestLogging);
              // Add all REST routes from Open API spec
              RestRoutes.register(
                  javalinConfig,
                  sessionApi, notificationApi, publishApi,
                  configService, metricService, c2SimService, webSocketService);
              javalinConfig.routes.beforeMatched(JavalinAuthHandler::addSecurityToAllEndpoints);
              // Must return XML instead of JSON:
              javalinConfig.routes.before("/api/c2sim/session/{sessionId}/initialization",
                  this::handleSpecialCaseRestOperationInitialization);

              javalinConfig.routes.before(this::handleBefore); // Client tracking id
              javalinConfig.routes.after(this::handleAfter);

              // Set exception handling:
              javalinConfig.routes.exception(C2SimException.class, this::handleRESTFulC2SimRestException);
              // Handle all exceptions thrown in REST controller
              javalinConfig.routes.exception(Exception.class, this::handleRESTFulExceptions);
              javalinConfig.routes.exception(AuthorisationException.class, this::handleAuthorizationException);

            });

  }

  /**
   * Collect RESTful metrics after handling request
   *
   * @param ctx The Javalin context
   */
  private void handleAfter(@NotNull Context ctx) {
    metricService.decActiveHttpRequests();
    Long start = ctx.attribute("metricStartTime");
    if (start != null) {
      long durationNs = System.nanoTime() - start;
      metricService.requestDuration(
          ctx.method().name(), ctx.path(), String.valueOf(ctx.status()), durationNs);
    }
  }

  private void setupSwaggerUi(JavalinConfig javalinConfig) {
    // Swagger UI (points to the same documentation path)
    javalinConfig.registerPlugin(
        new SwaggerPlugin(
            // /swagger has error that url is not recognised; fixed by static html page
            // see /openapi-ui
            swagger -> {
              swagger.withDocumentationPath("/openapi.yaml");
              swagger.withUiPath("/swagger");
            }));
  }

  /** Add a tracking ID to each request Can be used to correlate messages in log */
  private void handleBefore(@NotNull Context ctx) {
    metricService.incActiveHttpRequests();

    // Store start time processing request
    ctx.attribute(ContextHelper.ATTRIB_METRIC_START, System.nanoTime());

    ctx.attribute(ContextHelper.ATTRIB_WEB_SERVICE, this);

    // Add tracking ID to each request
    String trackingId = randomNanoId(DEFAULT_NUMBER_GENERATOR, DEFAULT_ALPHABET, 5);

    ctx.attribute(ATTRIB_TRACKING_ID, trackingId);

    if (ctx.path().startsWith("/api")) {
      var pathText = ctx.path(); // Invoke method(s) only conditionally.
      loggerRest.debug("Incoming (API) REST: {} {}", ctx.method(), pathText);
    }
    // This VERB need clientId in header
    if (ctx.path().startsWith("/api")) {
      switch (ctx.method().toString().toUpperCase()) {
        case "GET", "POST", "PUT", "DELETE" -> {
          String clientIdHeader = ctx.header(ContextHelper.ATTRIB_HEADER_PARAM_CLIENT_ID);

          if (clientIdHeader == null || clientIdHeader.isEmpty()) {
            throw new C2SimException(
                C2SimException.ErrorCode.NO_CLIENT_ID,
                "Rest header 'clientId' is mandatory (used to track the client).");
          }
        }
        default -> {
          // no-op
        }
      }
    }
  }

  // Special case, the REST operation initialization return XML
  // The generated code cannot handle this....
  // Generated code returns JSON, need XML (intercept call)
  private void handleSpecialCaseRestOperationInitialization(Context ctx) {

          var clientId = ctx.header(ContextHelper.ATTRIB_HEADER_PARAM_CLIENT_ID);
          if (clientId == null) {
            clientId = "";
          }
          var sessionId = ctx.pathParam("sessionId");
          var result = sessionApiServiceImpl.getSessionInitialization(clientId, sessionId, ctx);
          ctx.contentType("application/xml");

          ctx.result(result);
          ctx.skipRemainingHandlers();

  }



  private void handleAuthorizationException(AuthorisationException e, Context ctx) {
    var trackingId = ContextHelper.getAttributeValue(ctx, ATTRIB_TRACKING_ID);
    logger.error("Authorization error for tracking ID '{}': {}", trackingId, e.getMessage());

    ctx.status(HttpStatus.UNAUTHORIZED).result(e.getMessage());
    metricService.incAuthFailed();
  }

  /** Send error response to client */
  private void handleRESTFulC2SimRestException(C2SimException c2SimRestException, Context ctx) {

    var trackingId = ContextHelper.getAttributeValue(ctx, ATTRIB_TRACKING_ID);
    var sharedSessionName =
        ContextHelper.getAttributeValue(ctx, ContextHelper.ATTRIB_SHARED_SESSION_ID);
    var header = ContextHelper.getC2SimHeader(ctx);
    // TODO Use OIDC system name if available (with ContextHelper.getAuthorizer)

    // System that made the request
    var systemName = (header != null) ? header.getFromSendingSystem() : "UNKNOWN";
    updateMetricsForFailedRequests(c2SimRestException, sharedSessionName, systemName);
    logger.error(
        "Session '{}': Request '{}' by system '{}' with tracking id '{}' failed: {} ({}); "
            + "STATUS BAD_REQUEST(400)",
        sharedSessionName,
        ctx.req().getRequestURI(),
        systemName,
        trackingId,
        c2SimRestException.getMessage(),
        c2SimRestException.getError().getCode());
    ctx.json(c2SimRestException.getError());
    ctx.status(HttpStatus.BAD_REQUEST);
  }

  /**
   * Update metrics for failed RESTful request
   *
   * @param c2SimRestException Information about the error
   * @param sharedSessionName Shared session name where exception occurred in
   * @param systemName The system that made the request
   */
  private void updateMetricsForFailedRequests(
      C2SimException c2SimRestException, String sharedSessionName, String systemName) {
    var errorCode = C2SimException.ErrorCode.fromCode(c2SimRestException.getError().getCode());
    metricService.incInvalidMessagesSendByC2SimClient(
        sharedSessionName, systemName, convertToMetricCategory(errorCode));
  }

  /** Send error response to client */
  private void handleRESTFulExceptions(Exception e, Context ctx) {
    var trackingId = ContextHelper.getAttributeValue(ctx, ATTRIB_TRACKING_ID);
    // Check for error: use ctx.body()
    logger.error(
        "Request '{}' with tracking id '{}' failed (generic): {}; return STATUS 500 error code",
        ctx.req().getRequestURI(),
        trackingId,
        e.getMessage());

    ctx.result("C2SIM Internal Server Error: " + e.getMessage());
    ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Starts the Javalin server, binding on all interfaces ({@code 0.0.0.0}) at the configured port.
   * Logs the reachable URL so operators can quickly find the API endpoints.
   */
  public void serve() {

    // Don't want to return the hostname of docker container (when running in docker),
    // but the hostname that is externally accessible.
    var externalHostName = configService.getExternalHostname();
    if (externalHostName == null || externalHostName.isEmpty()) {
      // fallback if ENV is not set
      try {
        externalHostName = InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException ex) {
        externalHostName = "localhost";
      }
    }

    String url = "http(s)://" + externalHostName + ":" + this.webServerPort;
    logger.info("Listening on {}", url);
    logger.info("- {}/openapi.yaml to view Open API definition", url);
    logger.info("- {}/openapi-ui for Open API developers tools ", url);

    appServer.start("0.0.0.0", webServerPort);
  }

  /** {@inheritDoc} */
  public void stop() {
    appServer.stop();
  }

  /** {@inheritDoc} */
  @Override
  public ConfigService getConfigService() {
    return configService;
  }

  /** {@inheritDoc} */
  public Javalin getJavalin() {
    return appServer;
  }


}
