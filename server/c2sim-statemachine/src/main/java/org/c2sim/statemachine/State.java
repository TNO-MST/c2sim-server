package org.c2sim.statemachine;

/** The set of states in the C2SIM simulation lifecycle state machine. */
public enum State {
  /** No initialization has been submitted yet. Initial state. */
  UNINITIALIZED,

  /** Initialization has been submitted and is in progress. */
  INITIALIZING,

  /** All systems have confirmed initialization; the scenario is ready to start. */
  INITIALIZED,

  /** The simulation scenario is actively running. */
  RUNNING,

  /** The simulation scenario is temporarily paused. */
  PAUSED
}
