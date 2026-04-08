package org.c2sim.lox.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Utility methods for reading classpath resources and working with input streams. */
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

  /**
   * Returns the substring of {@code input} that precedes the first space character, or the entire
   * string if no space is found.
   *
   * @param input the source string
   * @return the prefix before the first space, or the whole input
   */
  public static String readUntilFirstSpace(String input) {
    int spaceIndex = input.indexOf(' ');
    if (spaceIndex == -1) {
      return input; // No space found, return whole string
    }
    return input.substring(0, spaceIndex);
  }

  /**
   * Reads all lines from the given {@link InputStream} and returns them as a single string with
   * newline characters between lines.
   *
   * @param is the input stream to read; the caller is responsible for closing it
   * @return the stream's content as a string
   * @throws IOException if reading fails
   */
  public static String toText(InputStream is) throws IOException {
    StringBuilder sb = new StringBuilder();
    String line;

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n"); // Append line and a newline character
      }
    }
    return sb.toString();
  }
}
