package org.c2sim.server.services.impl;

import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.c2sim.Program;
import org.c2sim.server.api.apis.NotificationsApi;
import org.c2sim.server.api.apis.PublishApi;
import org.c2sim.server.api.apis.SessionApi;
import org.c2sim.server.services.C2SimService;
import org.c2sim.server.services.ConfigService;
import org.c2sim.server.services.MetricService;
import org.c2sim.server.services.WebSocketService;
import org.c2sim.server.utils.ResourceHelper;
import org.c2sim.server.utils.StatusWebPage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;


/**
 * Registers all C2SIM REST routes on the Javalin router.
 *
 * <p>Routes are defined under the {@code /api/c2sim/} prefix and are grouped by resource:
 *
 * <ul>
 *   <li>{@code GET /api/c2sim/session/list} — list all sessions
 *   <li>{@code DELETE /api/c2sim/session/{sessionId}} — delete a session
 *   <li>{@code GET /api/c2sim/session/{sessionId}/info} — get session info
 *   <li>{@code PUT /api/c2sim/session/{sessionId}/create} — create or update a session
 *   <li>{@code POST /api/c2sim/session/{sessionId}/join} — join a session
 *   <li>{@code POST /api/c2sim/session/{sessionId}/resign} — resign from a session
 *   <li>{@code GET /api/c2sim/session/{sessionId}/stream-endpoints} — get stream endpoints
 *   <li>{@code GET /api/c2sim/session/{sessionId}/initialization} — get initialization XML
 *   <li>{@code POST /api/c2sim/session/{sessionId}/publish} — publish a C2SIM document
 *   <li>{@code POST /api/c2sim/session/{sessionId}/subscribe} — subscribe to notifications
 * </ul>
 *
 * <p>This is a utility class; instantiation is not allowed.
 */
public final class RestRoutes {

    private static final Logger logger = LoggerFactory.getLogger(RestRoutes.class);

    private RestRoutes() {
    }

    /**
     * Registers all C2SIM API routes on the supplied Javalin configuration.
     *
     * @param javalinConfig   the Javalin configuration to register routes on
     * @param sessionApi      the session API handler
     * @param notificationApi the notifications API handler
     * @param publishApi      the publish API handler
     */
    public static void register(
        JavalinConfig javalinConfig,
        SessionApi sessionApi,
        NotificationsApi notificationApi,
        PublishApi publishApi,
        ConfigService configService,
        MetricService metricService,
        C2SimService c2simService,
        WebSocketService webSocketService
    ) {

        javalinConfig.routes.apiBuilder(
            () -> {
                get("/", RestRoutes::handleMainPage);
                get("/openapi-ui.html", RestRoutes::handleOpenApiUi);
                get("/openapi.yaml", RestRoutes::handleOpenApiSpec);
                get("/health", RestRoutes::handleHealth);
                get("/configuration", ctx -> handleConfiguration(ctx, configService));
                get("/metrics", ctx -> handleMetrics(ctx, metricService, configService.getIsMetricsEnabled()));
                get("/status", ctx -> handleStatus(c2simService, ctx));
                path("api", () -> defineC2SimRoutes(sessionApi, notificationApi, publishApi));
                ws("/api/c2sim/session/{sessionName}/ws",
                    webSocketService::onNewWebSocket);
            });

    }

    public static void handleStatus(C2SimService c2simService, Context ctx) {
        ctx.html(StatusWebPage.createStatusPage(c2simService));
    }


    /**
     * Javalin handler that serves the raw OpenAPI YAML specification embedded in the classpath at
     * {@code /open-api-spec/c2sim-server-spec.yaml}.
     *
     * <p>Registered at {@code GET /openapi.yaml}. Returns HTTP 404 if the resource is missing.
     *
     * @param ctx the Javalin request context
     * @throws IOException if reading the classpath resource fails
     */
  /*
  Don't generate OpenAPI from annotations, just return the original OpenAPI (used to generated server stub)
   */
    private static void handleOpenApiSpec(Context ctx) throws IOException {
        String yaml;
        try (InputStream in =
                 Program.class.getResourceAsStream("/open-api-spec/c2sim-server-spec.yaml")) {
            if (in == null) {
                ctx.status(HttpStatus.NOT_FOUND).result("Failed to load openapi.yaml");
                return;
            }
            yaml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        ctx.contentType("application/yaml" /* ContentType.APPLICATION_YAML*/);
        ctx.header("Content-Disposition", "inline; filename=\"openapi.yaml\"");
        ctx.result(yaml);
    }

    private static void handleMetrics(@NotNull Context ctx,
                                      MetricService metricService,
                                      boolean isMetricsEnabled) {
        if (isMetricsEnabled) {
            logger.info("Endpoint metrics: /metrics");
            // Expose metrics for Prometheus to scrape
            ctx.contentType("text/plain; version=0.0.4; charset=utf-8");
            ctx.result(metricService.getRegistry().scrape());

        } else {
            ctx.result("Metric endpoints not enabled");
            ctx.status(HttpStatus.NOT_FOUND);
        }
    }

    private static void handleConfiguration(@NotNull Context ctx, ConfigService configService) {
        if (configService.getConfigEndpointIsExposed()) {
            logger.info("Endpoint C2SIM server configuration (as mark down): /configuration");
            // Expose configuration as mark down notation
            ctx.contentType("text/markdown");
            ctx.result(configService.asMarkDownTable());
        } else {
            ctx.result("Configuration endpoint disabled in config");
            ctx.status(HttpStatus.NOT_FOUND);
        }
    }



    /* ---------------------------- Route groups ---------------------------- */

    private static void defineC2SimRoutes(
        SessionApi sessionApi, NotificationsApi notificationApi, PublishApi publishApi) {
        path("c2sim", () -> defineSessionRoutes(sessionApi, notificationApi, publishApi));
    }

    private static void defineSessionRoutes(
        SessionApi sessionApi, NotificationsApi notificationApi, PublishApi publishApi) {
        path(
            "session",
            () -> {
                path("list", () -> get(sessionApi::getSessions)); // GET /api/c2sim/session/list
                path("{sessionId}", () -> defineSessionIdRoutes(sessionApi, notificationApi, publishApi));
            });
    }

    private static void defineSessionIdRoutes(
        SessionApi sessionApi, NotificationsApi notificationApi, PublishApi publishApi) {
        delete(sessionApi::deleteSession);

        path("info", () -> get(sessionApi::getSessionInfo)); // GET
        path("create", () -> put(sessionApi::createSession)); // PUT
        path("join", () -> post(sessionApi::joinSession)); // POST
        path("resign", () -> post(sessionApi::resignFromSession)); // POST
        path("stream-endpoints", () -> get(notificationApi::getStreamEndpoints)); // GET
        path("initialization", () -> get(sessionApi::getSessionInitialization)); // GET
        path("send", () -> post(publishApi::send)); // POST
    }

    private static void handleMainPage(Context ctx) {
        var html = ResourceHelper.readFromResource("info.html");
        ctx.html(
            html
                .replace("{{version}}", String.format("version %s", Program.getManifestImplVersion()))
                .replace("{{build}}", Program.getManifestBuildTime()));
    }

    private static void handleOpenApiUi(Context ctx) {
        //  The swagger ui need url to openapi-spec, this is fixed in this page
        var html = ResourceHelper.readFromResource("open-api-ui/openapi-ui.html");
        ctx.html(html);
    }

    private static void handleHealth(Context ctx) {
        ctx.json(Map.of("status", "UP", "timestamp", System.currentTimeMillis()));
    }


}
