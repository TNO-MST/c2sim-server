package org.c2sim.lox.helpers.builders;

import org.c2sim.lox.schema.GeodeticCoordinateType;

/**
 * Builder for {@link GeodeticCoordinateType} objects.
 *
 * <p>The altitude is stored in either the {@code altitudeMSL} or {@code altitudeAGL} field
 * depending on the {@link EAltitude} qualifier; both fields are set to {@code null} when {@link
 * EAltitude#NONE} is chosen.
 */
public class GeodeticCoordinateTypeBuilder {
  private final GeodeticCoordinateType geodetic;

  /** Qualifier that selects how the altitude value is interpreted. */
  public enum EAltitude {
    /** Altitude above ground level. */
    ABOVE_GROUND_LEVEL,
    /** Altitude above mean sea level. */
    MEAN_SEA_LEVEL,
    /** No altitude information; both altitude fields are set to {@code null}. */
    NONE
  }

  /**
   * Creates a new builder for the given geodetic position.
   *
   * @param latitude latitude in decimal degrees
   * @param longitude longitude in decimal degrees
   * @param altitude the altitude value, interpreted according to {@code alt}
   * @param alt specifies whether {@code altitude} is AGL, MSL, or absent
   * @return a new builder instance
   */
  public static GeodeticCoordinateTypeBuilder create(
      double latitude, double longitude, double altitude, EAltitude alt) {
    return new GeodeticCoordinateTypeBuilder(latitude, longitude, altitude, alt);
  }

  private GeodeticCoordinateTypeBuilder(
      double latitude, double longitude, double altitude, EAltitude alt) {
    geodetic = new GeodeticCoordinateType();
    geodetic.setLatitude(latitude);
    geodetic.setLongitude(longitude);
    switch (alt) {
      case MEAN_SEA_LEVEL -> geodetic.setAltitudeMSL(altitude);
      case ABOVE_GROUND_LEVEL -> geodetic.setAltitudeAGL(altitude);
      default -> {
        geodetic.setAltitudeMSL(null);
        geodetic.setAltitudeAGL(null);
      }
    }
  }

  /**
   * Builds and returns the {@link GeodeticCoordinateType}.
   *
   * @return the constructed geodetic coordinate object
   */
  public GeodeticCoordinateType build() {
    return geodetic;
  }
}
