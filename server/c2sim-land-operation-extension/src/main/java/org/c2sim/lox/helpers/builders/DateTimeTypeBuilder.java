package org.c2sim.lox.helpers.builders;

import java.time.Instant;
import org.c2sim.lox.helpers.DateTimeTypeHelper;
import org.c2sim.lox.schema.DateTimeType;

/**
 * Builder for {@link DateTimeType} objects.
 *
 * <p>The initial value is set from an {@link Instant} via {@link
 * DateTimeTypeHelper#createDateTimeType(Instant)}.
 */
public class DateTimeTypeBuilder {

  private final DateTimeType dateTime;

  /**
   * Creates a new builder initialized to the given timestamp.
   *
   * @param timestamp the point in time to use as the date-time value
   * @return a new builder instance
   */
  public static DateTimeTypeBuilder create(Instant timestamp) {
    return new DateTimeTypeBuilder(timestamp);
  }

  private DateTimeTypeBuilder(Instant timestamp) {

    dateTime = DateTimeTypeHelper.createDateTimeType(timestamp);
  }

  /**
   * Sets the optional name of the date-time value.
   *
   * @param name a human-readable label, or {@code null}
   * @return this builder
   */
  public DateTimeTypeBuilder name(String name) {
    dateTime.setName(name);
    return this;
  }

  /**
   * Builds and returns the {@link DateTimeType}.
   *
   * @return the constructed date-time object
   */
  public DateTimeType build() {
    return dateTime;
  }
}
