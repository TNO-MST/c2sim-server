package org.c2sim.lox.helpers.builders;

import java.util.UUID;
import org.c2sim.lox.schema.*;

/**
 * Builder for {@link HealthObservationType} objects.
 *
 * <p>At least one {@link EntityHealthStatusType} entry (strength, operational status, or resource)
 * must be added before calling {@link #build()}.
 */
public class HealthObservationTypeBuilder {

  // Prevent instantiation
  private HealthObservationTypeBuilder() {
    throw new AssertionError("Only static functions");
  }

  private final HealthObservationType healthObservation = new HealthObservationType();

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static HealthObservationTypeBuilder create() {
    return new HealthObservationTypeBuilder();
  }

  /**
   * Sets the actor reference UUID.
   *
   * @param actorReference the UUID of the observed actor
   * @return this builder
   */
  public HealthObservationTypeBuilder actorReference(UUID actorReference) {
    healthObservation.setActorReference(actorReference.toString());
    return this;
  }

  /**
   * Sets the confidence level of the observation.
   *
   * @param confidenceLevel the confidence level (0.0–1.0), or {@code null}
   * @return this builder
   */
  public HealthObservationTypeBuilder confidenceLevel(Double confidenceLevel) {
    healthObservation.setConfidenceLevel(confidenceLevel);
    return this;
  }

  /**
   * Sets the uncertainty interval of the observation.
   *
   * @param uncertaintyInterval the uncertainty interval, or {@code null}
   * @return this builder
   */
  public HealthObservationTypeBuilder uncertaintyInterval(Double uncertaintyInterval) {
    healthObservation.setUncertaintyInterval(uncertaintyInterval);
    return this;
  }

  /**
   * Sets the strength percentage. Updates an existing strength entry if one is already present,
   * otherwise adds a new {@link EntityHealthStatusType} with a {@link StrengthType}.
   *
   * @param percentage the strength as a percentage (0–100). Value is converted to int!
   * @return this builder
   */
  public HealthObservationTypeBuilder strength(Float percentage) {
    if (percentage == null) {
      return this;
    }
    return strength(percentage.intValue());
  }

  /**
   * Sets the strength percentage. Updates an existing strength entry if one is already present,
   * otherwise adds a new {@link EntityHealthStatusType} with a {@link StrengthType}.
   *
   * @param percentage the strength as a percentage (0–100)
   * @return this builder
   */
  public HealthObservationTypeBuilder strength(int percentage) {
    var x =
        healthObservation.getEntityHealthStatus().stream()
            .filter(item -> item.getStrength() != null)
            .findAny();
    if (x.isPresent()) {
      x.get().getStrength().setStrengthPercentage(percentage);
    } else {
      var strength = new StrengthType();
      strength.setStrengthPercentage(percentage);
      var ehs = new EntityHealthStatusType();
      ehs.setStrength(strength);
      healthObservation.getEntityHealthStatus().add(ehs);
    }
    return this;
  }

  /**
   * Sets the operational status. Updates an existing operational-status entry if one is already
   * present, otherwise adds a new {@link EntityHealthStatusType} with an {@link
   * OperationalStatusType}.
   *
   * @param status the operational status code
   * @return this builder
   */
  public HealthObservationTypeBuilder operationalStatus(OperationalStatusCodeType status) {
    var x =
        healthObservation.getEntityHealthStatus().stream()
            .filter(item -> item.getOperationalStatus() != null)
            .findAny();
    if (x.isPresent()) {
      x.get().getOperationalStatus().setOperationalStatusCode(status);
    } else {
      var operationalstatus = new OperationalStatusType();
      operationalstatus.setOperationalStatusCode(status);
      var ehs = new EntityHealthStatusType();
      ehs.setOperationalStatus(operationalstatus);
      healthObservation.getEntityHealthStatus().add(ehs);
    }
    return this;
  }

  /**
   * Adds a resource entry. Appends to an existing resources entry if one is already present,
   * otherwise adds a new {@link EntityHealthStatusType} with a {@link ResourcesType}.
   *
   * @param resource a builder for the resource to add
   * @return this builder
   */
  public HealthObservationTypeBuilder addResource(ResourceTypeBuilder resource) {
    var x =
        healthObservation.getEntityHealthStatus().stream()
            .filter(item -> item.getResources() != null)
            .findAny();
    if (x.isPresent()) {
      x.get().getResources().getResource().add(resource.build());
    } else {
      var rt = new ResourcesType();
      rt.getResource().add(resource.build());
      var ehs = new EntityHealthStatusType();
      ehs.setResources(rt);
      healthObservation.getEntityHealthStatus().add(ehs);
    }

    return this;
  }

  /**
   * Builds and returns the {@link HealthObservationType}.
   *
   * @return the constructed health observation
   * @throws IllegalStateException if no entity health status has been added
   */
  public HealthObservationType build() {
    if (healthObservation.getEntityHealthStatus().isEmpty()) {
      throw new IllegalStateException("At least one EntityHealthStatusType is required");
    }
    return healthObservation;
  }
}
