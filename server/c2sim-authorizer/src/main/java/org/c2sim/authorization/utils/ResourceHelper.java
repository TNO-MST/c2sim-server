package org.c2sim.authorization.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Helper class to access java resources */
public class ResourceHelper {

  private ResourceHelper() {
    throw new AssertionError("Only static functions");
  }

  /**
   * @param clazz Class in package that stores the resource
   * @param resourcePath The resource name
   * @return The resource content
   * @throws IOException Resource not found
   */
  public static byte[] readResourceAsBytes(Class<?> clazz, String resourcePath) throws IOException {
    try (InputStream inputStream = clazz.getResourceAsStream(resourcePath)) {
      if (inputStream == null) {
        throw new IOException("Resource not found: " + resourcePath);
      }
      return inputStream.readAllBytes();
    }
  }

  /**
   * @param clazz Class in package that stores the resource
   * @param resourcePath The resource name
   * @return The resource content
   * @throws IOException Resource not found
   */
  public static String readResourceAsString(Class<?> clazz, String resourcePath)
      throws IOException {
    return new String(readResourceAsBytes(clazz, resourcePath), StandardCharsets.UTF_8);
  }

  /**
   * Read text string till first space
   *
   * @param input The text
   * @return The text till first space.
   */
  public static String readUntilFirstSpace(String input) {
    int spaceIndex = input.indexOf(' ');
    if (spaceIndex == -1) {
      return input; // No space found, return whole string
    }
    return input.substring(0, spaceIndex);
  }
}
