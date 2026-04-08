package org.c2sim.lox.helpers.builders;

import java.util.UUID;
import org.c2sim.lox.schema.LocationObservationType;
import org.c2sim.lox.schema.LocationType;
import org.c2sim.lox.schema.OrientationType;

/**
 * Builder for {@link LocationObservationType} objects.
 *
 * <p>A {@code location} must be set before calling {@link #build()}.
 */
public class LocationObservationTypeBuilder {

  // Prevent instantiation
  private LocationObservationTypeBuilder() {
    throw new AssertionError("Only static functions");
  }

  private final LocationObservationType observation = new LocationObservationType();

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static LocationObservationTypeBuilder create() {
    return new LocationObservationTypeBuilder();
  }

  /**
   * Sets the UUID of the observed actor.
   *
   * @param reference the actor UUID
   * @return this builder
   */
  public LocationObservationTypeBuilder actorReference(UUID reference) {
    observation.setActorReference(reference.toString());
    return this;
  }

  /**
   * Sets the confidence level of the observation.
   *
   * @param confidence the confidence level (0.0–1.0), or {@code null}
   * @return this builder
   */
  public LocationObservationTypeBuilder confidenceLevel(Double confidence) {
    observation.setConfidenceLevel(confidence);
    return this;
  }

  /**
   * Sets the uncertainty interval of the observation.
   *
   * @param interval the uncertainty interval, or {@code null}
   * @return this builder
   */
  public LocationObservationTypeBuilder uncertaintyInterval(Double interval) {
    observation.setUncertaintyInterval(interval);
    return this;
  }

  /**
   * Sets the direction of movement from an {@link OrientationTypeBuilder}.
   *
   * @param direction a builder whose {@link OrientationTypeBuilder#build()} result is used
   * @return this builder
   */
  public LocationObservationTypeBuilder directionOfMovement(OrientationTypeBuilder direction) {
    observation.setDirectionOfMovement(direction.build());
    return this;
  }

  /**
   * Sets the direction of movement directly.
   *
   * @param direction the orientation value
   * @return this builder
   */
  public LocationObservationTypeBuilder directionOfMovement(OrientationType direction) {
    observation.setDirectionOfMovement(direction);
    return this;
  }

  /**
   * Shortcut to set the direction of movement from a heading angle in degrees.
   *
   * @param heading the heading in degrees
   * @return this builder
   */
  // Shortcut for
  public LocationObservationTypeBuilder heading(double heading) {
    directionOfMovement(OrientationTypeBuilder.create().heading(heading).build());
    return this;
  }

  /**
   * Shortcut to set a geodetic location (latitude, longitude, altitude).
   *
   * @param latitude latitude in decimal degrees
   * @param longitude longitude in decimal degrees
   * @param altitude altitude value
   * @param alt the altitude type qualifier
   * @return this builder
   */
  // Shortcut to set geodetic (most used)
  public LocationObservationTypeBuilder location(
      double latitude,
      double longitude,
      double altitude,
      GeodeticCoordinateTypeBuilder.EAltitude alt) {
    location(LocationTypeBuilder.create().createGeodetic(latitude, longitude, altitude, alt));
    return this;
  }

  /**
   * Sets the location from a {@link LocationTypeBuilder}.
   *
   * @param location a builder whose {@link LocationTypeBuilder#build()} result is used
   * @return this builder
   */
  public LocationObservationTypeBuilder location(LocationTypeBuilder location) {
    observation.setLocation(location.build());
    return this;
  }

  /**
   * Sets the location directly.
   *
   * @param location the location value
   * @return this builder
   */
  public LocationObservationTypeBuilder location(LocationType location) {
    observation.setLocation(location);
    return this;
  }

  /**
   * Sets the speed of the observed entity.
   *
   * @param speed the speed value, or {@code null}
   * @return this builder
   */
  public LocationObservationTypeBuilder speed(Double speed) {
    observation.setSpeed(speed);
    return this;
  }

  /**
   * Builds and returns the {@link LocationObservationType}.
   *
   * @return the constructed location observation
   * @throws IllegalStateException if no location has been set
   */
  public LocationObservationType build() {
    if (observation.getLocation() == null) {
      throw new IllegalStateException("location is required");
    }
    return observation;
  }
}
