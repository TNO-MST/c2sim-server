package org.c2sim;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.IOException;
import org.c2sim.server.DefaultModule;
import org.c2sim.server.exceptions.C2SimException;
import org.c2sim.server.services.*;
import org.c2sim.server.sessions.SharedSession;
import org.c2sim.server.utils.ResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application bootstrap that wires the Guice injector and initialises the server.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Creates the Google Guice {@link Injector} from {@link DefaultModule}.
 *   <li>Creates default {@link SharedSession}s as declared in the server configuration.
 *   <li>Exposes service instances obtained from the injector.
 *   <li>Starts and stops the HTTP REST / WebSocket servers.
 * </ul>
 *
 * <p>The singleton is obtained via {@link #getSingleton()}; the JVM exits with code {@code -1} if
 * construction fails.
 */
public class LaunchServer {

  private static final Logger logger = LoggerFactory.getLogger(LaunchServer.class);

  private static LaunchServer instance = null;

  // TODO cache all singletons from injector within this class?
  private final Injector injector;

  /**
   * Creates the Guice injector, optionally prints the license banner, and initialises the default
   * shared sessions from the server configuration.
   *
   * @throws IOException if the server configuration cannot be read
   */
  public LaunchServer() throws IOException {
    this.injector = Guice.createInjector(new DefaultModule());
    showLicense();
    initializeServer();
  }

  /**
   * Returns the singleton {@link LaunchServer} instance, creating it on first call.
   *
   * <p>If construction throws, the error is logged and the JVM terminates with exit code {@code
   * -1}.
   *
   * @return the singleton instance, never {@code null}
   */
  public static synchronized LaunchServer getSingleton() {
    if (instance == null) {
      try {
        instance = new LaunchServer();
      } catch (Exception ex) {
        logger.error("Failed to create main service instance", ex);
        System.exit(-1);
      }
    }
    return instance;
  }

  private void initializeServer() throws IOException {
    logger.info("Initializing C2SIM sever with default session(s).");
    var serverConfig = getConfigService().getServerConfiguration();
    for (var defaultSessionCfg : serverConfig.getDefaultSessions()) {
      try {
        var sharedSession =
            new SharedSession(
                getMetricService(),
                getConfigService(),
                getC2SimSchemaService(),
                defaultSessionCfg.getSharedSessionName(),
                defaultSessionCfg.getC2SimSchemaVersion(),
                defaultSessionCfg.getDisplayName(),
                defaultSessionCfg.getDescription(),
                true);
        getC2SimService().addSharedSession(sharedSession);
      } catch (C2SimException ex) {
        logger.error(
            "Failed to add shared session {}", defaultSessionCfg.getSharedSessionName(), ex);
      }
    }
  }

  private void showLicense() {
    if (getConfigService().getShowLicenceInConsole()) {
      var license = ResourceHelper.readFromResource("LICENSE.txt");
      logger.info("{}", license);
    } else {
      logger.info("License information in LICENSE (display in console disabled in config)");
    }
  }

  /** Starts the HTTP REST server and registers WebSocket endpoints. */
  public void start() {
    getWebService().serve();
  }

  /** Stops the HTTP REST server. */
  public void stop() {
    getWebService().stop();
  }

  /**
   * Returns the {@link ConfigService} singleton from the injector.
   *
   * @return the configuration service
   */
  public ConfigService getConfigService() {
    return injector.getInstance(ConfigService.class);
  }

  /**
   * Returns the {@link C2SimService} singleton from the injector.
   *
   * @return the C2SIM session management service
   */
  public C2SimService getC2SimService() {
    return injector.getInstance(C2SimService.class);
  }

  /**
   * Returns the {@link C2SimSchemaService} singleton from the injector.
   *
   * @return the XSD schema service
   */
  public C2SimSchemaService getC2SimSchemaService() {
    return injector.getInstance(C2SimSchemaService.class);
  }

  /**
   * Returns the {@link WebService} singleton from the injector.
   *
   * @return the Javalin HTTP service
   */
  public WebService getWebService() {
    return injector.getInstance(WebService.class);
  }

  /**
   * Returns the {@link WebSocketService} singleton from the injector.
   *
   * @return the WebSocket registration service
   */
  public WebSocketService getWebSocketService() {
    return injector.getInstance(WebSocketService.class);
  }

  /**
   * Returns the {@link MetricService} singleton from the injector.
   *
   * @return the metric service
   */
  public MetricService getMetricService() {
    return injector.getInstance(MetricService.class);
  }
}
