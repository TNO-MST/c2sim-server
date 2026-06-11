package org.c2sim;

import org.c2sim.server.ShutdownManager;
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
  public static void main(String[] args) throws InterruptedException {

    // Register shutdown hook to handle SIGTERM
    // Is done in static init ShutdownManager

    showWelcomeBanner();
    getManifestBuildInfoAndPrint();
    var deployment = LaunchServer.getSingleton();

    if (deployment == null) {
      logger.error("Startup failure");
      System.exit(1);
    }

    // Start javalin server
    deployment.start();
    ShutdownManager.awaitShutdown();
    // Cleanup or shutdown logic after being notified
    deployment.stop();

    logger.info("Shutdown process completed.");
    logger.info("Sever terminated");
  }

  private static void showWelcomeBanner() {
    // https://patorjk.com/software/taag/#p=display&f=Wavescape&t=C2SIM+SERVER&x=none&v=4&h=4&w=80&we=false
    var bannerText = ResourceHelper.readFromResource("banner.txt");
    logger.info("\n{}", bannerText);
  }
}
