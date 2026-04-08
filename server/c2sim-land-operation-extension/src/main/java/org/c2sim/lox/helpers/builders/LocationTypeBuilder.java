package org.c2sim.lox.helpers.builders;

import org.c2sim.lox.schema.LocationType;

/**
 * Builder for {@link LocationType} objects.
 *
 * <p>{@link LocationType} is a choice type holding either a geodetic coordinate or a relative
 * location. Exactly one of the two must be set before calling {@link #build()}.
 */
public class LocationTypeBuilder {
  // Prevent instantiation
  private LocationTypeBuilder() {}

  private final LocationType location = new LocationType();

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static LocationTypeBuilder create() {
    return new LocationTypeBuilder();
  }

  /**
   * Sets the location to a geodetic coordinate from a {@link GeodeticCoordinateTypeBuilder} (clears
   * any relative location).
   *
   * @param geodetic a builder whose {@link GeodeticCoordinateTypeBuilder#build()} result is used
   * @return this builder
   */
  public LocationTypeBuilder createGeodetic(GeodeticCoordinateTypeBuilder geodetic) {
    location.setRelativeLocation(null);
    location.setGeodeticCoordinate(geodetic.build());
    return this;
  }

  /**
   * Shortcut to set a geodetic coordinate from individual components (clears any relative
   * location).
   *
   * @param latitude latitude in decimal degrees
   * @param longitude longitude in decimal degrees
   * @param altitude altitude value
   * @param alt the altitude type qualifier
   * @return this builder
   */
  public LocationTypeBuilder createGeodetic(
      double latitude,
      double longitude,
      double altitude,
      GeodeticCoordinateTypeBuilder.EAltitude alt) {
    createGeodetic(GeodeticCoordinateTypeBuilder.create(latitude, longitude, altitude, alt));
    return this;
  }

  /**
   * Sets the location to a relative location from a {@link RelativeLocationTypeBuilder} (clears any
   * geodetic coordinate).
   *
   * @param relative a builder whose {@link RelativeLocationTypeBuilder#build()} result is used
   * @return this builder
   */
  public LocationTypeBuilder createRelative(RelativeLocationTypeBuilder relative) {
    location.setGeodeticCoordinate(null);
    location.setRelativeLocation(relative.build());
    return this;
  }

  /**
   * Builds and returns the {@link LocationType}.
   *
   * @return the constructed location object
   * @throws IllegalArgumentException if neither a geodetic coordinate nor a relative location has
   *     been set
   */
  public LocationType build() {
    if (location.getRelativeLocation() == null && location.getGeodeticCoordinate() == null) {
      throw new IllegalArgumentException("Location is null");
    }
    return location;
  }
}
