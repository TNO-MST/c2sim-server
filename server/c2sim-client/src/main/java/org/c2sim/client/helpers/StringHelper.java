package org.c2sim.client.helpers;

/** General-purpose string utilities. */
public final class StringHelper {

  // Prevent instantiation
  private StringHelper() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Returns {@code true} if the string is {@code null} or empty.
   *
   * @param value the string to test
   * @return {@code true} if {@code value} is {@code null} or has length zero
   */
  public static boolean isNullOrEmpty(String value) {
    return value == null || value.isEmpty();
  }
}
