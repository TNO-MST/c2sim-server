package org.c2sim.lox.helpers;

import java.time.Duration;
import java.time.Period;
import java.util.regex.Pattern;
import org.c2sim.lox.schema.DurationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for creating and formatting LOX XSD {@link DurationType} values.
 *
 * <p>The LOX XSD restricts duration strings to the pattern {@code PnnYnnMnnDTnnHnnMnnS} (two-digit
 * fields for each component). This class validates, creates, and formats such strings.
 */
public final class DurationTypeHelper {

  private static final Logger logger = LoggerFactory.getLogger(DurationTypeHelper.class);

  // Prevent instantiation
  private DurationTypeHelper() {
    throw new AssertionError("Only static functions");
  }

  // Regex equivalent to your XSD pattern
  private static final Pattern DURATION_PATTERN =
      Pattern.compile("^P\\d{2}Y\\d{2}M\\d{2}DT\\d{2}H\\d{2}M\\d{2}S$");

  /**
   * Validates whether the given string matches the required duration format.
   *
   * @param value the string to validate
   * @return true if the value matches the pattern, false otherwise
   */
  public static boolean isDurationValid(String value) {
    if (value == null) {
      return false;
    }
    return DURATION_PATTERN.matcher(value).matches();
  }

  /**
   * Builds a LOX duration string from individual date and time components.
   *
   * @param year years component
   * @param month months component
   * @param days days component
   * @param hours hours component
   * @param minutes minutes component
   * @param seconds seconds component
   * @return a duration string in the form {@code PnnYnnMnnDTnnHnnMnnS}
   */
  public static String create(int year, int month, int days, int hours, int minutes, int seconds) {
    var period = Period.of(year, month, days);
    var duration = Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    return create(period, duration);
  }

  /**
   * Builds a LOX duration string from a {@link Period} and a {@link Duration}.
   *
   * @param period the date part (years, months, days)
   * @param duration the time part (hours, minutes, seconds)
   * @return a duration string in the form {@code PnnYnnMnnDTnnHnnMnnS}
   */
  public static String create(Period period, Duration duration) {
    return String.format(
        "P%02dY%02dM%02dDT%02dH%02dM%02dS",
        period.getYears(),
        period.getMonths(),
        period.getDays(),
        duration.toHoursPart(),
        duration.toMinutesPart(),
        duration.toSecondsPart());
  }

  /**
   * Returns a human-readable representation of the time part of a {@link DurationType} (e.g. {@code
   * "2 hours, 15 minutes, 30 seconds"}).
   *
   * <p>Only the time portion (hours, minutes, seconds) is included; the date portion is ignored.
   * Returns {@code "no duration"} when {@code isoTimeDuration} is {@code null}, or a parse-error
   * message when the string cannot be parsed.
   *
   * @param durationtype the LOX duration value
   * @return a human-readable duration string
   */
  public static String formatDuration(DurationType durationtype) {
    try {
      if (durationtype.getIsoTimeDuration() == null) {
        return "no duration";
      }

      String[] duration = durationtype.getIsoTimeDuration().split("T");
      String durationTimePart = duration.length >= 2 ? duration[1] : "";

      // Period period = Period.parse(durationDatePart); // Period is ignored
      Duration timeDuration = Duration.parse("PT" + durationTimePart);

      long hours = timeDuration.toHours();
      long minutes = timeDuration.toMinutesPart();
      long seconds = timeDuration.toSecondsPart();

      StringBuilder result = new StringBuilder();

      if (hours > 0) {
        result.append(hours).append(" hours");
      }
      if (minutes > 0) {
        if (!result.isEmpty()) {
          result.append(", ");
        }
        result.append(minutes).append(" minutes");
      }
      if (seconds > 0) {
        if (!result.isEmpty()) {
          result.append(", ");
        }
        result.append(seconds).append(" seconds");
      }

      return !result.isEmpty() ? result.toString() : "0 seconds";
    } catch (Exception ex) {
      return String.format(
          "Duration parse error (%s): %s ", durationtype.getIsoTimeDuration(), ex.getMessage());
    }
  }

  /**
   * Returns the total number of seconds represented by the given {@link DurationType}.
   *
   * <p>Returns {@code 0} when the duration string cannot be parsed.
   *
   * @param durationtype the LOX duration value
   * @return total seconds, or {@code 0} on parse failure
   */
  public static long getTotalSeconds(DurationType durationtype) {
    try {
      Duration duration = Duration.parse(durationtype.getIsoTimeDuration());
      return duration.getSeconds(); // total seconds
    } catch (Exception ex) {

      logger.error("LOX:DurationTypeFunctions failed to parse duration: {}", ex.getMessage());
      return 0;
    }
  }
}
