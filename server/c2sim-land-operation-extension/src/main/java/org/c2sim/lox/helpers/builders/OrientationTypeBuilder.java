package org.c2sim.lox.helpers.builders;

import org.c2sim.lox.schema.EulerAnglesType;
import org.c2sim.lox.schema.HeadingType;
import org.c2sim.lox.schema.OrientationType;

/** Builder for {@link OrientationType} objects. */
public class OrientationTypeBuilder {

  private final OrientationType orientation = new OrientationType();

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static OrientationTypeBuilder create() {
    return new OrientationTypeBuilder();
  }

  /**
   * Sets the orientation to Euler angles (clears any previously set heading).
   *
   * @param eulerAngles the Euler-angles representation of the orientation
   * @return this builder
   */
  public OrientationTypeBuilder eulerAngles(EulerAnglesType eulerAngles) {
    orientation.setHeading(null);
    orientation.setEulerAngles(eulerAngles);
    return this;
  }

  /**
   * Sets the orientation to a heading angle in degrees (clears any previously set Euler angles).
   *
   * @param heading the heading angle in degrees (0–360)
   * @return this builder
   */
  public OrientationTypeBuilder heading(double heading) {
    var headingType = new HeadingType();
    headingType.setHeadingAngle(heading);
    orientation.setHeading(headingType);
    orientation.setEulerAngles(null);
    return this;
  }

  /**
   * Builds and returns the {@link OrientationType}.
   *
   * @return the constructed orientation object
   */
  public OrientationType build() {
    return orientation;
  }
}
