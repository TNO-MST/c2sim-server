package org.c2sim.lox.helpers.builders;

import java.util.UUID;
import org.c2sim.lox.schema.MagicMoveType;

/**
 * Builder for {@link MagicMoveType} objects.
 *
 * <p>Both {@code entityReference} and {@code location} are required before calling {@link
 * #build()}.
 */
public class MagicMoveTypeBuilder {

  // Prevent instantiation
  private MagicMoveTypeBuilder() {}

  private final MagicMoveType magicMove = new MagicMoveType();

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static MagicMoveTypeBuilder create() {
    return new MagicMoveTypeBuilder();
  }

  /**
   * Sets the UUID of the entity to be teleported.
   *
   * @param id the entity UUID
   * @return this builder
   */
  public MagicMoveTypeBuilder entityReference(UUID id) {
    magicMove.setEntityReference(id.toString());
    return this;
  }

  /**
   * Sets the target location from individual geodetic components.
   *
   * @param latitude latitude in decimal degrees
   * @param longitude longitude in decimal degrees
   * @param altitude altitude value
   * @param alt the altitude type qualifier
   * @return this builder
   */
  public MagicMoveTypeBuilder location(
      double latitude,
      double longitude,
      double altitude,
      GeodeticCoordinateTypeBuilder.EAltitude alt) {
    var loc = LocationTypeBuilder.create().createGeodetic(latitude, longitude, altitude, alt);
    magicMove.setLocation(loc.build());
    return this;
  }

  /**
   * Sets the target location from a {@link LocationTypeBuilder}.
   *
   * @param loc a builder whose {@link LocationTypeBuilder#build()} result is used
   * @return this builder
   */
  public MagicMoveTypeBuilder location(LocationTypeBuilder loc) {
    magicMove.setLocation(loc.build());
    return this;
  }

  /**
   * Sets the tasker (the system that issued the command).
   *
   * @param id the tasker UUID
   * @return this builder
   */
  public MagicMoveTypeBuilder tasker(UUID id) {
    magicMove.setTasker(id.toString());
    return this;
  }

  /**
   * Sets the task ID.
   *
   * @param id the task UUID
   * @return this builder
   */
  public MagicMoveTypeBuilder taskId(UUID id) {
    magicMove.setTaskID(id.toString());
    return this;
  }

  /**
   * Builds and returns the {@link MagicMoveType}.
   *
   * @return the constructed magic-move object
   * @throws IllegalArgumentException if {@code entityReference} or {@code location} is not set
   */
  public MagicMoveType build() {
    if (magicMove.getEntityReference() == null) {
      throw new IllegalArgumentException("EntityReference is null");
    }
    if (magicMove.getLocation() == null) {
      throw new IllegalArgumentException("Location is null");
    }
    return magicMove;
  }
}
