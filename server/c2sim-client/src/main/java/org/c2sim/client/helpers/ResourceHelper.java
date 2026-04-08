package org.c2sim.client.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Read resources from JAR */
public class ResourceHelper {
  // Prevent instantiation
  private ResourceHelper() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Reads a classpath resource relative to {@code clazz} and returns its raw bytes.
   *
   * @param clazz the class used as the base for the resource lookup
   * @param resourcePath the resource path, relative to {@code clazz} or absolute from the classpath
   *     root
   * @return the resource contents as a byte array
   * @throws IOException if the resource is not found or cannot be read
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
   * Reads a classpath resource relative to {@code clazz} and returns its content as a UTF-8 string.
   *
   * @param clazz the class used as the base for the resource lookup
   * @param resourcePath the resource path, relative to {@code clazz} or absolute from the classpath
   *     root
   * @return the resource contents as a {@link String}
   * @throws IOException if the resource is not found or cannot be read
   */
  public static String readResourceAsString(Class<?> clazz, String resourcePath)
      throws IOException {
    return new String(readResourceAsBytes(clazz, resourcePath), StandardCharsets.UTF_8);
  }
}
