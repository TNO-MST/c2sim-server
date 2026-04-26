package org.c2sim.lox.helpers.builders;

import org.c2sim.lox.schema.HostilityStatusCodeType;
import org.c2sim.lox.schema.NameObservationType;


import java.util.Objects;
import java.util.UUID;

public class NameObservationTypeBuilder {

    private final NameObservationType name = new NameObservationType();

    public static NameObservationTypeBuilder create() {
        return new NameObservationTypeBuilder();
    }

    /**
     * Sets the UUID of the observed actor.
     *
     * @param actorReference the actor UUID
     * @return this builder
     */
    public NameObservationTypeBuilder actorReference(UUID actorReference) {
        name.setActorReference(actorReference.toString());
        return this;
    }

    /**
     * Sets the confidence level of the observation.
     *
     * @param confidenceLevel the confidence level (0.0–1.0), or {@code null}
     * @return this builder
     */
    public NameObservationTypeBuilder confidenceLevel(Double confidenceLevel) {
        name.setConfidenceLevel(confidenceLevel);
        return this;
    }

    /**
     * Sets the uncertainty interval of the observation.
     *
     * @param uncertaintyInterval the uncertainty interval, or {@code null}
     * @return this builder
     */
    public NameObservationTypeBuilder uncertaintyInterval(Double uncertaintyInterval) {
        name.setUncertaintyInterval(uncertaintyInterval);
        return this;
    }

    /**
     * Sets the name of the observation.
     *
     * @param name the name of observed entity
     * @return this builder
     */
    public NameObservationTypeBuilder name(String name) {
        Objects.requireNonNull(name, "Name cannot be null");
        this.name.setName(name);
        return this;
    }

    /**
     * Sets the marking of the observation.
     *
     * @param marking the marking of observed entity
     * @return this builder
     */
    public NameObservationTypeBuilder marking(String marking) {
        this.name.setMarking(marking);
        return this;
    }

    /**
     * Sets the side of the observation.
     *
     * @param side the force side of observed entity, or {@code null}
     * @return this builder
     */
    public NameObservationTypeBuilder side(UUID side) {
        name.setSide(side != null ? side.toString() : null);
        return this;
    }

    /**
     * Sets the hostility status of the observation.
     *
     * @param hostilityStatus the hostility status of observed entity
     * @return this builder
     */
    public NameObservationTypeBuilder hostilityStatusCode(HostilityStatusCodeType hostilityStatus) {
        name.setHostilityStatusCode(hostilityStatus);
        return this;
    }


    /**
     * Builds and returns the {@link NameObservationType}.
     *
     * @return the constructed name observation
     */
    public NameObservationType build() {
        if (name.getName() == null) {
            throw new IllegalArgumentException("Name is null");
        }
        return name;
    }
}
