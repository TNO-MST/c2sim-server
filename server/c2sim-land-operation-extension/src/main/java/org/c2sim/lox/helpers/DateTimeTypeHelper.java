package org.c2sim.lox.helpers;

// The LOX XSD uses for timestamp xs:string and not xs:dateTime
// This is the reason JAXB will generate a string field for type DateTimeType.
// This pattern works for Instant.parse (subset of ISO standard)

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import org.c2sim.lox.schema.DateTimeType;

/**
 * Utility methods for converting between Java {@link Instant} and the LOX XSD timestamp format.
 *
 * <p>The LOX XSD defines timestamps as {@code xs:string} restricted to the pattern {@code
 * yyyy-MM-dd'T'HH:mm:ss'Z'} (UTC only, no fractional seconds). JAXB therefore generates {@code
 * String} fields rather than {@code XMLGregorianCalendar}.
 */
public class DateTimeTypeHelper {

  private static final DateTimeFormatter loxIsoFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

  private static final String DATE_TIME_REGEX = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z";
  private static final Pattern DateTimePattern = Pattern.compile(DATE_TIME_REGEX);

  private DateTimeTypeHelper() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Returns {@code true} if {@code dateTime} matches the LOX XSD timestamp pattern {@code
   * yyyy-MM-ddTHH:mm:ssZ}.
   *
   * @param dateTime the string to test
   * @return {@code true} when the string conforms to the pattern, {@code false} otherwise
   */
  public static boolean isValidXsdPattern(String dateTime) {
    return DateTimePattern.matcher(dateTime).matches();
  }

  /**
   * Parses a LOX ISO timestamp string into an {@link Instant}.
   *
   * @param isoDateTime a UTC timestamp in the form {@code yyyy-MM-ddTHH:mm:ssZ}
   * @return the corresponding {@link Instant}
   * @throws DateTimeException if the string is a valid ISO instant but does not match the stricter
   *     LOX XSD pattern
   */
  public static Instant toInstant(String isoDateTime) {
    Instant timestamp = Instant.parse(isoDateTime);
    if (!isValidXsdPattern(isoDateTime)) {
      throw new DateTimeException(
          isoDateTime
              + " is a valid ISO timestamp, but doesn't match LOX XSD pattern yyyy-MM-dd'T'HH:mm:ss'Z' ");
    }
    return timestamp;
  }

  /**
   * Returns the number of seconds since the Unix epoch for the given LOX ISO timestamp.
   *
   * @param isoDateTime a UTC timestamp in the form {@code yyyy-MM-ddTHH:mm:ssZ}
   * @return epoch-seconds
   * @throws DateTimeException if the string does not match the LOX XSD pattern
   */
  public static long toEpochSeconds(String isoDateTime) {
    return toInstant(isoDateTime).getEpochSecond();
  }

  /**
   * Returns the current UTC time formatted as a LOX ISO timestamp string ({@code
   * yyyy-MM-ddTHH:mm:ssZ}).
   *
   * @return the current time as a LOX-formatted string
   */
  // Return current time in LOX ISO notation
  public static String getNow() {
    return toIsoDateTimeText(Instant.now());
  }

  /**
   * Formats an {@link Instant} as a LOX ISO timestamp string ({@code yyyy-MM-ddTHH:mm:ssZ}).
   *
   * @param instant the point in time to format
   * @return the LOX-formatted timestamp string
   */
  // This is the ISO notation LOX uses
  // (subset of https://www.w3.org/TR/xmlschema-2/#dateTime)
  public static String toIsoDateTimeText(Instant instant) {
    return loxIsoFormatter.format(instant);
  }

  /**
   * Creates a {@link DateTimeType} whose {@code isoDateTime} field is set to the given instant.
   *
   * @param dateTimeValue the point in time
   * @return a new {@link DateTimeType} with {@code isoDateTime} set and {@code name} set to {@code
   *     null}
   */
  public static DateTimeType createDateTimeType(Instant dateTimeValue) {
    DateTimeType dateTime = new DateTimeType();
    dateTime.setIsoDateTime(toIsoDateTimeText(dateTimeValue));
    dateTime.setName(null);
    return dateTime;
  }

  /**
   * Creates a {@link DateTimeType} whose {@code isoDateTime} field is set to the current UTC time.
   *
   * @return a new {@link DateTimeType} representing now
   */
  public static DateTimeType createDateTimeTypeNow() {
    DateTimeType dateTime = new DateTimeType();
    dateTime.setIsoDateTime(getNow());
    dateTime.setName(null); // Optional
    return dateTime;
  }

  /**
   * Creates a {@link DateTimeType} from a LOX ISO timestamp string.
   *
   * @param dateTimeValue a UTC timestamp in the form {@code yyyy-MM-ddTHH:mm:ssZ}
   * @return a new {@link DateTimeType} with {@code isoDateTime} set
   * @throws DateTimeException if the string does not match the LOX XSD pattern
   */
  public static DateTimeType createDateTimeType(String dateTimeValue) {
    // Convert it first to check is string is valid
    Instant timestamp = toInstant(dateTimeValue);
    return createDateTimeType(timestamp);
  }

  /**
   * Returns {@code true} if {@code isoDateTime} represents the sentinel "empty" date-time value
   * {@code 0000-00-00T00:00:00Z}.
   *
   * @param isoDateTime the string to test
   * @return {@code true} when the value is the empty sentinel, {@code false} otherwise
   */
  public static boolean isEmptyDateTime(String isoDateTime) {
    return isoDateTime.equals("0000-00-00T00:00:00Z");
  }
}
