package org.c2sim.lox.helpers.builders;

import java.time.Instant;
import java.util.UUID;
import org.c2sim.lox.schema.*;

/**
 * Builder for {@link PositionReportContentType} objects.
 *
 * <p>{@code timeOfObservation} is required before calling {@link #build()}. The entity health
 * status fields (strength, operational status, resources) are handled the same way as in {@link
 * HealthObservationTypeBuilder}: the builder finds or creates the relevant {@link
 * EntityHealthStatusType} entry and updates it.
 */
public class PositionReportContentTypeBuilder {
  private final PositionReportContentType report;

  private PositionReportContentTypeBuilder() {
    this.report = new PositionReportContentType();
  }

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static PositionReportContentTypeBuilder create() {
    return new PositionReportContentTypeBuilder();
  }

  /**
   * Sets the observation duration.
   *
   * @param duration the duration of the observation period
   * @return this builder
   */
  public PositionReportContentTypeBuilder duration(DurationType duration) {
    report.setDuration(duration);
    return this;
  }

  /**
   * Sets the time of observation from an {@link Instant}.
   *
   * @param timeInstant the point in time when the position was observed
   * @return this builder
   */
  public PositionReportContentTypeBuilder timeOfObservation(Instant timeInstant) {
    report.setTimeOfObservation(TimeInstantTypeBuilder.absoluteTime(timeInstant));
    return this;
  }

  /**
   * Sets the time of observation directly.
   *
   * @param timeInstant the time instant value
   * @return this builder
   */
  public PositionReportContentTypeBuilder timeOfObservation(TimeInstantType timeInstant) {
    report.setTimeOfObservation(timeInstant);
    return this;
  }

  /**
   * Sets the heading angle.
   *
   * @param heading the heading in degrees, or {@code null}
   * @return this builder
   */
  public PositionReportContentTypeBuilder headingAngle(Double heading) {
    report.setHeadingAngle(heading);
    return this;
  }

  /**
   * Sets the location directly.
   *
   * @param location the location value
   * @return this builder
   */
  public PositionReportContentTypeBuilder location(LocationType location) {
    report.setLocation(location);
    return this;
  }

  /**
   * Sets the location from a geodetic coordinate builder.
   *
   * @param location a builder whose {@link GeodeticCoordinateTypeBuilder#build()} result is used
   * @return this builder
   */
  public PositionReportContentTypeBuilder location(GeodeticCoordinateTypeBuilder location) {
    var loc = new LocationType();
    loc.setRelativeLocation(null);
    loc.setGeodeticCoordinate(location.build());
    report.setLocation(loc);
    return this;
  }

  /**
   * Sets the location from a relative-location builder.
   *
   * @param location a builder whose {@link RelativeLocationTypeBuilder#build()} result is used
   * @return this builder
   */
  public PositionReportContentTypeBuilder location(RelativeLocationTypeBuilder location) {
    var loc = new LocationType();
    loc.setGeodeticCoordinate(null);
    loc.setRelativeLocation(location.build());
    report.setLocation(loc);
    return this;
  }

  /**
   * Sets the speed of the entity.
   *
   * @param speed the speed value, or {@code null}
   * @return this builder
   */
  public PositionReportContentTypeBuilder speed(Double speed) {
    report.setSpeed(speed);
    return this;
  }

  /**
   * Sets the subject entity UUID.
   *
   * @param entityId the UUID of the entity whose position is reported
   * @return this builder
   */
  public PositionReportContentTypeBuilder subjectEntity(UUID entityId) {
    report.setSubjectEntity(entityId.toString());
    return this;
  }

  /**
   * Sets the strength percentage. Updates an existing strength entry if one is already present,
   * otherwise adds a new {@link EntityHealthStatusType}.
   *
   * @param percentage the strength as a percentage (0–100)
   * @return this builder
   */
  public PositionReportContentTypeBuilder strength(int percentage) {
    var x =
        report.getEntityHealthStatus().stream()
            .filter(item -> item.getStrength() != null)
            .findAny();
    if (x.isPresent()) {
      x.get().getStrength().setStrengthPercentage(percentage);
    } else {
      var strength = new StrengthType();
      strength.setStrengthPercentage(percentage);
      var ehs = new EntityHealthStatusType();
      ehs.setStrength(strength);
      report.getEntityHealthStatus().add(ehs);
    }
    return this;
  }

  /**
   * Sets the operational status. Updates an existing operational-status entry if one is already
   * present, otherwise adds a new {@link EntityHealthStatusType}.
   *
   * @param status the operational status code
   * @return this builder
   */
  public PositionReportContentTypeBuilder operationalStatus(OperationalStatusCodeType status) {
    var x =
        report.getEntityHealthStatus().stream()
            .filter(item -> item.getOperationalStatus() != null)
            .findAny();
    if (x.isPresent()) {
      x.get().getOperationalStatus().setOperationalStatusCode(status);
    } else {
      var operationalstatus = new OperationalStatusType();
      operationalstatus.setOperationalStatusCode(status);
      var ehs = new EntityHealthStatusType();
      ehs.setOperationalStatus(operationalstatus);
      report.getEntityHealthStatus().add(ehs);
    }
    return this;
  }

  /**
   * Adds a resource entry. Appends to an existing resources entry if one is already present,
   * otherwise adds a new {@link EntityHealthStatusType}.
   *
   * @param resource a builder for the resource to add
   * @return this builder
   */
  public PositionReportContentTypeBuilder addResource(ResourceTypeBuilder resource) {
    var x =
        report.getEntityHealthStatus().stream()
            .filter(item -> item.getResources() != null)
            .findAny();
    if (x.isPresent()) {
      x.get().getResources().getResource().add(resource.build());
    } else {
      var rt = new ResourcesType();
      rt.getResource().add(resource.build());
      var ehs = new EntityHealthStatusType();
      ehs.setResources(rt);
      report.getEntityHealthStatus().add(ehs);
    }

    return this;
  }

  /**
   * Builds and returns the {@link PositionReportContentType}.
   *
   * @return the constructed position report content
   * @throws IllegalArgumentException if {@code timeOfObservation} is not set
   */
  public PositionReportContentType build() {
    if (report.getTimeOfObservation() == null) {
      throw new IllegalArgumentException("TimeOfObservation is null");
    }
    return report;
  }
}
