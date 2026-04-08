package org.c2sim;

import org.c2sim.server.utils.ManifestHelper;
import org.c2sim.server.utils.ResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the C2SIM server process.
 *
 * <p>Startup sequence:
 *
 * <ol>
 *   <li>Registers a JVM shutdown hook that sets a flag and wakes the main thread on SIGTERM.
 *   <li>Prints the welcome banner and manifest build info.
 *   <li>Obtains the {@link LaunchServer} singleton (bootstraps Guice + default sessions).
 *   <li>Starts the Javalin HTTP and WebSocket servers.
 *   <li>Blocks the main thread until the shutdown hook fires.
 *   <li>Calls {@link LaunchServer#stop()} and exits cleanly.
 * </ol>
 *
 * <p>The public fields {@link #manifestImplVersion} and {@link #manifestBuildTime} are populated
 * from the JAR manifest at startup and may be read by other components for diagnostic purposes.
 */
public class Program {
  private static final Logger logger = LoggerFactory.getLogger(Program.class);
  private static final Object lock = new Object();

  private static volatile boolean isShuttingDown = false; // Flag to indicate shutdown

  private static String manifestImplVersion = "";
  private static String manifestBuildTime = "";

  private static void getManifestBuildInfoAndPrint() {
    var attrib = ManifestHelper.getManifestAttributes();
    manifestImplVersion = ManifestHelper.getValue(attrib, ManifestHelper.MANIFEST_IMPL_VERSION, "");
    manifestBuildTime = ManifestHelper.getValue(attrib, ManifestHelper.MANIFEST_BUILD_TIME, "");
    logger.info("C2SIM Server version={} | buildTime={}", manifestImplVersion, manifestBuildTime);
  }

  /**
   * Get version from application manifest
   *
   * @return application version
   */
  public static String getManifestImplVersion() {
    return manifestImplVersion;
  }

  /**
   * Get build timestamp from application manifest
   *
   * @return application version
   */
  public static String getManifestBuildTime() {
    return manifestBuildTime;
  }

  /** Main application C2SIm server */
  public Program() {
    super();
  }

  /**
   * Application entry point.
   *
   * @param args command-line arguments (currently unused)
   */
  public static void main(String[] args) {

    // Register shutdown hook to handle SIGTERM
    // Register a shutdown hook to notify the main thread when the application is shutting down
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  logger.info("Shutdown hook triggered. Application is shutting down...");

                  // Set the shutdown flag and notify the waiting thread
                  synchronized (lock) {
                    isShuttingDown = true; // Set shutdown flag
                    lock.notifyAll(); // Notify the thread waiting on the lock
                  }

                  // Perform additional cleanup tasks here if needed
                }));

    showWelcomeBanner();
    getManifestBuildInfoAndPrint();
    var deployment = LaunchServer.getSingleton();

    if (deployment == null) {
      logger.error("Startup failure");
      System.exit(1);
    }

    // Start javalin server
    deployment.start();
    logger.info("Press <enter> to stop the server (when running in terminal mode)");
    // System.in.read(); Doesn't work for kubernetes POD
    synchronized (lock) {
      while (!isShuttingDown) {
        try {
          logger.info("C2SIM Server is running...");
          lock.wait(); // This will block the main thread
        } catch (InterruptedException e) {
          logger.warn("Main thread interrupted.");
          Thread.currentThread().interrupt();
        }
      }
    }

    // Cleanup or shutdown logic after being notified
    logger.info("Main thread shutdown process.");
    deployment.stop();
    logger.info("Sever terminated");
  }

  private static void showWelcomeBanner() {
    // https://patorjk.com/software/taag/#p=display&f=Wavescape&t=C2SIM+SERVER&x=none&v=4&h=4&w=80&we=false
    var bannerText = ResourceHelper.readFromResource("banner.txt");
    logger.info("\n{}", bannerText);
  }
}
