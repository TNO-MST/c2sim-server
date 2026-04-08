package org.c2sim.lox;

/**
 * Global compile-time constants for the C2SIM (SISO-STD-C2SIM) protocol and schema.
 *
 * <p>These values must match the version used by the JAXB schema generator.
 */
public class Global {

  // Prevent instantiation
  private Global() {
    throw new AssertionError("Only static functions");
  }

  // Must match the version in the generator
  /** C2SIM XML Schema version (e.g. {@code "1.0.2"}). */
  public static final String C2SIM_SCHEMA_VERSION = "1.0.2";

  /** C2SIM protocol version string placed in the {@code C2SIMHeader}. */
  public static final String C2SIM_PROTOCOL_VERSION = "1.0.2";

  /** C2SIM protocol identifier placed in the {@code C2SIMHeader}. */
  public static final String C2SIM_PROTOCOL = "SISO-STD-C2SIM";
}
