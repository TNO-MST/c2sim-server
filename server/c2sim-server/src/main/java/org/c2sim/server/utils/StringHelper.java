package org.c2sim.server.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class StringHelper {
  private StringHelper() {}

  /**
   * Convert stream into stream
   *
   * @param text te text
   * @return the stream object
   */
  public static InputStream toStream(String text) {
    return new ByteArrayInputStream(text.getBytes());
  }
}
