package org.c2sim.lox.helpers.builders;

import java.util.Objects;
import org.c2sim.lox.helpers.DurationTypeHelper;
import org.c2sim.lox.schema.DurationType;

/**
 * Builder for {@link DurationType} objects.
 *
 * <p>The duration string is created by {@link DurationTypeHelper#create} and validated by {@link
 * DurationTypeHelper#isDurationValid} when {@link #build()} is called.
 */
public class DurationTypeBuilder {
  private final DurationType duration;

  /**
   * Creates a builder for a duration with the given individual components.
   *
   * @param year years component
   * @param month months component
   * @param days days component
   * @param hours hours component
   * @param minutes minutes component
   * @param seconds seconds component
   */
  public DurationTypeBuilder(int year, int month, int days, int hours, int minutes, int seconds) {

    duration = new DurationType();
    duration.setIsoTimeDuration(
        DurationTypeHelper.create(year, month, days, hours, minutes, seconds));
  }

  /**
   * Builds and returns the {@link DurationType}.
   *
   * @return the constructed duration object
   * @throws RuntimeException if the duration string is {@code null} or does not match the LOX XSD
   *     pattern
   */
  public DurationType build() {
    Objects.requireNonNull(duration.getIsoTimeDuration());
    if (!DurationTypeHelper.isDurationValid(duration.getIsoTimeDuration())) {
      throw new IllegalArgumentException("IsoTimeDuration is invalid");
    }
    return duration;
  }
}
