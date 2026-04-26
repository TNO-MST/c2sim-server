package org.c2sim.lox.helpers.builders;

import org.c2sim.lox.schema.*;

import java.time.Instant;
import java.util.List;

/**
 * Builder for {@link ObservationReportContentType} objects.
 *
 * <p>One or more {@link ObservationType} entries can be added via {@link #addObservation} or {@link
 * #addAllObservations}.
 */
public class ObservationReportContentTypeBuilder {
    private final ObservationReportContentType reportContent;

    private ObservationReportContentTypeBuilder() {
        this.reportContent = new ObservationReportContentType();
    }

    /**
     * Creates a new, empty builder.
     *
     * @return a new builder instance
     */
    public static ObservationReportContentTypeBuilder create() {
        return new ObservationReportContentTypeBuilder();
    }

    /**
     * Sets the observation duration.
     *
     * @param duration the duration of the observation period
     * @return this builder
     */
    public ObservationReportContentTypeBuilder duration(DurationType duration) {
        reportContent.setDuration(duration);
        return this;
    }

    /**
     * Sets the time of observation from an {@link Instant}.
     *
     * @param timeInstant the point in time when the observation was made
     * @return this builder
     */
    public ObservationReportContentTypeBuilder timeOfObservation(Instant timeInstant) {
        reportContent.setTimeOfObservation(TimeInstantTypeBuilder.absoluteTime(timeInstant));
        return this;
    }

    /**
     * Sets the time of observation directly.
     *
     * @param timeInstant the time instant value
     * @return this builder
     */
    public ObservationReportContentTypeBuilder timeOfObservation(TimeInstantType timeInstant) {
        reportContent.setTimeOfObservation(timeInstant);
        return this;
    }

    /**
     * Adds a single activity observation to the report content.
     *
     * @param activityObservation the observation to add
     * @return this builder
     */
    public ObservationReportContentTypeBuilder
    addActivityObservation(ActivityObservationTypeBuilder activityObservation) {
        if (activityObservation != null) {
            reportContent.getObservation().add(ObservationTypeBuilder.createActivityObservation(activityObservation));
        }
        return this;
    }

    /**
     * Adds a single location observation to the report content.
     *
     * @param locationObservation the observation to add
     * @return this builder
     */
    public ObservationReportContentTypeBuilder
    addLocationObservation(LocationObservationTypeBuilder locationObservation) {
        if (locationObservation != null) {
            reportContent.getObservation().add(ObservationTypeBuilder.createLocationObservation(locationObservation));
        }
        return this;
    }

    /**
     * Adds a single name observation to the report content.
     *
     * @param nameObservation the observation to add
     * @return this builder
     */
    public ObservationReportContentTypeBuilder
    addNameObservation(NameObservationTypeBuilder nameObservation) {
        if (nameObservation != null) {
            reportContent.getObservation().add(
                    ObservationTypeBuilder.createNameObservation(nameObservation));
        }
        return this;
    }

    /**
     * Adds a single health observation to the report content.
     *
     * @param healthObservation the observation to add
     * @return this builder
     */
    public ObservationReportContentTypeBuilder
    addHealthObservation(HealthObservationTypeBuilder healthObservation) {
        if (healthObservation != null) {
            reportContent.getObservation().add(
                    ObservationTypeBuilder.createHealthObservation(healthObservation));
        }
        return this;
    }

    /**
     * Adds a single resource observation to the report content.
     *
     * @param resourceObservation the observation to add
     * @return this builder
     */
    public ObservationReportContentTypeBuilder
    addResourceObservation(ResourceObservationTypeBuilder resourceObservation) {
        if (resourceObservation != null) {
            reportContent.getObservation().add(
                    ObservationTypeBuilder.createResourceObservation(resourceObservation));
        }
        return this;
    }

    /**
     * Adds a single subject object observation to the report content.
     *
     * @param subjectTypeObservation the observation to add
     * @return this builder
     */
    public ObservationReportContentTypeBuilder
    addSubjectObjectObservation(SubjectTypeObservationTypeBuilder subjectTypeObservation) {
        if (subjectTypeObservation != null) {
            reportContent.getObservation().add(
                    ObservationTypeBuilder.createSubjectObjectObservation(subjectTypeObservation));
        }
        return this;
    }



    /**
     * Adds all observations from the given list to the report content.
     *
     * @param observations the list of observations to add
     * @return this builder
     */
    public ObservationReportContentTypeBuilder addAllObservations(
            List<ObservationType> observations) {
        reportContent.getObservation().addAll(observations);
        return this;
    }

    /**
     * Adds a single observation to the report content.
     *
     * @param observation the observation to add
     * @return this builder
     */
    public ObservationReportContentTypeBuilder addObservation(ObservationType observation) {
        reportContent.getObservation().add(observation);
        return this;
    }


    /**
     * Builds and returns the {@link ObservationReportContentType}.
     *
     * @return the constructed observation report content
     */
    public ObservationReportContentType build() {
        return reportContent;
    }
}
