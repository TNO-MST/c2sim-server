package org.c2sim.lox.helpers;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.c2sim.lox.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for traversing and querying a {@link C2SIMInitializationBodyType} object graph.
 *
 * <p>The LOX XSD generates a deeply nested object hierarchy ({@code ObjectDefinitions → Entity →
 * ActorEntity → ...}) without common super-type accessors. This class provides per-entity-kind
 * iterators and search methods that hide the nesting.
 *
 * <p>Each entity kind has two overloads:
 *
 * <ul>
 *   <li>{@code getXxx(msg, consumer)} — visits every entity of that kind.
 *   <li>{@code getXxx(msg, filter, consumer)} — visits only those that satisfy the predicate.
 * </ul>
 *
 * <p>Symbol helpers ({@code getAPP6SIDC}, {@code getSISOEntityType}) are provided for every entity
 * kind that carries an {@code APP6CSymbol} or {@code SISOEntityType} field.
 *
 * <p>Due to bad design of the XSD, it is not real possible to generalize the functions
 */
@SuppressWarnings("checkstyle:LineLength")
public final class C2SIMInitializationBodyTypeHelper {

  private static final Logger logger =
      LoggerFactory.getLogger(C2SIMInitializationBodyTypeHelper.class);

  // Prevent instantiation
  private C2SIMInitializationBodyTypeHelper() {
    throw new AssertionError("Only static functions");
  }

  private static String assertString(String text) {
    return (text == null) ? "" : text;
  }

  /**
   * Returns the APP-6C Symbol Identification Code (SIDC) for the given tactical area.
   *
   * @param areaType the area type entity
   * @return the SIDC string, or an empty string if not set
   */
  public static String getAPP6SIDC(final TacticalAreaType areaType) {
    return getAPP6SIDC(areaType.getAPP6CSymbol());
  }

  /**
   * Returns the APP-6C SIDC for the given point feature.
   *
   * @param pointType the point feature entity
   * @return the SIDC string, or an empty string if not set
   */
  public static String getAPP6SIDC(final PointType pointType) {

    return getAPP6SIDC(pointType.getAPP6CSymbol());
  }

  /**
   * Returns the APP-6C SIDC for the given route.
   *
   * @param routeType the route entity
   * @return the SIDC string, or an empty string if not set
   */
  public static String getAPP6SIDC(final RouteType routeType) {

    return getAPP6SIDC(routeType.getAPP6CSymbol());
  }

  /**
   * Returns the APP-6C SIDC for the given geographic feature.
   *
   * @param geographicFeatureType the geographic feature entity
   * @return the SIDC string, or an empty string if not set
   */
  public static String getAPP6SIDC(final GeographicFeatureType geographicFeatureType) {
    return getAPP6SIDC(geographicFeatureType.getAPP6CSymbol());
  }

  /**
   * Returns the APP-6C SIDC for the given subsurface vessel.
   *
   * @param subsurfaceVessel the subsurface vessel entity
   * @return the SIDC string, or an empty string if not set
   */
  public static String getAPP6SIDC(final SubsurfaceVesselType subsurfaceVessel) {
    return getAPP6SIDC(subsurfaceVessel.getAPP6CSymbol());
  }

  /**
   * Returns the APP-6C SIDC for the given surface vessel.
   *
   * @param surfaceVessel the surface vessel entity
   * @return the SIDC string, or an empty string if not set
   */
  public static String getAPP6SIDC(final SurfaceVesselType surfaceVessel) {
    return getAPP6SIDC(surfaceVessel.getAPP6CSymbol());
  }

  /**
   * Returns the APP-6C SIDC for the given non-military organization.
   *
   * @param nonMilitary the non-military organization entity
   * @return the SIDC string, or an empty string if not set
   */
  public static String getAPP6SIDC(final NonMilitaryOrganizationType nonMilitary) {
    return getAPP6SIDC(nonMilitary.getAPP6CSymbol());
  }

  /**
   * Returns the APP-6C SIDC for the given person.
   *
   * @param person the person entity
   * @return the SIDC string, or an empty string if not set
   */
  public static String getAPP6SIDC(final PersonType person) {
    return getAPP6SIDC(person.getAPP6CSymbol());
  }

  /**
   * Returns the APP-6C SIDC for the given cultural feature.
   *
   * @param culturalfeature the cultural feature entity
   * @return the SIDC string, or an empty string if not set
   */
  public static String getAPP6SIDC(final CulturalFeatureType culturalfeature) {
    return getAPP6SIDC(culturalfeature.getAPP6CSymbol());
  }

  /**
   * Returns the APP-6C SIDC for the given aircraft.
   *
   * @param aircraft the aircraft entity
   * @return the SIDC string, or an empty string if not set
   */
  public static String getAPP6SIDC(final AircraftType aircraft) {

    return getAPP6SIDC(aircraft.getAPP6CSymbol());
  }

  /**
   * Returns the APP-6C SIDC for the given unit.
   *
   * @param unit the unit entity
   * @return the SIDC string, or an empty string if not set
   */
  public static String getAPP6SIDC(final UnitType unit) {

    return getAPP6SIDC(unit.getAPP6CSymbol());
  }

  /**
   * Returns the APP-6C SIDC for the given vehicle.
   *
   * @param vehicle the vehicle entity
   * @return the SIDC string, or an empty string if not set
   */
  public static String getAPP6SIDC(final VehicleType vehicle) {

    return getAPP6SIDC(vehicle.getAPP6CSymbol());
  }

  /**
   * Returns the APP-6C SIDC string from the given symbol object.
   *
   * @param app6sidc the symbol object, or {@code null}
   * @return the SIDC string, or an empty string when {@code app6sidc} is {@code null} or has no
   *     value
   */
  public static String getAPP6SIDC(final APP6CSymbolType app6sidc) {
    return (app6sidc != null) ? assertString(app6sidc.getAPP6CSIDC()) : "";
  }

