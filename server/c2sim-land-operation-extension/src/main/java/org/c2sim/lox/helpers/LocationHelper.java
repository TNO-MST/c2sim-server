package org.c2sim.lox.helpers;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.c2sim.lox.schema.*;

/**
 * Utility methods for reading and writing the geodetic location of C2SIM entities.
 *
 * <p>The LOX XSD does not generate a common supertype with a {@code getLocation()} method, so this
 * class provides per-type overloads for every entity kind that can carry a location (units,
 * persons, vehicles, vessels, aircraft, cultural/geographic features, and raw entity states).
 *
 * <p>Limitations (by design):
 *
 * <ul>
 *   <li>Only the first geodetic coordinate is returned when there is exactly one; {@link
 *       Optional#empty()} is returned when there are zero or more than one.
 *   <li>Relative positions are not yet supported and always yield {@link Optional#empty()}.
 * </ul>
 */
@SuppressWarnings("unused")
public class LocationHelper {

  private LocationHelper() {
    throw new AssertionError("Only static functions");
  }

  /*

  The following classes can have a location:
  Actors:
   - Unit
   - NonMilitaryOrganization
   - Person
   - Aircraft
   - Vehicle
   - SurfaceVessel
   - SubsurfaceVessel

   - CulturalFeature
   - GeographicFeature
   - EntityState

   The XSD code generator doesn't generate a super function to get location

   Not implemented:
   - Assumes there is only one location, the data model supports multiple locations
     (return null when there are multiple locations)
   - Relative position to other entity (returns null for now)

    */

  /**
   * Returns all geodetic coordinates of the given tactical area feature's current state.
   *
   * @param areaFeature the area feature, or {@code null}
   * @return an {@link Optional} containing the coordinate list, or empty if not present
   */
  public static Optional<List<GeodeticCoordinateType>> getLocations(TacticalAreaType areaFeature) {
    return (areaFeature != null) ? getLocations(areaFeature.getCurrentState()) : Optional.empty();
  }

  /**
   * Returns the geodetic location of the given point feature's current state.
   *
   * @param pointFeature the point feature, or {@code null}
   * @return an {@link Optional} containing the coordinate, or empty if not present
   */
  public static Optional<GeodeticCoordinateType> getLocation(PointType pointFeature) {
    return (pointFeature != null) ? getLocation(pointFeature.getCurrentState()) : Optional.empty();
  }

  /**
   * Returns the geodetic location of the given geographic feature's current state.
   *
   * @param geographicFeature the geographic feature, or {@code null}
   * @return an {@link Optional} containing the coordinate, or empty if not present
   */
  public static Optional<GeodeticCoordinateType> getLocation(
      GeographicFeatureType geographicFeature) {
    return (geographicFeature != null)
        ? getLocation(geographicFeature.getCurrentState())
        : Optional.empty();
  }

  /**
   * Returns the geodetic location of the given subsurface vessel's current state.
   *
   * @param subsurfaceVessel the subsurface vessel, or {@code null}
   * @return an {@link Optional} containing the coordinate, or empty if not present
   */
  public static Optional<GeodeticCoordinateType> getLocation(
      SubsurfaceVesselType subsurfaceVessel) {
    return (subsurfaceVessel != null)
        ? getLocation(subsurfaceVessel.getCurrentState())
        : Optional.empty();
  }

  /**
   * Returns the geodetic location of the given surface vessel's current state.
   *
   * @param surfaceVessel the surface vessel, or {@code null}
   * @return an {@link Optional} containing the coordinate, or empty if not present
   */
  public static Optional<GeodeticCoordinateType> getLocation(SurfaceVesselType surfaceVessel) {
    return (surfaceVessel != null)
        ? getLocation(surfaceVessel.getCurrentState())
        : Optional.empty();
  }

  /**
   * Returns the geodetic location of the given non-military organization's current state.
   *
   * @param nonMilitary the non-military organization, or {@code null}
   * @return an {@link Optional} containing the coordinate, or empty if not present
   */
  public static Optional<GeodeticCoordinateType> getLocation(
      NonMilitaryOrganizationType nonMilitary) {
    return (nonMilitary != null) ? getLocation(nonMilitary.getCurrentState()) : Optional.empty();
  }

