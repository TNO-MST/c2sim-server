package org.c2sim.lox.helpers.builders;

import org.c2sim.lox.schema.DurationType;
import org.c2sim.lox.schema.SimulationTimeType;

/**
 * Builder for {@link SimulationTimeType} objects.
 *
 * <p>{@code delayTimeAmount} is required before calling {@link #build()}.
 */
public class SimulationTimeTypeBuilder {
  private final SimulationTimeType simulationTime = new SimulationTimeType();

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static SimulationTimeTypeBuilder create() {
    return new SimulationTimeTypeBuilder();
  }

  /**
   * Sets the optional name of the simulation time value.
   *
   * @param name a human-readable label, or {@code null}
   * @return this builder
   */
  public SimulationTimeTypeBuilder name(String name) {
    simulationTime.setName(name);
    return this;
  }

  /**
   * Sets the delay time amount.
   *
   * @param duration the duration that represents the simulation delay
   * @return this builder
   */
  public SimulationTimeTypeBuilder delayTimeAmount(DurationType duration) {
    simulationTime.setDelayTimeAmount(duration);
    return this;
  }

  /**
   * Builds and returns the {@link SimulationTimeType}.
   *
   * @return the constructed simulation time object
   * @throws IllegalStateException if {@code delayTimeAmount} is not set
   */
  public SimulationTimeType build() {
    if (simulationTime.getDelayTimeAmount() == null) {
      throw new IllegalStateException("delayTimeAmount is required");
    }
    return simulationTime;
  }
}
