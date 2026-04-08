package org.c2sim.lox.helpers;

import java.time.Instant;
import org.c2sim.lox.schema.DateTimeType;
import org.c2sim.lox.schema.RelativeTimeType;
import org.c2sim.lox.schema.SimulationTimeType;
import org.c2sim.lox.schema.TimeInstantType;

/**
 * Utility methods for creating and formatting {@link TimeInstantType} values.
 *
 * <p>A {@link TimeInstantType} is a choice type that holds exactly one of:
 *
 * <ul>
 *   <li>an absolute {@link DateTimeType} (LOX ISO format),
 *   <li>a {@link RelativeTimeType} (delay after a named event), or
 *   <li>a {@link SimulationTimeType} (delay in simulation time).
 * </ul>
 */
public class TimeInstantTypeHelper {

  // Prevent instantiation
  private TimeInstantTypeHelper() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Creates a {@link TimeInstantType} with an absolute date-time set to {@code dateTimeValue}.
   *
   * @param dateTimeValue the point in time
   * @return a new {@link TimeInstantType} with its {@code dateTime} choice populated
   */
  public static TimeInstantType createTimeInstantType(Instant dateTimeValue) {
    TimeInstantType timeInstant = new TimeInstantType();
    timeInstant.setDateTime(DateTimeTypeHelper.createDateTimeType(dateTimeValue));
    return timeInstant;
  }

  /**
   * Creates a {@link TimeInstantType} with an absolute date-time parsed from a LOX ISO string.
   *
   * <p>Expects ISO notation; not compatible with NETN-ETR timestamp notation.
   *
   * @param isoDateTime a UTC timestamp in the form {@code yyyy-MM-ddTHH:mm:ssZ}
   * @return a new {@link TimeInstantType} with its {@code dateTime} choice populated
   * @throws java.time.DateTimeException if the string does not match the LOX XSD pattern
   */
  /* Expects ISO notation, is not compatible with NETN-ETR timestamp notation */
  public static TimeInstantType createTimeInstantType(String isoDateTime) {
    // Convert it first to Instant (to check if text is valid)
    Instant timestamp = DateTimeTypeHelper.toInstant(isoDateTime);
    return createTimeInstantType(timestamp);
  }

  /**
   * Returns a human-readable description of the given {@link TimeInstantType}.
   *
   * <p>Dispatches to the appropriate helper based on the populated choice field ({@code dateTime},
   * {@code relativeTime}, or {@code simulationTime}). Returns {@code "<invalid>"} when none of the
   * choice fields is populated.
   *
   * @param timestamp the time instant to describe
   * @return a human-readable string representation
   */
  public static String getTimeInstantTypeAsText(TimeInstantType timestamp) {
    // Choice:
    // 1.) DateTime => IsoDateTime with name
    // 2.) RelativeTime => Relative time
    // 3.) SimulationTime => Simulation time
    if (timestamp.getDateTime() != null) {
      return getDateTimeTypeAsText(timestamp.getDateTime());
    }
    if (timestamp.getRelativeTime() != null) {
      return getRelativeTimeAsText(timestamp.getRelativeTime());
    }
    if (timestamp.getSimulationTime() != null) {
      return getSimulationAsText(timestamp.getSimulationTime());
    }
    return "<invalid>";
  }

  /**
   * Returns a human-readable description of the given absolute {@link DateTimeType}.
   *
   * @param timestamp the date-time value; may be {@code null}
   * @return a formatted string, an empty string when {@code timestamp} is {@code null}, or an error
   *     message when parsing fails
   */
  public static String getDateTimeTypeAsText(DateTimeType timestamp) {
    try {
      if ((timestamp == null) || (timestamp.getIsoDateTime() == null)) {
        return "";
      }
      var isoDateTime = timestamp.getIsoDateTime();
      Instant datetime = DateTimeTypeHelper.toInstant(isoDateTime);
      return String.format(
          "absolute time error: %s (%s)",
          datetime, timestamp.getName() != null ? timestamp.getName() : "<no name>");
    } catch (Exception ex) {
      return String.format("Error parsing absolute: %s ", ex.getMessage());
    }
  }

  /**
   * Returns a human-readable description of the given relative time value.
   *
   * @param timestamp the relative time value; may be {@code null}
   * @return a formatted string, an empty string when {@code timestamp} is {@code null}, or an error
   *     string when formatting fails
   */
  public static String getRelativeTimeAsText(RelativeTimeType timestamp) {
    if (timestamp == null) {
      return "";
    }
    try {
      return String.format(
          "relative: %s after event with uuid  %s (%s)",
          DurationTypeHelper.formatDuration(timestamp.getDelayTimeAmount()),
          timestamp.getEventReference(),
          timestamp.getTimeReferenceCode().value());
    } catch (Exception ex) {
      return "<error " + ex.getMessage() + ">";
    }
  }

  /**
   * Returns a human-readable description of the given simulation-time value.
   *
   * @param timestamp the simulation time value; may be {@code null}
   * @return a formatted string, an empty string when {@code timestamp} is {@code null}, or an error
   *     string when formatting fails
   */
  public static String getSimulationAsText(SimulationTimeType timestamp) {
    if (timestamp == null) {
      return "";
    }
    try {
      return String.format(
          "simulation time: delay %s",
          DurationTypeHelper.formatDuration(timestamp.getDelayTimeAmount()));
    } catch (Exception ex) {
      return String.format("simulation time error: %s", ex.getMessage());
    }
  }
}
