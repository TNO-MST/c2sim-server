package org.c2sim.server.services;

import java.io.IOException;
import java.nio.file.Path;
import org.c2sim.server.models.ServerConfigurationData;
import org.c2sim.server.security.EAuthLevel;
import org.jetbrains.annotations.NotNull;

/**
 * Service that provides access to all server configuration settings.
 *
 * <p>Settings are typically read from a configuration file on the classpath or an external mount,
 * and environment-variable overrides are supported for containerised deployments.
 */
public interface ConfigService {

  /**
   * Returns the externally advertised hostname of this server instance.
   *
   * @return the external hostname or IP address
   */
  String getExternalHostname();

  /**
   * Returns the externally advertised port of this server instance.
   *
   * @return the external port number
   */
  int getExternalPort();

  /**
   * Returns the local port on which the Javalin HTTP server listens.
   *
   * @return the web server port number
   */
  int getWebServerPortNumber();

  /**
   * Returns whether incoming C2SIM XML documents are validated against the XSD schema before being
   * published.
   *
   * @return {@code true} if XSD validation is enabled
   */
  boolean getXsdValidationEnabled();

  /**
   * Returns the maximum allowed size of a single C2SIM message in megabytes.
   *
   * @return the maximum message size in MB
   */
  float getMaxC2SimMessageSizeInMb();

  /**
   * Returns whether the license text should be printed to the console at startup.
   *
   * @return {@code true} if the license should be shown
   */
  boolean getShowLicenceInConsole();

  /**
   * Returns the path to the writable configuration folder used for runtime data.
   *
   * @return the configuration folder path
   * @throws IOException if the folder cannot be determined or created
   */
  Path getConfigFolder() throws IOException;

  /**
   * Returns the path to the folder containing the built-in (embedded) C2SIM XSD schemas.
   *
   * @return the internal XSD schema folder path
   */
  Path getC2SimXsdSchemaInternalFolder();

  /**
   * Returns the path to the folder where additional (externally mounted) C2SIM XSD schemas are
   * stored, e.g. via a Docker volume.
   *
   * @return the external XSD schema folder path
   */
  Path getC2SimXsdSchemaExternalFolder();

  /**
   * Returns the system name used by the server itself when creating C2SIM message headers.
   *
   * @return the server system name
   */
  String getSystemNameServer();

  /**
   * Reads and returns the server configuration data (default sessions, etc.).
   *
   * @return the server configuration
   * @throws IOException if the configuration file cannot be read
   */
  @NotNull
  ServerConfigurationData getServerConfiguration() throws IOException;

  /**
   * Returns the path to the directory that contains the server's static documentation files.
   *
   * @return the docs directory path
   */
  Path getDocsDirectory();

  /**
   * Returns whether Prometheus/Micrometer metrics are enabled.
   *
   * @return {@code true} if metrics are enabled
   */
  boolean getIsMetricsEnabled();

  /**
   * Returns the current authentication enforcement level.
   *
   * @return the active {@link EAuthLevel}
   */
  EAuthLevel getAuthMode();

  /**
   * Returns the public key (PEM or JWKS) used to verify Bearer tokens, or an empty string when OIDC
   * discovery is used instead.
   *
   * @return the public key string, or an empty string
   */
  String getBearerPublicKey();

  /**
   * Returns the URL of the OIDC identity provider used for token validation.
   *
   * @return the identity provider URL
   */
  String getIdentityProviderUrl();

  /** Configuration in markdown notation */
  String asMarkDownTable();

  /**
   * Is the endpoint /configuration exposed
   *
   * @return if endpoint for config is exposed
   */
  boolean getConfigEndpointIsExposed();
}
