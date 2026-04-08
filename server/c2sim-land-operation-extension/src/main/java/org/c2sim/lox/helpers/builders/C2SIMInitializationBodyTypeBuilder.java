package org.c2sim.lox.helpers.builders;

import com.sun.istack.NotNull;
import java.time.Instant;
import java.util.UUID;
import org.c2sim.lox.schema.*;

/**
 * Builder for {@link C2SIMInitializationBodyType} objects.
 *
 * <p>Primarily used for testing. The builder creates an XSD-valid initialization body with a single
 * vehicle entity and a system entity list entry referencing that vehicle.
 */
// For now only used for testing
public class C2SIMInitializationBodyTypeBuilder {

  // Prevent instantiation
  private C2SIMInitializationBodyTypeBuilder() {
    throw new AssertionError("Only static functions");
  }

  private final C2SIMInitializationBodyType init = new C2SIMInitializationBodyType();

  private final String vehicleId = UUID.randomUUID().toString();

  /**
   * Creates a new builder for the given system name.
   *
   * @param systemName the name of the system to include in the entity list
   * @return a new builder instance
   */
  public static C2SIMInitializationBodyTypeBuilder create(String systemName) {
    return new C2SIMInitializationBodyTypeBuilder(systemName);
  }

  private C2SIMInitializationBodyTypeBuilder(String systemName) {
    // Just create empty object that is XSD valid
    var scenario = new ScenarioSettingType();
    scenario.setDateTime(DateTimeTypeBuilder.create(Instant.now()).build());
    scenario.setVersion("1.0");
    init.setScenarioSetting(scenario);
    var obj = new ObjectDefinitionsType();

    init.getObjectDefinitions().add(obj);
    obj.getEntity().add(createVehicle());
    init.getSystemEntityList().add(createSystemList(systemName));
  }

  private SystemEntityListType createSystemList(String systemName) {
    SystemEntityListType lst = new SystemEntityListType();
    lst.setSystemName(systemName);
    lst.getActorReference().add(vehicleId);
    return lst;
  }

  private EntityType createVehicle() {
    var entity = new EntityType();
    var actor = new ActorEntityType();
    var platform = new PlatformType();
    var vehicle = new VehicleType();
    vehicle.setUUID(vehicleId);
    vehicle.setEntityDescriptor(new EntityDescriptorType());
    entity.setActorEntity(actor);
    actor.setPlatform(platform);
    platform.setVehicle(vehicle);
    return entity;
  }

  /**
   * Sets the scenario date-time.
   *
   * @param dt a {@link DateTimeTypeBuilder} for the scenario start time
   * @return this builder
   */
  public C2SIMInitializationBodyTypeBuilder scenarioTime(@NotNull DateTimeTypeBuilder dt) {
    init.getScenarioSetting().setDateTime(dt.build());
    return this;
  }

  /**
   * Sets the scenario version string.
   *
   * @param version the version string (e.g. {@code "1.0"})
   * @return this builder
   */
  public C2SIMInitializationBodyTypeBuilder scenarioVersion(@NotNull String version) {
    init.getScenarioSetting().setVersion(version);
    return this;
  }

  /**
   * Builds and returns the {@link C2SIMInitializationBodyType}.
   *
   * @return the constructed initialization body
   */
  public C2SIMInitializationBodyType build() {
    return init;
  }
}
