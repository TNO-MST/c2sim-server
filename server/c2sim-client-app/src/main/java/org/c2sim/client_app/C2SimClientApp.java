package org.c2sim.client_app;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.c2sim.client.model.StateType;
import org.c2sim.client.security.OidcCredentialFlow;
import org.c2sim.client.security.OidcCredentialFlowConfig;
import org.c2sim.client.security.OidcTokenProvider;
import org.c2sim.lox.exceptions.LoxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo / integration-test entry point for the C2SIM client application.
 *
 * <p>Starts a configurable number of {@link MonitorClient}s, connects them to a local C2SIM server,
 * drives the server through its full state-machine lifecycle sends position-report messages for a
 * fixed period, and then disconnects them cleanly.
 *
 * <p>Expected runtime environment:
 *
 * <ul>
 *   <li>A C2SIM server reachable at {@code http://localhost:7777}
 *   <li>A Keycloak (or compatible OIDC) server at {@code http://localhost:8080}
 * </ul>
 */
public class C2SimClientApp {
  private static final Logger logger = LoggerFactory.getLogger(C2SimClientApp.class);

  /** Number of client instances to create and connect. */
  private static final int CLIENT_COUNT = 1;

  /** Delay between client to startup. */
  private static final Duration START_DELAY = Duration.ofMillis(50);

  /**
   * Application entry point.
   *
   * <p>Waits 5 seconds for the C2SIM server to be ready, then creates, connects, and drives all
   * monitor clients. Exits with status {@code -1} if not all clients connect within 30 seconds or
   * if the server cannot be brought to the RUNNING state.
   *
   * @param args command-line arguments (not used)
   * @throws InterruptedException if the main thread is interrupted while waiting
   * @throws LoxException if a C2SIM XML factory operation fails during state-machine setup
   */
  public static void main(String[] args) throws InterruptedException, LoxException {

    logger.info("Wait 5 seconds (C2SIM server startup");
    Thread.sleep(5000); // Give C2SIM server some time to startup

    OidcCredentialFlowConfig authCfg =
        new OidcCredentialFlowConfig(
            URI.create("http://localhost:8080/realms/c2sim/.well-known/openid-configuration"),
            "client",
            "secret");
    OidcTokenProvider oidcProvider = new OidcCredentialFlow(authCfg);

    CountDownLatch connectedCounter = new CountDownLatch(CLIENT_COUNT);
    List<MonitorClient> clients = new ArrayList<>(CLIENT_COUNT);
    for (int i = 0; i < CLIENT_COUNT; i++) {

      var monitor =
          new MonitorClient(
              oidcProvider, URI.create("http://localhost:7777/api"), i, connectedCounter);
      clients.add(monitor);
      // launch in a virtual thread
      Thread.startVirtualThread(monitor);
    }

    logger.info("Start joining all C2SIM clients to shared session ");
    // Connect all clients
    clients.forEach(
        client -> {
          try {
            client.getC2SimClient().connect();
            // small stagger to avoid thundering herd on the server
            Thread.sleep(START_DELAY.toMillis());
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt(); // restore interrupt flag
          } catch (Exception e) {
            logger.error(
                "C2SIM Client '{}' failed to connect: '{}'.",
                client.getC2SimClient().getClientId(),
                e.getMessage());
          }
        });

    logger.info("Waiting for all C2SIM clients to be connected (joined and setup stream).........");
    // Wait until all clients attempted to connect
    if (!connectedCounter.await(30, TimeUnit.SECONDS)) {
      logger.error("Not all C2Clients connected to C2SIM server (timeout)");
      System.exit(-1);
    }

    logger.info("All C2SIM clients connected.");

    logger.info(
        "Master C2SIM client brings C2SIM server in RUNNING state (other clients will follow).");
    try {
      clients.getFirst().bringIntoRunningState();
    } catch (Exception e) {
      logger.error("Failed to bring C2SIM server in RUNNING state: '{}'.", e.getMessage());
      System.exit(-1);
    }

    if (!clients.stream()
        .allMatch(x -> x.getC2SimClient().getCachedC2SimServerState() == StateType.RUNNING)) {
      logger.error("Not all clients are in running state");

      System.exit(-1);
    }

    for (int i = 0; i < 15000; i++) {
      Thread.sleep(1000);
    }
    logger.info("Resign all C2SIM clients");
    clients.forEach(
        client -> {
          try {
            client.getC2SimClient().resignAndDisconnect();
          } catch (Exception e) {
            logger.error(
                "C2SIM Client '{}' failed to disconnect: '{}'.",
                client.getC2SimClient().getClientId(),
                e.getMessage());
          }
        });

    while (!Thread.currentThread().isInterrupted()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // restore flag
        break; // exit loop
      }
    }
  }
}