  /**
   * Returns the SISO entity type string for the given tactical area.
   *
   * @param areaFeature the area feature entity
   * @return a dot-separated DIS entity type string, or an empty string if not set
   */
  public static String getSISOEntityType(final TacticalAreaType areaFeature) {
    return getSISOEntityType(areaFeature.getSISOEntityType());
  }

  /**
   * Returns the SISO entity type string for the given point feature.
   *
   * @param pointFeature the point feature entity
   * @return a dot-separated DIS entity type string, or an empty string if not set
   */
  public static String getSISOEntityType(final PointType pointFeature) {
    return getSISOEntityType(pointFeature.getSISOEntityType());
  }

  /**
   * Returns the SISO entity type string for the given route.
   *
   * @param routeFeature the route entity
   * @return a dot-separated DIS entity type string, or an empty string if not set
   */
  public static String getSISOEntityType(final RouteType routeFeature) {
    return getSISOEntityType(routeFeature.getSISOEntityType());
  }

  /**
   * Returns the SISO entity type string for the given geographic feature.
   *
   * @param geographicFeature the geographic feature entity
   * @return a dot-separated DIS entity type string, or an empty string if not set
   */
  public static String getSISOEntityType(final GeographicFeatureType geographicFeature) {
    return getSISOEntityType(geographicFeature.getSISOEntityType());
  }

  /**
   * Returns the SISO entity type string for the given subsurface vessel.
   *
   * @param subsurfaceVessel the subsurface vessel entity
   * @return a dot-separated DIS entity type string, or an empty string if not set
   */
  public static String getSISOEntityType(final SubsurfaceVesselType subsurfaceVessel) {
    return getSISOEntityType(subsurfaceVessel.getSISOEntityType());
  }

  /**
   * Returns the SISO entity type string for the given surface vessel.
   *
   * @param surfaceVessel the surface vessel entity
   * @return a dot-separated DIS entity type string, or an empty string if not set
   */
  public static String getSISOEntityType(final SurfaceVesselType surfaceVessel) {
    return getSISOEntityType(surfaceVessel.getSISOEntityType());
  }

  /**
   * Returns the SISO entity type string for the given person.
   *
   * @param person the person entity
   * @return a dot-separated DIS entity type string, or an empty string if not set
   */
  public static String getSISOEntityType(final PersonType person) {
    return getSISOEntityType(person.getSISOEntityType());
  }

  /**
   * Returns the SISO entity type string for the given cultural feature.
   *
   * @param culturalFeature the cultural feature entity
   * @return a dot-separated DIS entity type string, or an empty string if not set
   */
  public static String getSISOEntityType(final CulturalFeatureType culturalFeature) {
    return getSISOEntityType(culturalFeature.getSISOEntityType());
  }

  /**
   * Returns the SISO entity type string for the given non-military organization.
   *
   * @param nonMilitary the non-military organization entity
   * @return a dot-separated DIS entity type string, or an empty string if not set
   */
  public static String getSISOEntityType(final NonMilitaryOrganizationType nonMilitary) {
    return getSISOEntityType(nonMilitary.getSISOEntityType());
  }

  /**
   * Returns the SISO entity type string for the given aircraft.
   *
   * @param aircraft the aircraft entity
   * @return a dot-separated DIS entity type string, or an empty string if not set
   */
  public static String getSISOEntityType(final AircraftType aircraft) {
    return getSISOEntityType(aircraft.getSISOEntityType());
  }

  /**
   * Returns the SISO entity type string for the given unit.
   *
   * @param unit the unit entity
   * @return a dot-separated DIS entity type string, or an empty string if not set
   */
  public static String getSISOEntityType(final UnitType unit) {
    return getSISOEntityType(unit.getSISOEntityType());
  }

  /**
   * Returns the SISO entity type string for the given vehicle.
   *
   * @param vehicle the vehicle entity
   * @return a dot-separated DIS entity type string, or an empty string if not set
   */
  public static String getSISOEntityType(final VehicleType vehicle) {
    return getSISOEntityType(vehicle.getSISOEntityType());
  }

  /**
   * Returns the dot-separated DIS entity type string for the given SISO entity type object.
   *
   * @param entiyType the SISO entity type object, or {@code null}
   * @return a string in the form {@code kind.domain.country.category.subCategory.specific.extra},
   *     or an empty string when {@code entiyType} is {@code null}
   */
  public static String getSISOEntityType(final SISOEntityTypeType entiyType) {
    return (entiyType != null) ? convertToText(entiyType) : "";
  }

  /**
   * Formats a {@link SISOEntityTypeType} as a dot-separated DIS entity type string.
   *
   * @param entityType the SISO entity type object
   * @return a string in the form {@code kind.domain.country.category.subCategory.specific.extra}
   */
  public static String convertToText(SISOEntityTypeType entityType) {
    return String.format(
        "%d.%d.%d.%d.%d.%d.%d",
        entityType.getDISKind(),
        entityType.getDISDomain(),
        entityType.getDISCountry().intValue(),
        entityType.getDISCategory(),
        entityType.getDISSubCategory(),
        entityType.getDISSpecific(),
        entityType.getDISExtra());
  }

  /**
   * Invokes {@code consumer} for every {@link UnitType} in the initialization body.
   *
   * @param msg the initialization body
   * @param consumer the callback invoked for each unit
   */
  public static void getUnits(
      final C2SIMInitializationBodyType msg, final Consumer<UnitType> consumer) {
    getUnits(msg, unit -> true /* no filtering */, consumer);
  }

