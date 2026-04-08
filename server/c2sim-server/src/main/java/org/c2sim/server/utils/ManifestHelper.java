package org.c2sim.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.c2sim.Program;

/**
 * Utility methods for reading values from the JAR's {@code META-INF/MANIFEST.MF}.
 *
 * <p>Used at startup by {@link Program} to expose the build timestamp and implementation version in
 * log output and the REST status page.
 *
 * <p>This is a utility class; instantiation is not allowed.
 */
public class ManifestHelper {

  /** Manifest attribute name for the build timestamp. */
  public static final String MANIFEST_BUILD_TIME = "Build-Time";

  /** Manifest attribute name for the implementation version. */
  public static final String MANIFEST_IMPL_VERSION = "Implementation-Version";

  // Prevent instantiation
  private ManifestHelper() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Reads and returns the main attributes from {@code META-INF/MANIFEST.MF}.
   *
   * <p>If the manifest cannot be found or read, an empty {@link Attributes} object is returned so
   * callers never need to handle a {@code null} result.
   *
   * @return the manifest main attributes, or empty attributes on failure
   */
  public static Attributes getManifestAttributes() {
    try (InputStream in =
        Program.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF")) {
      if (in != null) {
        Manifest mf = new Manifest(in);
        return mf.getMainAttributes();
      }
    } catch (IOException e) {
      // ignore or log if needed
    }
    return new Attributes();
  }

  /**
   * Returns the value of the named attribute, or {@code defaultValue} if the attribute is absent.
   *
   * @param list the attribute set to search
   * @param key the attribute name
   * @param defaultValue the fallback value
   * @return the attribute value, or {@code defaultValue}
   */
  public static String getValue(final Attributes list, String key, String defaultValue) {
    return Optional.ofNullable(list.getValue(key)).orElse(defaultValue);
  }
}
