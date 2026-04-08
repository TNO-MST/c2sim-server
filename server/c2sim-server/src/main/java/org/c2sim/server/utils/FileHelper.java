package org.c2sim.server.utils;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility methods for filesystem path resolution relative to the executing JAR or class directory.
 *
 * <p>This is a utility class; instantiation is not allowed.
 */
public class FileHelper {

  // Prevent instantiation
  private FileHelper() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Returns the directory that contains the executing JAR (or class files when running from an
   * IDE).
   *
   * <p>Determines the location from the code source of this class. Falls back to the JVM {@code
   * user.dir} system property if the code source is unavailable or its URI cannot be parsed.
   *
   * @return the absolute path to the directory containing the executing JAR or classes
   */
  public static Path getExecutingDirectory() {
    try {
      // Works both for running from classes and from JAR
      var codeSource = FileHelper.class.getProtectionDomain().getCodeSource();

      if (codeSource == null) {
        // fallback if running in unusual context
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath();
      }

      var uri = codeSource.getLocation().toURI();
      Path path = Paths.get(uri);

      return path.getParent().toAbsolutePath();

    } catch (URISyntaxException e) {
      // Fallback to current working dir
      return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }
  }
}
