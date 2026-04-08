package org.c2sim.lox.helpers.builders;

import java.util.UUID;
import org.c2sim.lox.schema.RelativeLocationType;

/**
 * Builder for {@link RelativeLocationType} objects.
 *
 * <p>A relative location references another entity by UUID rather than specifying absolute geodetic
 * coordinates.
 */
public class RelativeLocationTypeBuilder {
  private final RelativeLocationType relLoc;

  /**
   * Creates a new builder with the given entity reference UUID.
   *
   * @param entityRef the UUID of the entity used as the reference point
   * @return a new builder instance
   */
  public static RelativeLocationTypeBuilder create(UUID entityRef) {
    return new RelativeLocationTypeBuilder(entityRef);
  }

  /**
   * Constructs a builder with the given entity reference UUID.
   *
   * @param entityRef the UUID of the entity used as the reference point
   */
  public RelativeLocationTypeBuilder(UUID entityRef) {
    relLoc = new RelativeLocationType();
    relLoc.setEntityReference(entityRef.toString());
  }

  /**
   * Builds and returns the {@link RelativeLocationType}.
   *
   * @return the constructed relative location object
   */
  public RelativeLocationType build() {
    return relLoc;
  }
}
