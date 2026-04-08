package org.c2sim.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.c2sim.Program;

/**
 * Utility methods for reading classpath resources as strings.
 *
 * <p>This is a utility class; instantiation is not allowed.
 */
public class ResourceHelper {

  // Prevent instantiation
  private ResourceHelper() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Reads the named classpath resource and returns its content as a UTF-8 string.
   *
   * <p>Returns an empty string if the resource cannot be found or an I/O error occurs, so callers
   * do not need to handle {@code null} or exceptions.
   *
   * @param resourcePath the classpath-relative path to the resource
   * @return the resource content, or {@code ""} if not available
   */
  public static String readFromResource(String resourcePath) {
    try (InputStream inputStream =
        Program.class.getClassLoader().getResourceAsStream(resourcePath)) {
      if (inputStream != null) {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      }
    } catch (IOException ex) {
      // Don't really care
    }
    return "";
  }
}