  /**
   * Returns the geodetic location of the given person's current state.
   *
   * @param person the person entity, or {@code null}
   * @return an {@link Optional} containing the coordinate, or empty if not present
   */
  public static Optional<GeodeticCoordinateType> getLocation(PersonType person) {
    return (person != null) ? getLocation(person.getCurrentState()) : Optional.empty();
  }

  /**
   * Returns the geodetic location of the given cultural feature's current state.
   *
   * @param feature the cultural feature, or {@code null}
   * @return an {@link Optional} containing the coordinate, or empty if not present
   */
  public static Optional<GeodeticCoordinateType> getLocation(CulturalFeatureType feature) {
    return (feature != null) ? getLocation(feature.getCurrentState()) : Optional.empty();
  }

  /**
   * Returns the geodetic location of the given aircraft's current state.
   *
   * @param aircraft the aircraft entity, or {@code null}
   * @return an {@link Optional} containing the coordinate, or empty if not present
   */
  public static Optional<GeodeticCoordinateType> getLocation(AircraftType aircraft) {
    return (aircraft != null) ? getLocation(aircraft.getCurrentState()) : Optional.empty();
  }

  /**
   * Returns the geodetic location of the given vehicle's current state.
   *
   * @param vehicle the vehicle entity, or {@code null}
   * @return an {@link Optional} containing the coordinate, or empty if not present
   */
  public static Optional<GeodeticCoordinateType> getLocation(VehicleType vehicle) {
    return (vehicle != null) ? getLocation(vehicle.getCurrentState()) : Optional.empty();
  }

  /**
   * Returns the geodetic location of the given unit's current state.
   *
   * @param unit the unit entity, or {@code null}
   * @return an {@link Optional} containing the coordinate, or empty if not present
   */
  public static Optional<GeodeticCoordinateType> getLocation(UnitType unit) {
    return (unit != null) ? getLocation(unit.getCurrentState()) : Optional.empty();
  }

  /**
   * Returns all geodetic coordinates from the given entity state's physical location list.
   *
   * <p>Relative positions are not supported and are excluded from the result.
   *
   * @param entity the entity state, or {@code null}
   * @return an {@link Optional} containing the list of geodetic coordinates, or empty if none
   */
  public static Optional<List<GeodeticCoordinateType>> getLocations(EntityStateType entity) {

    // TODO Relative positions not supported
    if ((entity != null)
        && (entity.getPhysicalState() != null)
        && (entity.getPhysicalState().getLocation() != null)
        && (entity.getPhysicalState().getLocation().stream()
            .anyMatch(x -> x.getGeodeticCoordinate() != null))) {

      return Optional.of(
          entity.getPhysicalState().getLocation().stream()
              .map(LocationType::getGeodeticCoordinate)
              .filter(Objects::nonNull)
              .toList());
    }

    return Optional.empty();
  }

  /**
   * Returns the single geodetic coordinate of the given entity state, or empty when there are zero
   * or more than one coordinates.
   *
   * @param entity the entity state, or {@code null}
   * @return an {@link Optional} containing the coordinate, or empty
   */
  public static Optional<GeodeticCoordinateType> getLocation(EntityStateType entity) {
    var locations = getLocations(entity);
    // Expect only one coordinate
    return (locations.isPresent() && locations.get().size() == 1)
        ? Optional.of(locations.get().getFirst())
        : Optional.empty();
  }

  /**
   * Sets the geodetic location on the given entity state, replacing any existing location entries.
   *
   * <p>If {@code location} is {@code null} the location list is cleared. If {@code currentState} is
   * {@code null} the method does nothing.
   *
   * @param currentState the entity state to update, or {@code null}
   * @param location the new geodetic coordinate, or {@code null} to clear the location
   */
  public static void setLocation(EntityStateType currentState, GeodeticCoordinateType location) {

    if (currentState != null) {
      if (currentState.getPhysicalState() == null) {
        currentState.setPhysicalState(new PhysicalStateType());
      }
      if (location != null) {
        var lt = new LocationType();
        lt.setGeodeticCoordinate(location);
        currentState.getPhysicalState().getLocation().clear();
        currentState.getPhysicalState().getLocation().add(lt);
      } else {
        currentState.getPhysicalState().getLocation().clear();
      }
    }
  }
}