  /**
   * Invokes {@code consumer} for every {@link ForceSideType} in the initialization body.
   *
   * @param msg the initialization body
   * @param consumer the callback invoked for each force side
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static void getForceSides(
      final C2SIMInitializationBodyType msg, final Consumer<ForceSideType> consumer) {
    msg.getObjectDefinitions()
        .forEach(
            objectDefinition -> {
              if ((objectDefinition != null) && (objectDefinition.getAbstractObject() != null)) {
                for (AbstractObjectType obj : objectDefinition.getAbstractObject()) {
                  if (obj.getForceSide() != null) {
                    consumer.accept(obj.getForceSide());
                  }
                }
              }
            });
  }

  /**
   * Invokes {@code consumer} for every {@link UnitType} in the initialization body that satisfies
   * {@code filter}.
   *
   * @param msg the initialization body
   * @param filter predicate to select which units are passed to the consumer
   * @param consumer the callback invoked for each matching unit
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static void getUnits(
      final C2SIMInitializationBodyType msg,
      Predicate<UnitType> filter,
      final Consumer<UnitType> consumer) {
    // ObjectDefinitions -> Entity -> ActorEntity ->CollectiveEntity -> MilitaryOrganization
    msg.getObjectDefinitions()
        .forEach(
            objectDefinition -> {
              if ((objectDefinition != null) && (objectDefinition.getEntity() != null)) {
                objectDefinition
                    .getEntity()
                    .forEach(
                        entity -> {
                          if ((entity.getActorEntity() != null)
                              && (entity.getActorEntity().getCollectiveEntity() != null)
                              && (entity
                                      .getActorEntity()
                                      .getCollectiveEntity()
                                      .getMilitaryOrganization()
                                  != null)
                              && (entity
                                      .getActorEntity()
                                      .getCollectiveEntity()
                                      .getMilitaryOrganization()
                                      .getUnit()
                                  != null)) {

                            UnitType unit =
                                entity
                                    .getActorEntity()
                                    .getCollectiveEntity()
                                    .getMilitaryOrganization()
                                    .getUnit();
                            if (filter.test(unit)) {
                              consumer.accept(unit);
                            }
                          }
                        });
              }
            });
  }

  /**
   * Invokes {@code consumer} for every {@link NonMilitaryOrganizationType} in the initialization
   * body.
   *
   * @param msg the initialization body
   * @param consumer the callback invoked for each non-military organization
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static void getNonMilitaryOrganizations(
      final C2SIMInitializationBodyType msg, final Consumer<NonMilitaryOrganizationType> consumer) {
    getNonMilitaryOrganizations(msg, organization -> true /* no filtering */, consumer);
  }

  /**
   * Invokes {@code consumer} for every {@link NonMilitaryOrganizationType} in the initialization
   * body that satisfies {@code filter}.
   *
   * @param msg the initialization body
   * @param filter predicate to select which organizations are passed to the consumer
   * @param consumer the callback invoked for each matching organization
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static void getNonMilitaryOrganizations(
      final C2SIMInitializationBodyType msg,
      Predicate<NonMilitaryOrganizationType> filter,
      final Consumer<NonMilitaryOrganizationType> consumer) {
    // ObjectDefinitions -> Entity -> ActorEntity ->CollectiveEntity -> NonMilitaryOrganization
    msg.getObjectDefinitions()
        .forEach(
            objectDefinition -> {
              if ((objectDefinition != null) && (objectDefinition.getEntity() != null)) {
                objectDefinition
                    .getEntity()
                    .forEach(
                        entity -> {
                          if ((entity.getActorEntity() != null)
                              && (entity.getActorEntity().getCollectiveEntity() != null)
                              && (entity
                                      .getActorEntity()
                                      .getCollectiveEntity()
                                      .getNonMilitaryOrganization()
                                  != null)) {
                            NonMilitaryOrganizationType organization =
                                entity
                                    .getActorEntity()
                                    .getCollectiveEntity()
                                    .getNonMilitaryOrganization();
                            if (filter.test(organization)) {
                              consumer.accept(organization);
                            }
                          }
                        });
              }
            });
  }

  /**
   * Finds the first {@link NonMilitaryOrganizationType} whose UUID matches {@code uuid}
   * (case-insensitive).
   *
   * @param msg the initialization body
   * @param uuid the UUID to search for
   * @return an {@link Optional} containing the matching entity, or empty if not found
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static Optional<NonMilitaryOrganizationType> findNonMilitaryOrganizationByUuid(
      final C2SIMInitializationBodyType msg, final String uuid) {
    AtomicReference<NonMilitaryOrganizationType> result = new AtomicReference<>(null);
    getNonMilitaryOrganizations(
        msg,
        nonMilitaryOrganization -> {
          if (uuid.equalsIgnoreCase(nonMilitaryOrganization.getUUID())) {
            result.getAndSet(nonMilitaryOrganization);
          }
        });
    return Optional.ofNullable(result.get());
  }

  // PERSONS

  /**
   * Invokes {@code consumer} for every {@link PersonType} in the initialization body.
   *
   * @param msg the initialization body
   * @param consumer the callback invoked for each person
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static void getPersons(
      final C2SIMInitializationBodyType msg, final Consumer<PersonType> consumer) {
    getPersons(msg, organization -> true /* no filtering */, consumer);
  }

  /**
   * Invokes {@code consumer} for every {@link PersonType} in the initialization body that satisfies
   * {@code filter}.
   *
   * @param msg the initialization body
   * @param filter predicate to select which persons are passed to the consumer
   * @param consumer the callback invoked for each matching person
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static void getPersons(
      final C2SIMInitializationBodyType msg,
      Predicate<PersonType> filter,
      final Consumer<PersonType> consumer) {
    // ObjectDefinitions -> Entity -> ActorEntity -> Person
    msg.getObjectDefinitions()
        .forEach(
            objectDefinition -> {
              if ((objectDefinition != null) && (objectDefinition.getEntity() != null)) {
                objectDefinition
                    .getEntity()
                    .forEach(
                        entity -> {
                          if ((entity.getActorEntity() != null)
                              && (entity.getActorEntity().getPerson() != null)) {
                            PersonType person = entity.getActorEntity().getPerson();
                            if (filter.test(person)) {
                              consumer.accept(person);
                            }
                          }
                        });
              }
            });
  }

  /**
   * Finds the first {@link VehicleType} whose UUID matches {@code uuid} (case-insensitive).
   *
   * @param msg the initialization body
   * @param uuid the UUID to search for
   * @return an {@link Optional} containing the matching vehicle, or empty if not found
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static Optional<VehicleType> findVehicleByUuid(
      final C2SIMInitializationBodyType msg, final String uuid) {
    AtomicReference<VehicleType> result = new AtomicReference<>(null);
    getVehicles(
        msg,
        vehicle -> {
          if (uuid.equalsIgnoreCase(vehicle.getUUID())) {
            result.getAndSet(vehicle);
          }
        });
    return Optional.ofNullable(result.get());
  }

  // AIRCRAFTS

  /**
   * Invokes {@code consumer} for every {@link AircraftType} in the initialization body.
   *
   * @param msg the initialization body
   * @param consumer the callback invoked for each aircraft
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static void getAircrafts(
      final C2SIMInitializationBodyType msg, final Consumer<AircraftType> consumer) {
    getAircrafts(msg, aircraft -> true /* no filtering */, consumer);
  }

  /**
   * Invokes {@code consumer} for every {@link AircraftType} in the initialization body that
   * satisfies {@code filter}.
   *
   * @param msg the initialization body
   * @param filter predicate to select which aircraft are passed to the consumer
   * @param consumer the callback invoked for each matching aircraft
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static void getAircrafts(
      final C2SIMInitializationBodyType msg,
      Predicate<AircraftType> filter,
      final Consumer<AircraftType> consumer) {
    // ObjectDefinitions -> Entity -> ActorEntity -> Platform -> Aircraft
    msg.getObjectDefinitions()
        .forEach(
            objectDefinition -> {
              if ((objectDefinition != null) && (objectDefinition.getEntity() != null)) {
                objectDefinition
                    .getEntity()
                    .forEach(
                        entity -> {
                          if ((entity.getActorEntity() != null)
                              && (entity.getActorEntity().getPlatform() != null)
                              && (entity.getActorEntity().getPlatform().getAircraft() != null)) {
                            AircraftType aircraft =
                                entity.getActorEntity().getPlatform().getAircraft();
                            if (filter.test(aircraft)) {
                              consumer.accept(aircraft);
                            }
                          }
                        });
              }
            });
  }

  // SUB SURFACE VESSELS

  /**
   * Invokes {@code consumer} for every {@link SubsurfaceVesselType} in the initialization body.
   *
   * @param msg the initialization body
   * @param consumer the callback invoked for each subsurface vessel
   */
  public static void getSubSurfaceVessels(
      final C2SIMInitializationBodyType msg, final Consumer<SubsurfaceVesselType> consumer) {
    getSubSurfaceVessels(msg, subsurfaceVessel -> true /* no filtering */, consumer);
  }

  /**
   * Invokes {@code consumer} for every {@link SubsurfaceVesselType} in the initialization body that
   * satisfies {@code filter}.
   *
   * @param msg the initialization body
   * @param filter predicate to select which vessels are passed to the consumer
   * @param consumer the callback invoked for each matching subsurface vessel
   */
  public static void getSubSurfaceVessels(
      final C2SIMInitializationBodyType msg,
      Predicate<SubsurfaceVesselType> filter,
      final Consumer<SubsurfaceVesselType> consumer) {
    // ObjectDefinitions -> Entity -> ActorEntity -> Platform -> SubsurfaceVessel
    msg.getObjectDefinitions()
        .forEach(
            objectDefinition -> {
              if ((objectDefinition != null) && (objectDefinition.getEntity() != null)) {
                objectDefinition
                    .getEntity()
                    .forEach(
                        entity -> {
                          if ((entity.getActorEntity() != null)
                              && (entity.getActorEntity().getPlatform() != null)
                              && (entity.getActorEntity().getPlatform().getSubsurfaceVessel()
                                  != null)) {
                            SubsurfaceVesselType subSurfaceVessel =
                                entity.getActorEntity().getPlatform().getSubsurfaceVessel();
                            if (filter.test(subSurfaceVessel)) {
                              consumer.accept(subSurfaceVessel);
                            }
                          }
                        });
              }
            });
  }

  // SURFACE VESSELS

  /**
   * Invokes {@code consumer} for every {@link SurfaceVesselType} in the initialization body.
   *
   * @param msg the initialization body
   * @param consumer the callback invoked for each surface vessel
   */
  public static void getSurfaceVessels(
      final C2SIMInitializationBodyType msg, final Consumer<SurfaceVesselType> consumer) {
    getSurfaceVessels(msg, subsurfaceVessel -> true /* no filtering */, consumer);
  }

  /**
   * Invokes {@code consumer} for every {@link SurfaceVesselType} in the initialization body that
   * satisfies {@code filter}.
   *
   * @param msg the initialization body
   * @param filter predicate to select which vessels are passed to the consumer
   * @param consumer the callback invoked for each matching surface vessel
   */
  public static void getSurfaceVessels(
      final C2SIMInitializationBodyType msg,
      Predicate<SurfaceVesselType> filter,
      final Consumer<SurfaceVesselType> consumer) {
    // ObjectDefinitions -> Entity -> ActorEntity -> Platform -> SurfaceVessel
    msg.getObjectDefinitions()
        .forEach(
            objectDefinition -> {
              if ((objectDefinition != null) && (objectDefinition.getEntity() != null)) {
                objectDefinition
                    .getEntity()
                    .forEach(
                        entity -> {
                          if ((entity.getActorEntity() != null)
                              && (entity.getActorEntity().getPlatform() != null)
                              && (entity.getActorEntity().getPlatform().getSurfaceVessel()
                                  != null)) {
                            SurfaceVesselType surfaceVessel =
                                entity.getActorEntity().getPlatform().getSurfaceVessel();
                            if (filter.test(surfaceVessel)) {
                              consumer.accept(surfaceVessel);
                            }
                          }
                        });
              }
            });
  }

  // VEHICLE

  /**
   * Invokes {@code consumer} for every {@link VehicleType} in the initialization body.
   *
   * @param msg the initialization body
   * @param consumer the callback invoked for each vehicle
   */
  public static void getVehicles(
      final C2SIMInitializationBodyType msg, final Consumer<VehicleType> consumer) {
    getVehicles(msg, subsurfaceVessel -> true /* no filtering */, consumer);
  }

  /**
   * Invokes {@code consumer} for every {@link VehicleType} in the initialization body that
   * satisfies {@code filter}.
   *
   * @param msg the initialization body
   * @param filter predicate to select which vehicles are passed to the consumer
   * @param consumer the callback invoked for each matching vehicle
   */
  public static void getVehicles(
      final C2SIMInitializationBodyType msg,
      Predicate<VehicleType> filter,
      final Consumer<VehicleType> consumer) {
    // ObjectDefinitions -> Entity -> ActorEntity -> Platform -> Vehicle
    msg.getObjectDefinitions()
        .forEach(
            objectDefinition -> {
              if ((objectDefinition != null) && (objectDefinition.getEntity() != null)) {
                objectDefinition
                    .getEntity()
                    .forEach(
                        entity -> {
                          if ((entity.getActorEntity() != null)
                              && (entity.getActorEntity().getPlatform() != null)
                              && (entity.getActorEntity().getPlatform().getVehicle() != null)) {
                            VehicleType vehicle =
                                entity.getActorEntity().getPlatform().getVehicle();
                            if (filter.test(vehicle)) {
                              consumer.accept(vehicle);
                            }
                          }
                        });
              }
            });
  }

  // CulturalFeature

  /**
   * Invokes {@code consumer} for every {@link CulturalFeatureType} in the initialization body.
   *
   * @param msg the initialization body
   * @param consumer the callback invoked for each cultural feature
   */
  public static void getCultureFeatures(
      final C2SIMInitializationBodyType msg, final Consumer<CulturalFeatureType> consumer) {
    getCultureFeatures(msg, cultureFeature -> true /* no filtering */, consumer);
  }

  /**
   * Invokes {@code consumer} for every {@link CulturalFeatureType} in the initialization body that
   * satisfies {@code filter}.
   *
   * @param msg the initialization body
   * @param filter predicate to select which cultural features are passed to the consumer
   * @param consumer the callback invoked for each matching cultural feature
   */
  public static void getCultureFeatures(
      final C2SIMInitializationBodyType msg,
      Predicate<CulturalFeatureType> filter,
      final Consumer<CulturalFeatureType> consumer) {
    // ObjectDefinitions -> Entity -> ActorEntity -> PhysicalEntity -> CulturalFeature
    msg.getObjectDefinitions()
        .forEach(
            objectDefinition -> {
              if ((objectDefinition != null) && (objectDefinition.getEntity() != null)) {
                objectDefinition
                    .getEntity()
                    .forEach(
                        entity -> {
                          if ((entity.getActorEntity() != null)
                              && (entity.getPhysicalEntity() != null)
                              && (entity.getPhysicalEntity().getCulturalFeature() != null)) {
                            CulturalFeatureType culturalFeature =
                                entity.getPhysicalEntity().getCulturalFeature();
                            if (filter.test(culturalFeature)) {
                              consumer.accept(culturalFeature);
                            }
                          }
                        });
              }
            });
  }

  // Environmental Object

  /**
   * Invokes {@code consumer} for every {@link EnvironmentalObjectType} in the initialization body.
   *
   * @param msg the initialization body
   * @param consumer the callback invoked for each environmental object
   */
  public static void getEnvironmentalObjects(
      final C2SIMInitializationBodyType msg, final Consumer<EnvironmentalObjectType> consumer) {
    getEnvironmentalObjects(msg, cultureFeature -> true /* no filtering */, consumer);
  }

  /**
   * Invokes {@code consumer} for every {@link EnvironmentalObjectType} in the initialization body
   * that satisfies {@code filter}.
   *
   * @param msg the initialization body
   * @param filter predicate to select which environmental objects are passed to the consumer
   * @param consumer the callback invoked for each matching environmental object
   */
  public static void getEnvironmentalObjects(
      final C2SIMInitializationBodyType msg,
      Predicate<EnvironmentalObjectType> filter,
      final Consumer<EnvironmentalObjectType> consumer) {
    // ObjectDefinitions -> Entity -> ActorEntity -> PhysicalEntity -> EnvironmentalObject
    msg.getObjectDefinitions()
        .forEach(
            objectDefinition -> {
              if ((objectDefinition != null) && (objectDefinition.getEntity() != null)) {
                objectDefinition
                    .getEntity()
                    .forEach(
                        entity -> {
                          if ((entity.getActorEntity() != null)
                              && (entity.getPhysicalEntity() != null)
                              && (entity.getPhysicalEntity().getEnvironmentalObject() != null)) {
                            EnvironmentalObjectType environmentalObject =
                                entity.getPhysicalEntity().getEnvironmentalObject();
                            if (filter.test(environmentalObject)) {
                              consumer.accept(environmentalObject);
                            }
                          }
                        });
              }
            });
  }

  // GeographicFeature

  /**
   * Invokes {@code consumer} for every {@link GeographicFeatureType} in the initialization body.
   *
   * @param msg the initialization body
   * @param consumer the callback invoked for each geographic feature
   */
  public static void getGeographicFeatures(
      final C2SIMInitializationBodyType msg, final Consumer<GeographicFeatureType> consumer) {
    getGeographicFeatures(msg, cultureFeature -> true /* no filtering */, consumer);
  }

  /**
   * Invokes {@code consumer} for every {@link GeographicFeatureType} in the initialization body
   * that satisfies {@code filter}.
   *
   * @param msg the initialization body
   * @param filter predicate to select which geographic features are passed to the consumer
   * @param consumer the callback invoked for each matching geographic feature
   */
  public static void getGeographicFeatures(
      final C2SIMInitializationBodyType msg,
      Predicate<GeographicFeatureType> filter,
      final Consumer<GeographicFeatureType> consumer) {
    // ObjectDefinitions -> Entity -> ActorEntity -> PhysicalEntity -> GeographicFeature
    msg.getObjectDefinitions()
        .forEach(
            objectDefinition -> {
              if ((objectDefinition != null) && (objectDefinition.getEntity() != null)) {
                objectDefinition
                    .getEntity()
                    .forEach(
                        entity -> {
                          if ((entity.getActorEntity() != null)
                              && (entity.getPhysicalEntity() != null)
                              && (entity.getPhysicalEntity().getGeographicFeature() != null)) {
                            GeographicFeatureType geographicFeature =
                                entity.getPhysicalEntity().getGeographicFeature();
                            if (filter.test(geographicFeature)) {
                              consumer.accept(geographicFeature);
                            }
                          }
                        });
              }
            });
  }

  // MapGraphic

  /**
   * Invokes {@code consumer} for every {@link MapGraphicType} in the initialization body.
   *
   * @param msg the initialization body
   * @param consumer the callback invoked for each map graphic
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static void getMapGraphics(
      final C2SIMInitializationBodyType msg, final Consumer<MapGraphicType> consumer) {
    getMapGraphics(msg, cultureFeature -> true /* no filtering */, consumer);
  }

  /**
   * Invokes {@code consumer} for every {@link MapGraphicType} in the initialization body that
   * satisfies {@code filter}.
   *
   * @param msg the initialization body
   * @param filter predicate to select which map graphics are passed to the consumer
   * @param consumer the callback invoked for each matching map graphic
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static void getMapGraphics(
      final C2SIMInitializationBodyType msg,
      Predicate<MapGraphicType> filter,
      final Consumer<MapGraphicType> consumer) {
    // ObjectDefinitions -> Entity -> ActorEntity -> PhysicalEntity -> MapGraphic
    msg.getObjectDefinitions()
        .forEach(
            objectDefinition -> {
              if ((objectDefinition != null) && (objectDefinition.getEntity() != null)) {
                objectDefinition
                    .getEntity()
                    .forEach(
                        entity -> {
                          if ((entity.getPhysicalEntity() != null)
                              && (entity.getPhysicalEntity().getMapGraphic() != null)) {
                            MapGraphicType geographicFeature =
                                entity.getPhysicalEntity().getMapGraphic();
                            if (filter.test(geographicFeature)) {
                              consumer.accept(geographicFeature);
                            }
                          }
                        });
              }
            });
  }

  /**
   * Invokes {@code consumer} for every {@link TacticalGraphicType} in the initialization body.
   *
   * @param msg the initialization body
   * @param consumer the callback invoked for each tactical graphic
   */
  public static void getTacticalGraphics(
      final C2SIMInitializationBodyType msg, final Consumer<TacticalGraphicType> consumer) {
    getTacticalGraphics(msg, tacticalGraphic -> true /* no filtering */, consumer);
  }

  /**
   * Invokes {@code consumer} for every {@link TacticalGraphicType} in the initialization body that
   * satisfies {@code filter}.
   *
   * @param msg the initialization body
   * @param filter predicate to select which tactical graphics are passed to the consumer
   * @param consumer the callback invoked for each matching tactical graphic
   */
  public static void getTacticalGraphics(
      final C2SIMInitializationBodyType msg,
      Predicate<TacticalGraphicType> filter,
      final Consumer<TacticalGraphicType> consumer) {
    getMapGraphics(
        msg,
        mapGraphic -> true,
        mapGraphic -> {
          if ((mapGraphic.getTacticalGraphic() != null)
              && (filter.test(mapGraphic.getTacticalGraphic()))) {
            consumer.accept(mapGraphic.getTacticalGraphic());
          }
        });
  }

  /**
   * Builds a map from system name to the list of actor objects owned by that system.
   *
   * <p>Each actor reference UUID is resolved to a {@link UnitType}, {@link
   * NonMilitaryOrganizationType}, or {@link VehicleType} if a match is found; otherwise the raw
   * UUID string is added to the list.
   *
   * @param msg the initialization body
   * @return a map from system name to list of resolved actor objects (or UUID strings)
   */
  // Object = UnitType (MilitaryOrganization), NonMilitaryOrganization, String when not found or
  // mapped
  // Returns: SystemName with a list of objects owned by system
  public static Map<String, List<Object>> getActors(final C2SIMInitializationBodyType msg) {

    Map<String, List<Object>> result = new HashMap<>();

    if (msg.getSystemEntityList() == null) {
      return result;
    }

    for (SystemEntityListType system : msg.getSystemEntityList()) {
      String systemName = system.getSystemName();
      List<Object> actors = new ArrayList<>();
      result.put(systemName, actors);

      for (String actorReference : system.getActorReference()) {
        actors.add(resolveActor(msg, actorReference));
      }
    }

    return result;
  }

  private static Object resolveActor(C2SIMInitializationBodyType msg, String actorReference) {

    return findUnitByUuid(msg, actorReference)
        .<Object>map(u -> u)
        .or(() -> findNonMilitaryOrganizationByUuid(msg, actorReference).map(o -> o))
        .or(() -> findVehicleByUuid(msg, actorReference).map(v -> v))
        .orElse(actorReference);
  }

  /**
   * Finds the first {@link UnitType} whose UUID matches {@code uuid} (case-insensitive).
   *
   * @param msg the initialization body
   * @param uuid the UUID to search for; returns empty immediately when {@code null}
   * @return an {@link Optional} containing the matching unit, or empty if not found
   */
  public static Optional<UnitType> findUnitByUuid(
      final C2SIMInitializationBodyType msg, final String uuid) {
    if (uuid == null) {
      return Optional.empty();
    }
    AtomicReference<UnitType> result = new AtomicReference<>(null);
    getUnits(
        msg,
        unit -> {
          if (uuid.equalsIgnoreCase(unit.getUUID())) {
            result.getAndSet(unit);
          }
        });
    return Optional.ofNullable(result.get());
  }

  /**
   * Returns the UUID of the given actor object as a {@link UUID}, or {@code null} when the object
   * type is not recognized.
   *
   * <p>Currently only {@link UnitType} is handled.
   *
   * @param item the actor object
   * @return the UUID, or {@code null}
   */
  public static UUID getUUID(final Object item) {
    if (item instanceof UnitType unitType) {
      return UUID.fromString(unitType.getUUID());
    }
    return null;
  }

  private static <T> void findAndConsume(
      C2SIMInitializationBodyType msg,
      String uuid,
      BiConsumer<C2SIMInitializationBodyType, Consumer<T>> getter,
      Function<T, String> uuidExtractor,
      Consumer<T> consumer) {

    if (consumer == null) {
      return;
    }

    getter.accept(
        msg,
        item -> {
          if (uuid.equalsIgnoreCase(uuidExtractor.apply(item))) {
            consumer.accept(item);
          }
        });
  }

  /**
   * Searches all entity kinds for the entity with the given UUID and invokes the matching consumer.
   *
   * <p>Multiple consumers may be invoked if entities of different kinds share the same UUID (which
   * should not happen in a well-formed document). Passing {@code null} for {@code uuid} is a no-op.
   *
   * @param msg the initialization body
   * @param uuid the UUID to search for
   * @param consumerUnit invoked when a matching {@link UnitType} is found
   * @param consumerNonMilitary invoked when a matching {@link NonMilitaryOrganizationType} is found
   * @param consumerAircraft invoked when a matching {@link AircraftType} is found
   * @param consumerSubsurfaceVessels invoked when a matching {@link SubsurfaceVesselType} is found
   * @param consumerSurfaceVessels invoked when a matching {@link SurfaceVesselType} is found
   * @param consumerVehicle invoked when a matching {@link VehicleType} is found
   * @param consumerCultureFeature invoked when a matching {@link CulturalFeatureType} is found
   * @param consumerEnvironmentalObject invoked when a matching {@link EnvironmentalObjectType} is
   *     found
   * @param consumerGeographicFeature invoked when a matching {@link GeographicFeatureType} is found
   */
  public static void findUuid(
      final C2SIMInitializationBodyType msg,
      final String uuid,
      Consumer<UnitType> consumerUnit,
      Consumer<NonMilitaryOrganizationType> consumerNonMilitary,
      Consumer<PersonType> consumerPerson,
      Consumer<AircraftType> consumerAircraft,
      Consumer<SubsurfaceVesselType> consumerSubsurfaceVessels,
      Consumer<SurfaceVesselType> consumerSurfaceVessels,
      Consumer<VehicleType> consumerVehicle,
      Consumer<CulturalFeatureType> consumerCultureFeature,
      Consumer<EnvironmentalObjectType> consumerEnvironmentalObject,
      Consumer<GeographicFeatureType> consumerGeographicFeature) {

    if (uuid == null) {
      return;
    }

    findAndConsume(
        msg, uuid, C2SIMInitializationBodyTypeHelper::getUnits, UnitType::getUUID, consumerUnit);

    findAndConsume(
        msg,
        uuid,
        C2SIMInitializationBodyTypeHelper::getNonMilitaryOrganizations,
        NonMilitaryOrganizationType::getUUID,
        consumerNonMilitary);

    findAndConsume(
        msg,
        uuid,
        C2SIMInitializationBodyTypeHelper::getPersons,
        PersonType::getUUID,
        consumerPerson);

    findAndConsume(
        msg,
        uuid,
        C2SIMInitializationBodyTypeHelper::getAircrafts,
        AircraftType::getUUID,
        consumerAircraft);

    findAndConsume(
        msg,
        uuid,
        C2SIMInitializationBodyTypeHelper::getSubSurfaceVessels,
        SubsurfaceVesselType::getUUID,
        consumerSubsurfaceVessels);

    findAndConsume(
        msg,
        uuid,
        C2SIMInitializationBodyTypeHelper::getSurfaceVessels,
        SurfaceVesselType::getUUID,
        consumerSurfaceVessels);

    findAndConsume(
        msg,
        uuid,
        C2SIMInitializationBodyTypeHelper::getVehicles,
        VehicleType::getUUID,
        consumerVehicle);

    findAndConsume(
        msg,
        uuid,
        C2SIMInitializationBodyTypeHelper::getCultureFeatures,
        CulturalFeatureType::getUUID,
        consumerCultureFeature);

    findAndConsume(
        msg,
        uuid,
        C2SIMInitializationBodyTypeHelper::getEnvironmentalObjects,
        EnvironmentalObjectType::getUUID,
        consumerEnvironmentalObject);

    findAndConsume(
        msg,
        uuid,
        C2SIMInitializationBodyTypeHelper::getGeographicFeatures,
        GeographicFeatureType::getUUID,
        consumerGeographicFeature);
  }

  /**
   * Returns the set of federate (system) names declared in the initialization body's {@code
   * SystemEntityList}.
   *
   * @param body the initialization body
   * @param defaultValue the value to return when {@code systemEntityList} is {@code null}
   * @return the set of required federate names, or {@code defaultValue}
   */
  @SuppressWarnings("checkstyle:LineLength")
  public static Set<String> getRequiredFederates(
      C2SIMInitializationBodyType body, Set<String> defaultValue) {
    if (body.getSystemEntityList() != null) {
      Set<String> required = new HashSet<>();
      for (SystemEntityListType systemEntity : body.getSystemEntityList()) {
        required.add(systemEntity.getSystemName());
      }
      return required;
    }
    return defaultValue;
  }

  /**
   * Returns the scenario start date-time from the initialization body.
   *
   * <p>Returns:
   *
   * <ul>
   *   <li>{@code defaultValue} when no scenario date-time is set.
   *   <li>The current time ({@link Instant#now()}) when the date-time is the sentinel value {@code
   *       0000-00-00T00:00:00Z} (workaround for a known data issue).
   *   <li>The parsed {@link Instant} otherwise.
   * </ul>
   *
   * @param body the initialization body
   * @param defaultValue the value to return when no scenario date-time is present
   * @return the scenario start date-time as an {@link Instant}
   * @throws java.time.DateTimeException if the stored value cannot be parsed
   */
  /*
      returns
      - defaultValue if scenario time is not set
      - Current time if scenario time is 0000-00-00T00:00:00Z (bug fix)
      - throw exception when text cannot be converted to datetime object
  */
  public static Instant getScenarioDateTime(
      C2SIMInitializationBodyType body, Instant defaultValue) {
    if (body.getScenarioSetting() != null && body.getScenarioSetting().getDateTime() != null) {
      String value = body.getScenarioSetting().getDateTime().getIsoDateTime();
      if (DateTimeTypeHelper.isEmptyDateTime(value)) {
        // A zero value may be present in the message, but is in fact an invalid value.
        // Work around this by using NOW.
        return Instant.now();
      } else {
        return DateTimeTypeHelper.toInstant(value);
      }
    } else {
      return defaultValue;
    }
  }

  private static void setLocation(EntityStateType currentState, GeodeticCoordinateType location) {
    if (location == null) {
      return;
    }
    // Clone object
    var loc = new GeodeticCoordinateType();
    loc.setLatitude(location.getLatitude());
    loc.setLongitude(location.getLongitude());
    loc.setAltitudeAGL(location.getAltitudeAGL());
    loc.setAltitudeMSL(location.getAltitudeMSL());
    LocationHelper.setLocation(currentState, loc);
  }

  @SuppressWarnings("checkstyle:LineLength")
  private static Optional<GeodeticCoordinateType> getLocationFromSuperior(
      C2SIMInitializationBodyType body, String superiorUuid) {
    // Lambda function requires atomic
    AtomicReference<Optional<GeodeticCoordinateType>> result =
        new AtomicReference<>(Optional.empty());
    AtomicReference<String> superiorUuidOfSuperior = new AtomicReference<>(null);
    findUuid(
        body,
        superiorUuid,
        unitType -> {
          result.set(LocationHelper.getLocation(unitType));
          if (unitType.getEntityDescriptor() != null) {
            superiorUuidOfSuperior.set(unitType.getEntityDescriptor().getSuperior());
          }
        },
        nonMilitaryOrganizationType -> {},
        personType -> {},
        aircraftType -> {},
        subsurfaceVesselType -> {},
        vehicleType -> {},
        culturalFeatureType -> {
          // No actor
        },
        environmentalObjectType -> {
          // No actor
        },
        geographicFeatureType -> {
          // No actor
        },
        geographicFeatureType -> {
          // No actor
        });
    if (result.get().isEmpty()) {
      // Recursive search
      if (superiorUuidOfSuperior.get() == null) {
        return Optional.empty();
      }
      return getLocationFromSuperior(body, superiorUuidOfSuperior.get());
    } else {
      return result.get();
    }
  }

  /**
   * Fills in missing entity locations by inheriting the position from the entity's superior in the
   * command hierarchy.
   *
   * <p>Currently only {@link UnitType} entities are updated. The search walks up the superior chain
   * recursively until a location is found or the chain ends.
   *
   * @param body the initialization body to update in place
   */
  /*
  If the position is not set it should inherit the position from its superior.
   */
  public static void fixEmptyLocations(C2SIMInitializationBodyType body) {
    // TODO: While searching could fix mission location in back trail (is faster)
    // TODO: Only units are updated now (do all actors like nonMilitaryOrganizationType, personType,
    // etc
    // Fix unit positions
    try {
      getUnits(
          body,
          unit -> {
            if ((LocationHelper.getLocation(unit).isEmpty()
                && (unit.getEntityDescriptor() != null))) {
              var superiorUuid = unit.getEntityDescriptor().getSuperior();
              var locationFromSuperior = getLocationFromSuperior(body, superiorUuid);
              if (locationFromSuperior.isPresent()) {
                if (unit.getCurrentState() == null) {
                  unit.setCurrentState(new EntityStateType());
                }
                setLocation(unit.getCurrentState(), locationFromSuperior.get());
              }
            }
          });
    } catch (Exception ex) {
      logger.error(
          "Error C2SIMInitializationBodyTypeFunctions::fixEmptyLocations: {}", ex.getMessage());
    }
  }
}
