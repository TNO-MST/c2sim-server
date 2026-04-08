package org.c2sim.lox.helpers.builders;

import java.time.Instant;
import java.util.List;
import org.c2sim.lox.schema.DurationType;
import org.c2sim.lox.schema.ObservationReportContentType;
import org.c2sim.lox.schema.ObservationType;
import org.c2sim.lox.schema.TimeInstantType;

/**
 * Builder for {@link ObservationReportContentType} objects.
 *
 * <p>One or more {@link ObservationType} entries can be added via {@link #addObservation} or {@link
 * #addAllObservations}.
 */
public class ObservationReportContentTypeBuilder {
  private final ObservationReportContentType reportContent;

  private ObservationReportContentTypeBuilder() {
    this.reportContent = new ObservationReportContentType();
  }

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static ObservationReportContentTypeBuilder create() {
    return new ObservationReportContentTypeBuilder();
  }

  /**
   * Sets the observation duration.
   *
   * @param duration the duration of the observation period
   * @return this builder
   */
  public ObservationReportContentTypeBuilder duration(DurationType duration) {
    reportContent.setDuration(duration);
    return this;
  }

  /**
   * Sets the time of observation from an {@link Instant}.
   *
   * @param timeInstant the point in time when the observation was made
   * @return this builder
   */
  public ObservationReportContentTypeBuilder timeOfObservation(Instant timeInstant) {
    reportContent.setTimeOfObservation(TimeInstantTypeBuilder.absoluteTime(timeInstant));
    return this;
  }

  /**
   * Sets the time of observation directly.
   *
   * @param timeInstant the time instant value
   * @return this builder
   */
  public ObservationReportContentTypeBuilder timeOfObservation(TimeInstantType timeInstant) {
    reportContent.setTimeOfObservation(timeInstant);
    return this;
  }

  /**
   * Adds a single observation to the report content.
   *
   * @param observation the observation to add
   * @return this builder
   */
  public ObservationReportContentTypeBuilder addObservation(ObservationType observation) {
    reportContent.getObservation().add(observation);
    return this;
  }

  /**
   * Adds all observations from the given list to the report content.
   *
   * @param observations the list of observations to add
   * @return this builder
   */
  public ObservationReportContentTypeBuilder addAllObservations(
      List<ObservationType> observations) {
    reportContent.getObservation().addAll(observations);
    return this;
  }

  /**
   * Builds and returns the {@link ObservationReportContentType}.
   *
   * @return the constructed observation report content
   */
  public ObservationReportContentType build() {
    return reportContent;
  }
}
