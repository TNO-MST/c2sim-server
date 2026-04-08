package org.c2sim.server.utils;

import org.c2sim.server.api.models.StateType;
import org.c2sim.statemachine.State;

/**
 * Bidirectional converter between the state-machine {@link State} enum and the REST API {@link
 * StateType} enum.
 *
 * <p>Keeps the state-machine library free of dependencies on the generated REST model types.
 *
 * <p>This is a utility class; instantiation is not allowed.
 */
public class StateHelper {
  // In state machine library we don't want dependency to REST types

  private StateHelper() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Converts a state-machine {@link State} to the corresponding REST API {@link StateType}.
   *
   * @param state the state-machine state
   * @return the matching REST model state type
   * @throws IllegalArgumentException if no mapping is defined for the given state
   */
  public static StateType convert(State state) {
    return switch (state) {
      case INITIALIZED -> StateType.INITIALIZED;
      case UNINITIALIZED -> StateType.UNINITIALIZED;
      case PAUSED -> StateType.PAUSED;
      case RUNNING -> StateType.RUNNING;
      case INITIALIZING -> StateType.INITIALIZING;
      default -> throw new IllegalArgumentException("Not implemented, add mapping!");
    };
  }

  /**
   * Converts a REST API {@link StateType} to the corresponding state-machine {@link State}.
   *
   * @param stateType the REST model state type
   * @return the matching state-machine state
   * @throws IllegalArgumentException if no mapping is defined for the given state type
   */
  public static State convert(StateType stateType) {
    return switch (stateType) {
      case INITIALIZED -> State.INITIALIZED;
      case UNINITIALIZED -> State.UNINITIALIZED;
      case PAUSED -> State.PAUSED;
      case RUNNING -> State.RUNNING;
      case INITIALIZING -> State.INITIALIZING;
      default -> throw new IllegalArgumentException("Not implemented, add mapping!");
    };
  }
}
