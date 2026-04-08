package org.c2sim.server.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import java.util.TimeZone;

/**
 * Provides the shared, pre-configured Jackson {@link ObjectMapper} for the C2SIM server.
 *
 * <p>The mapper is configured to:
 *
 * <ul>
 *   <li>support Kotlin data classes via the Kotlin module
 *   <li>serialise {@code java.time.*} types as ISO-8601 strings (not UNIX timestamps)
 *   <li>include a colon in the timezone offset (e.g. {@code +01:00}) and use UTC
 * </ul>
 *
 * <p>This is a utility class; instantiation is not allowed.
 */
public class C2SimObjectMapper {

  // Prevent instantiation
  private C2SimObjectMapper() {
    throw new AssertionError("Only static functions");
  }

  /**
   * The shared {@link ObjectMapper} instance used throughout the C2SIM server.
   *
   * <p>Thread-safe once fully constructed; do not mutate after startup.
   */
  public static final ObjectMapper mapper =
      new ObjectMapper()
          .registerModule(new KotlinModule.Builder().build())
          .registerModule(new JavaTimeModule()) // support java.time.*
          .disable(
              SerializationFeature
                  .WRITE_DATES_AS_TIMESTAMPS) // write ISO-8601 strings, not timestamps
          .setDateFormat(
              new StdDateFormat()
                  .withColonInTimeZone(true)
                  .withTimeZone(TimeZone.getTimeZone("UTC")));
  // READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE
  //    FAIL_ON_UNKNOWN_PROPERTIES
}
