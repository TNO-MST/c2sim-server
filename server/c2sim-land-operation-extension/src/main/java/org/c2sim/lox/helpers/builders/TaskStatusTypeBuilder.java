package org.c2sim.lox.helpers.builders;

import java.time.Instant;
import java.util.UUID;
import org.c2sim.lox.schema.DurationType;
import org.c2sim.lox.schema.TaskStatusCodeType;
import org.c2sim.lox.schema.TaskStatusType;
import org.c2sim.lox.schema.TimeInstantType;

/**
 * Builder for {@link TaskStatusType} objects.
 *
 * <p>The fields {@code currentTask}, {@code timeOfObservation}, and {@code taskStatusCode} are all
 * required before calling {@link #build()}.
 */
public class TaskStatusTypeBuilder {

  private final TaskStatusType taskStatus;

  private TaskStatusTypeBuilder() {
    this.taskStatus = new TaskStatusType();
  }

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static TaskStatusTypeBuilder create() {
    return new TaskStatusTypeBuilder();
  }

  /**
   * Sets the task duration.
   *
   * @param duration the duration of the task
   * @return this builder
   */
  public TaskStatusTypeBuilder duration(DurationType duration) {
    taskStatus.setDuration(duration);
    return this;
  }

  /**
   * Sets the time of observation from an {@link Instant}.
   *
   * @param time the point in time when the task status was observed
   * @return this builder
   */
  public TaskStatusTypeBuilder timeOfObservation(Instant time) {
    timeOfObservation(TimeInstantTypeBuilder.absoluteTime(time));
    return this;
  }

  /**
   * Sets the time of observation directly.
   *
   * @param time the time instant value
   * @return this builder
   */
  public TaskStatusTypeBuilder timeOfObservation(TimeInstantType time) {
    taskStatus.setTimeOfObservation(time);
    return this;
  }

  /**
   * Sets the UUID of the task being reported on.
   *
   * @param task the task UUID
   * @return this builder
   */
  public TaskStatusTypeBuilder currentTask(UUID task) {
    taskStatus.setCurrentTask(task.toString());
    return this;
  }

  /**
   * Sets the task status code.
   *
   * @param code the current status of the task
   * @return this builder
   */
  public TaskStatusTypeBuilder taskStatusCode(TaskStatusCodeType code) {
    taskStatus.setTaskStatusCode(code);
    return this;
  }

  /**
   * Builds and returns the {@link TaskStatusType}.
   *
   * @return the constructed task status object
   * @throws IllegalArgumentException if {@code currentTask}, {@code timeOfObservation}, or {@code
   *     taskStatusCode} is not set
   */
  public TaskStatusType build() {
    if (taskStatus.getCurrentTask() == null) {
      throw new IllegalArgumentException("CurrentTask is null");
    }
    if (taskStatus.getTimeOfObservation() == null) {
      throw new IllegalArgumentException("TimeOfObservation is null");
    }
    if (taskStatus.getTaskStatusCode() == null) {
      throw new IllegalArgumentException("TaskStatusCode is null");
    }
    return taskStatus;
  }
}
