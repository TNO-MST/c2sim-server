package org.c2sim.lox.helpers.builders;

import org.c2sim.lox.schema.ActionCodeType;
import org.c2sim.lox.schema.ActivityObservationType;
import java.util.UUID;

public class ActivityObservationTypeBuilder {
    private final ActivityObservationType activity = new ActivityObservationType();

    public static ActivityObservationTypeBuilder create() {
        return new ActivityObservationTypeBuilder();
    }

    /**
     * Sets the UUID of the observed actor.
     *
     * @param actorReference the actor UUID
     * @return this builder
     */
    public ActivityObservationTypeBuilder actorReference(UUID actorReference) {
        activity.setActorReference(actorReference.toString());
        return this;
    }

    /**
     * Sets the confidence level of the observation.
     *
     * @param confidenceLevel the confidence level (0.0–1.0), or {@code null}
     * @return this builder
     */
    public ActivityObservationTypeBuilder confidenceLevel(Double confidenceLevel) {
        activity.setConfidenceLevel(confidenceLevel);
        return this;
    }

    /**
     * Sets the uncertainty interval of the observation.
     *
     * @param uncertaintyInterval the uncertainty interval, or {@code null}
     * @return this builder
     */
    public ActivityObservationTypeBuilder uncertaintyInterval(Double uncertaintyInterval) {
        activity.setUncertaintyInterval(uncertaintyInterval);
        return this;
    }

    /**
     * Sets the action code of the observation.
     *
     * @param actionCode the action code of observed entity
     * @return this builder
     */
    public ActivityObservationTypeBuilder name(ActionCodeType actionCode) {
        this.activity.setActionCode(actionCode);
        return this;
    }


    /**
     * Builds and returns the {@link ActivityObservationType}.
     *
     * @return the constructed activity observation
     */
    public ActivityObservationType build() {
        return activity;
    }
}
