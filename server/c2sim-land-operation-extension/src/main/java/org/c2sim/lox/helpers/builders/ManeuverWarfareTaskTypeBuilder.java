package org.c2sim.lox.helpers.builders;

import java.util.UUID;
import org.c2sim.lox.schema.*;

/**
 * Builder for {@link ManeuverWarfareTaskType} objects.
 *
 * <p>Both {@code uuid} and {@code performingEntity} must be set before calling {@link #build()}.
 */
public class ManeuverWarfareTaskTypeBuilder {
  private final ManeuverWarfareTaskType task = new ManeuverWarfareTaskType();

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static ManeuverWarfareTaskTypeBuilder create() {
    return new ManeuverWarfareTaskTypeBuilder();
  }

  /**
   * Adds a geodetic location to the task's location list.
   *
   * @param location a builder whose {@link GeodeticCoordinateTypeBuilder#build()} result is used
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder addLocation(GeodeticCoordinateTypeBuilder location) {
    return addLocation((location.build()));
  }

  /**
   * Adds a geodetic location to the task's location list.
   *
   * @param location the geodetic coordinate to add
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder addLocation(GeodeticCoordinateType location) {
    var loc = new LocationType();
    loc.setGeodeticCoordinate(location);
    task.getLocation().add(loc);
    return this;
  }

  /**
   * Adds a relative location to the task's location list.
   *
   * @param location a builder whose {@link RelativeLocationTypeBuilder#build()} result is used
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder addLocation(RelativeLocationTypeBuilder location) {
    return addLocation((location.build()));
  }

  /**
   * Adds a relative location to the task's location list.
   *
   * @param location the relative location to add
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder addLocation(RelativeLocationType location) {
    var loc = new LocationType();
    loc.setRelativeLocation(location);
    task.getLocation().add(loc);
    return this;
  }

  /**
   * Adds a map-graphic ID reference (only if not already present).
   *
   * @param id the map-graphic UUID to reference
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder addMapGraphicID(UUID id) {
    var idText = id.toString();
    if (!task.getMapGraphicID().contains(idText)) {
      task.getMapGraphicID().add(idText);
    }
    return this;
  }

  /**
   * Sets the task name.
   *
   * @param name a human-readable label for the task
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder name(String name) {
    task.setName(name);
    return this;
  }

  /**
   * Sets the task UUID.
   *
   * @param id the UUID to assign to this task
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder uuid(UUID id) {
    task.setUUID(id.toString());
    return this;
  }

  /**
   * Adds an affected entity reference (only if not already present).
   *
   * @param id the UUID of the affected entity
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder addEffectedEntity(UUID id) {
    var idText = id.toString();
    if (!task.getAffectedEntity().contains(idText)) {
      task.getAffectedEntity().add(idText);
    }
    return this;
  }

  /**
   * Adds a desired effect code (only if not already present).
   *
   * @param code the desired effect code to add
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder addDesiredEffectCode(DesiredEffectCodeType code) {
    if (!task.getDesiredEffectCode().contains(code)) {
      task.getDesiredEffectCode().add(code);
    }
    return this;
  }

  /**
   * Sets the task duration from a {@link DurationTypeBuilder}.
   *
   * @param duration a builder whose {@link DurationTypeBuilder#build()} result is used
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder duration(DurationTypeBuilder duration) {
    return duration(duration.build());
  }

  /**
   * Sets the task end time.
   *
   * @param endTime the end time
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder endTime(TimeInstantType endTime) {
    task.setEndTime(endTime);
    return this;
  }

  /**
   * Sets the task start time.
   *
   * @param startTime the start time
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder startTime(TimeInstantType startTime) {
    task.setStartTime(startTime);
    return this;
  }

  /**
   * Sets the performing entity.
   *
   * @param id the UUID of the entity that will perform the task
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder performingEntity(UUID id) {
    var idText = id.toString();
    task.setPerformingEntity(idText);
    return this;
  }

  /**
   * Sets the task action code.
   *
   * @param code the action to perform (e.g. {@code MOVE_TO_LOCATION})
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder taskActionCode(TaskActionCodeType code) {
    task.setTaskActionCode(code);
    return this;
  }

  /**
   * Adds a rule of engagement to the task.
   *
   * @param rule the rule to add
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder addRulesOfEngagement(RuleOfEngagementType rule) {
    task.getRuleOfEngagement().add(rule);
    return this;
  }

  /**
   * Adds a task functional relation to the task.
   *
   * @param x the functional relation to add
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder addTaskFunctionalRelation(TaskFunctionalRelationType x) {
    task.getTaskFunctionalRelation().add(x);
    return this;
  }

  /**
   * Sets the task duration directly.
   *
   * @param duration the duration value
   * @return this builder
   */
  public ManeuverWarfareTaskTypeBuilder duration(DurationType duration) {
    task.setDuration(duration);
    return this;
  }

  /**
   * Builds and returns the {@link ManeuverWarfareTaskType}.
   *
   * @return the constructed task
   * @throws IllegalArgumentException if {@code uuid} or {@code performingEntity} is not set
   */
  public ManeuverWarfareTaskType build() {
    if (task.getUUID() == null) {
      throw new IllegalArgumentException("UUID is null");
    }
    if (task.getPerformingEntity() == null) {
      throw new IllegalArgumentException("PerformingEntity is null");
    }
    return task;
  }
}
