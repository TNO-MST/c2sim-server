package org.c2sim.server.services;

import java.io.InputStream;
import org.c2sim.lox.validation.LoxXsdValidator;

/**
 * Service for managing and applying C2SIM XSD schemas.
 *
 * <p>Implementations maintain a catalogue of supported schema versions and expose validation
 * against a named schema version.
 */
public interface C2SimSchemaService {

  /**
   * Returns the array of C2SIM schema version identifiers that this server supports.
   *
   * @return the supported schema versions (never {@code null}, may be empty)
   */
  String[] getSupportedSchemaVersions();

  /**
   * Validates the given XML stream against the named schema version.
   *
   * @param schemaVersion the schema version to validate against
   * @param xmlStream the XML content to validate
   * @return the {@link LoxXsdValidator} result containing any errors, warnings, or fatal errors
   */
  LoxXsdValidator validate(String schemaVersion, InputStream xmlStream);

  /**
   * Checks whether the given schema version is supported by this server.
   *
   * @param schemaVersion the schema version to check
   * @param throwException {@code true} to throw a {@link
   *     org.c2sim.server.exceptions.C2SimException} when the version is not supported; {@code
   *     false} to return silently
   * @return {@code true} if the schema version is supported; {@code false} if not (only when {@code
   *     throwException} is {@code false})
   */
  boolean checkIfSchemaVersionIsSupported(String schemaVersion, boolean throwException);
}
