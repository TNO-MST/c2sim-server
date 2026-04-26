package org.c2sim.lox.helpers.builders;

import org.c2sim.lox.schema.ObservationType;

/**
 * Factory methods for creating {@link ObservationType} choice wrappers.
 *
 * <p>{@link ObservationType} is a XSD choice type. Each static method sets exactly one of the
 * available choices (location, subject-type/APP-6C symbol, health, or resource observation).
 */
public class ObservationTypeBuilder {

  private ObservationTypeBuilder() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Creates an {@link ObservationType} holding a location observation.
   *
   * @param observation a builder whose {@link LocationObservationTypeBuilder#build()} result is
   *     used
   * @return an {@link ObservationType} with the location observation set
   */
  public static ObservationType createLocationObservation(
      LocationObservationTypeBuilder observation) {
    var ob = new ObservationType();
    ob.setLocationObservation(observation.build());
    return ob;
  }

  /**
   * Creates an {@link ObservationType} holding a subject-type (APP-6C symbol) observation.
   *
   * @param subjectTypeObservation a builder whose {@link SubjectTypeObservationTypeBuilder#build()}
   *     result is used
   * @return an {@link ObservationType} with the subject-type observation set
   */
  public static ObservationType createApp6SymbolObservation(
      SubjectTypeObservationTypeBuilder subjectTypeObservation) {
    var ob = new ObservationType();
    ob.setSubjectTypeObservation(subjectTypeObservation.build());
    return ob;
  }

  /**
   * Creates an {@link ObservationType} holding a health observation.
   *
   * @param healthObservation a builder whose {@link HealthObservationTypeBuilder#build()} result is
   *     used
   * @return an {@link ObservationType} with the health observation set
   */
  public static ObservationType createHealthObservation(
      HealthObservationTypeBuilder healthObservation) {
    var ob = new ObservationType();
    ob.setHealthObservation(healthObservation.build());
    return ob;
  }

  /**
   * Creates an {@link ObservationType} holding a subject object observation.
   *
   * @param subjectObjectObservation a builder whose {@link SubjectTypeObservationTypeBuilder#build()} result is
   *     used
   * @return an {@link ObservationType} with the subject object observation set
   */
  public static ObservationType createSubjectObjectObservation(
          SubjectTypeObservationTypeBuilder subjectObjectObservation) {
    var ob = new ObservationType();
    ob.setSubjectTypeObservation(subjectObjectObservation.build());
    return ob;
  }

  /**
   * Creates an {@link ObservationType} holding a name observation.
   *
   * @param nameObservation a builder whose {@link NameObservationTypeBuilder#build()} result is
   *     used
   * @return an {@link ObservationType} with the name observation set
   */
  public static ObservationType createNameObservation(
          NameObservationTypeBuilder nameObservation) {
    var ob = new ObservationType();
    ob.setNameObservation(nameObservation.build());
    return ob;
  }

  /**
   * Creates an {@link ObservationType} holding a name observation.
   *
   * @param activityObservation a builder whose {@link ActivityObservationTypeBuilder#build()} result is
   *     used
   * @return an {@link ObservationType} with the name observation set
   */
  public static ObservationType createActivityObservation(
          ActivityObservationTypeBuilder activityObservation) {
    var ob = new ObservationType();
    ob.setActivityObservation(activityObservation.build());
    return ob;
  }



  /**
   * Creates an {@link ObservationType} holding a resource observation.
   *
   * @param healthObservation a builder whose {@link ResourceObservationTypeBuilder#build()} result
   *     is used
   * @return an {@link ObservationType} with the resource observation set
   */
  // In createHealthObservation also resources can be set?!
  public static ObservationType createResourceObservation(
      ResourceObservationTypeBuilder healthObservation) {
    var ob = new ObservationType();
    ob.setResourceObservation(healthObservation.build());
    return ob;
  }
}
