package org.c2sim.lox;

import java.lang.reflect.Method;
import java.util.UUID;
import org.c2sim.lox.helpers.C2SIMInitializationBodyTypeHelper;
import org.c2sim.lox.schema.C2SIMInitializationBodyType;
import org.c2sim.lox.schema.SystemEntityListType;

/**
 * Convenience wrapper around {@link C2SIMInitializationBodyType}. Helper functions to get
 * information from hierachical structure.
 */
public class C2simInitialization {

  private final C2SIMInitializationBodyType initialization;

  /**
   * Creates a new wrapper for the given initialisation body.
   *
   * @param init the C2SIM initialisation body
   */
  public C2simInitialization(C2SIMInitializationBodyType init) {
    this.initialization = init;
  }

  /**
   * Returns the name of the system that owns the actor identified by {@code uuid}.
   *
   * @param uuid the actor reference UUID
   * @return the system name, or an empty string if not found
   */
  public String lookupOwnerSystemByUUID(UUID uuid) {
    return lookupOwnerSystemByUUID(uuid.toString());
  }

  /**
   * Returns the name of the system that owns the actor identified by {@code uuid}.
   *
   * @param uuid the actor reference UUID as a string
   * @return the system name, or an empty string if not found or if the initialisation body is null
   */
  public String lookupOwnerSystemByUUID(/*UUID*/ String uuid) {
    if (this.initialization == null) {
      return "";
    }
    for (SystemEntityListType system : this.initialization.getSystemEntityList()) {
      if (system.getActorReference().contains(uuid)) {
        return system.getSystemName();
      }
    }
    return "";
  }

  private static final String ERROR_MSG = "<error>";

  /**
   * Resolves the given UUID to a human-readable entity label (e.g. {@code "Unit Blue Force 1"}).
   *
   * @param uuid the entity UUID
   * @return a descriptive label for the entity, the UUID string if not found, or {@code "<no init
   *     data>"} if the initialisation body is null
   */
  public String lookupEntityByUUID(UUID uuid) {
    return lookupEntityByUUID(uuid.toString());
  }

  /**
   * Resolves the given UUID string to a human-readable entity label (type of object).
   *
   * @param uuid the entity UUID as a string
   * @return a descriptive label for the entity, the UUID string if not found, or {@code "<no init
   *     data>"} if the initialisation body is null
   */
  public String lookupEntityByUUID(String uuid) {
    if (initialization == null) {
      return "<no init data>";
    }

    // Default result
    final String[] result = {uuid};

    C2SIMInitializationBodyTypeHelper.findUuid(
        initialization,
        uuid,
        unit -> result[0] = label("Unit", unit),
        nonMil -> result[0] = label("Non Military", nonMil),
        person -> result[0] = label("Person", person),
        aircraft -> result[0] = label("Aircraft", aircraft),
        sub -> result[0] = label("Subsurface vessel", sub),
        surf -> result[0] = label("Surface vessel", surf),
        veh -> result[0] = label("Vehicle", veh),
        cult -> result[0] = label("CultureFeature", cult),
        env -> result[0] = label("EnvironmentalObject", env),
        geo -> result[0] = "GeographicFeature" // no name?
        );

    return result[0];
  }

  // TODO Reflection is not the best option, but classes don't share getName
  private String label(String type, Object obj) {
    if (obj == null) {
      return ERROR_MSG;
    }

    try {
      Method m = obj.getClass().getMethod("getName");
      Object name = m.invoke(obj);
      return type + " " + name;
    } catch (Exception e) {
      return ERROR_MSG; // getName() does not exist
    }
  }
}
