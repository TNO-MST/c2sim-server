package org.c2sim.lox.helpers.builders;

import java.time.Instant;
import org.c2sim.lox.schema.*;

/**
 * Factory methods for creating {@link TimeInstantType} choice values.
 *
 * <p>{@link TimeInstantType} is an XSD choice type that holds exactly one of:
 *
 * <ul>
 *   <li>an absolute date-time ({@link #absoluteTime}),
 *   <li>a relative time offset ({@link #relativeTime}), or
 *   <li>a simulation-time delay ({@link #simulationTime}).
 * </ul>
 */
public class TimeInstantTypeBuilder {

  private TimeInstantTypeBuilder() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Creates a {@link TimeInstantType} holding an absolute UTC date-time.
   *
   * @param absolute the point in time
   * @return a {@link TimeInstantType} with {@code dateTime} set
   */
  public static TimeInstantType absoluteTime(Instant absolute) {
    var timeInstant = new TimeInstantType();
    timeInstant.setDateTime(DateTimeTypeBuilder.create(absolute).build());
    return timeInstant;
  }

  /**
   * Creates a {@link TimeInstantType} holding a relative time value.
   *
   * @param relative the relative time (delay after a named event)
   * @return a {@link TimeInstantType} with {@code relativeTime} set
   */
  public static TimeInstantType relativeTime(RelativeTimeType relative) {
    var timeInstant = new TimeInstantType();
    timeInstant.setRelativeTime(relative);
    return timeInstant;
  }

  /**
   * Creates a {@link TimeInstantType} holding a simulation-time delay.
   *
   * @param relative a builder whose {@link SimulationTimeTypeBuilder#build()} result is used
   * @return a {@link TimeInstantType} with {@code simulationTime} set
   */
  public static TimeInstantType simulationTime(SimulationTimeTypeBuilder relative) {
    var timeInstant = new TimeInstantType();
    timeInstant.setSimulationTime(relative.build());
    return timeInstant;
  }
}
