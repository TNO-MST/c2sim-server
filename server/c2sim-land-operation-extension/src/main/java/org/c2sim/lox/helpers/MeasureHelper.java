package org.c2sim.lox.helpers;

import java.util.function.Supplier;

/** Utility for measuring the wall-clock execution time of a task in milliseconds. */
public final class MeasureHelper {

  /**
   * Holds the result of a timed execution together with the elapsed time.
   *
   * @param <T> the type of the computation result
   * @param result the value returned by the measured task
   * @param timeMs the elapsed wall-clock time in milliseconds
   */
  public record MeasurementResult<T>(T result, double timeMs) {}

  private MeasureHelper() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Executes {@code task} and returns its result together with the elapsed time.
   *
   * @param <T> the return type of the task
   * @param task the computation to measure
   * @return a {@link MeasurementResult} containing the task's return value and the elapsed
   *     wall-clock time in milliseconds
   */
  public static <T> MeasurementResult<T> measureMs(Supplier<T> task) {
    long start = System.nanoTime();
    T result = task.get();
    long end = System.nanoTime();
    double elapsedMs = (end - start) / 1_000_000.0;
    return new MeasurementResult<>(result, elapsedMs);
  }
}
