package org.c2sim.lox.helpers.builders;

import java.util.List;
import java.util.UUID;
import org.c2sim.lox.schema.ResourceObservationType;
import org.c2sim.lox.schema.ResourceType;

/**
 * Builder for {@link ResourceObservationType} objects.
 *
 * <p>At least one resource entry must be added before calling {@link #build()}.
 */
public class ResourceObservationTypeBuilder {
  private final ResourceObservationType observation = new ResourceObservationType();

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static ResourceObservationTypeBuilder create() {
    return new ResourceObservationTypeBuilder();
  }

  /**
   * Sets the UUID of the observed actor.
   *
   * @param actorReference the actor UUID
   * @return this builder
   */
  public ResourceObservationTypeBuilder actorReference(UUID actorReference) {
    observation.setActorReference(actorReference.toString());
    return this;
  }

  /**
   * Sets the confidence level of the observation.
   *
   * @param confidenceLevel the confidence level (0.0–1.0), or {@code null}
   * @return this builder
   */
  public ResourceObservationTypeBuilder confidenceLevel(Double confidenceLevel) {
    observation.setConfidenceLevel(confidenceLevel);
    return this;
  }

  /**
   * Sets the uncertainty interval of the observation.
   *
   * @param uncertaintyInterval the uncertainty interval, or {@code null}
   * @return this builder
   */
  public ResourceObservationTypeBuilder uncertaintyInterval(Double uncertaintyInterval) {
    observation.setUncertaintyInterval(uncertaintyInterval);
    return this;
  }

  /**
   * Adds a single resource entry directly.
   *
   * @param resource the resource to add
   * @return this builder
   */
  public ResourceObservationTypeBuilder addResource(ResourceType resource) {
    observation.getResource().add(resource);
    return this;
  }

  /**
   * Adds all resource entries from the given list.
   *
   * @param resources the list of resources to add
   * @return this builder
   */
  public ResourceObservationTypeBuilder addAllResources(List<ResourceType> resources) {
    observation.getResource().addAll(resources);
    return this;
  }

  /**
   * Adds a resource entry from a {@link ResourceTypeBuilder}.
   *
   * @param resource a builder whose {@link ResourceTypeBuilder#build()} result is used
   * @return this builder
   */
  public ResourceObservationTypeBuilder addResource(ResourceTypeBuilder resource) {
    observation.getResource().add(resource.build());
    return this;
  }

  /**
   * Builds and returns the {@link ResourceObservationType}.
   *
   * @return the constructed resource observation
   * @throws IllegalStateException if no resource has been added
   */
  public ResourceObservationType build() {
    if (observation.getResource().isEmpty()) {
      throw new IllegalStateException("At least one ResourceType is required");
    }
    return observation;
  }
}
