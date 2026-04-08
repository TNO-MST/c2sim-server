package org.c2sim.server.services.impl;

import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.c2sim.server.models.ServerConfigurationData;
import org.c2sim.server.security.EAuthLevel;
import org.c2sim.server.services.ConfigService;
import org.c2sim.server.services.EnvService;
import org.c2sim.server.utils.Config;
import org.c2sim.server.utils.FileHelper;
import org.c2sim.server.utils.JsonFileReader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link ConfigService} implementation that reads server configuration from environment
 * variables. The predefined shared session names are configured in a JSON configuration file.
 *
 * <p>All environment variables recognised by this service are prefixed with {@code C2SIM_}. On
 * construction the resolved configuration is printed to the log so operators can verify the active
 * settings at a glance.
 *
 * <p>If the {@code server-config.json} file does not exist on first startup, a default
 * configuration with a single shared session ({@code "default"} / schema {@code "1.0.2"}) is
 * generated and written to disk automatically.
 */
public class DefaultConfigService implements ConfigService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultConfigService.class);
  // All ENV variable must start with C2SIM_ (easy to see what belong to C2SIm server)
  private static final Config.Option<Boolean> C2SIM_XSD_VALIDATION_ENABLED =
      new Config.Option<>(
          "C2SIM_XSD_VALIDATION_ENABLED",
          true,
          "Xsd validation for C2SIM XML enabled",
          Boolean.class,
          Config::asBool);
  private static final Config.Option<Float> C2SIM_MAX_MSG_SIZE_MB =
      new Config.Option<>(
          "C2SIM_MAX_MSG_SIZE_MB",
          10.0f,
          "Max REST body size (MB) for C2SIM messages",
          Float.class,
          Config::asFloat);
  private static final Config.Option<String> C2SIM_EXTERNAL_HOSTNAME =
      new Config.Option<>(
          "C2SIM_EXTERNAL_HOSTNAME",
          "127.0.0.1",
          "External hostname or ip address of C2SIM server (when empty ignore)",
          String.class,
          Config::asString);
  private static final Config.Option<Integer> C2SIM_EXTERNAL_PORT_NUMBER =
      new Config.Option<>(
          "C2SIM_EXTERNAL_PORT_NUMBER",
          7777,
          "External port number of C2SIM server",
          Integer.class,
          Config::asInt);
  private static final Config.Option<EAuthLevel> C2SIM_AUTH_MODE =
      new Config.Option<>(
          "C2SIM_AUTH_MODE",
          EAuthLevel.MIXED_AUTH,
          "Bearer tokens (not required (disables all auth) / mandatory / mixed)",
          EAuthLevel.class,
          Config.asEnum(EAuthLevel.class));
  private static final Config.Option<String> C2SIM_BEARER_PUBLIC_KEY =
      new Config.Option<>(
          "C2SIM_BEARER_PUBLIC_KEY",
          "",
          "When empty: public key fetched from Identity Provider. When set, this key is used (no IDP needed)",
          String.class,
          Config::asString);
  private static final Config.Option<String> C2SIM_IDENTITY_PROVIDER_URL =
      new Config.Option<>(
          "C2SIM_IDENTITY_PROVIDER_URL",
          "http://localhost:8080/realms/c2sim/.well-known/openid-configuration",
          "The URL of the Identity Provider",
          String.class,
          Config::asString);
  private static final Config.Option<Boolean> C2SIM_EXPOSE_CFG_ENDPOINT =
      new Config.Option<>(
          "C2SIM_EXPOSE_CFG_ENDPOINT",
          true,
          "Configuration endpoint /configuration is be exposed ",
          Boolean.class,
          Config::asBool);

  private static final String DEFAULT_SHARED_SESSION_NAME = "default";
  private static final String DEFAULT_SHARED_SESSION_SCHEMA_VERSION = "1.0.2";
  private final Config cfg;
  private ServerConfigurationData serverConfig;
  private final EnvService envService;

  /**
   * Creates the service, reads all {@code C2SIM_*} environment variables, and logs the resolved
   * configuration table. * @param envService Used for mocking getEnv
   */
  @Inject
  public DefaultConfigService(EnvService envService) {
    Objects.requireNonNull(envService);
    this.envService = envService;
    Config config = null;
    try {
      config =
          new Config.Builder()
              .add(C2SIM_XSD_VALIDATION_ENABLED)
              .add(C2SIM_MAX_MSG_SIZE_MB)
              .add(C2SIM_EXTERNAL_HOSTNAME)
              .add(C2SIM_EXTERNAL_PORT_NUMBER)
              .add(C2SIM_AUTH_MODE)
              .add(C2SIM_BEARER_PUBLIC_KEY)
              .add(C2SIM_IDENTITY_PROVIDER_URL)
              .add(C2SIM_EXPOSE_CFG_ENDPOINT)
              .build(envService.getenv()); // reads System.getenv
      var table = config.asTable();
      logger.info("Server config:\n {}", table);
    } catch (Exception e) {
      logger.error("Failed to load config: {}", e.getMessage(), e);
      System.exit(-1);
    }
    cfg = config;
  }

  /**
   * Current configuration in markdown (ENV name, datatype, value)
   *
   * @return Markdown document
   */
  public String asMarkDownTable() {
    return cfg.asMarkdownTable();
  }

  /** {@inheritDoc} */
  @Override
  public boolean getConfigEndpointIsExposed() {
    return cfg.get(C2SIM_EXPOSE_CFG_ENDPOINT);
  }

  /** {@inheritDoc} */
  @Override
  public String getExternalHostname() {
    return cfg.get(C2SIM_EXTERNAL_HOSTNAME);
  }

  /** {@inheritDoc} */
  @Override
  public int getExternalPort() {
    return cfg.get(C2SIM_EXTERNAL_PORT_NUMBER);
  }

  /** {@inheritDoc} */
  @Override
  public int getWebServerPortNumber() {
    return 7777;
  }

  /** {@inheritDoc} */
  @Override
  public boolean getXsdValidationEnabled() {
    return cfg.get(C2SIM_XSD_VALIDATION_ENABLED);
  }

  /** {@inheritDoc} */
  @Override
  public boolean getShowLicenceInConsole() {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public float getMaxC2SimMessageSizeInMb() {
    return cfg.get(C2SIM_MAX_MSG_SIZE_MB);
  }

  /**
   * Reads the server configuration from {@code config/server-config.json} relative to the executing
   * directory.
   *
   * <p>If the file does not exist a default configuration is generated and written to disk. If the
   * file is present but cannot be parsed, the process exits with status {@code 1}.
   *
   * @return the parsed (or generated) {@link ServerConfigurationData}
   * @throws IOException if the config folder cannot be created
   */
  public @NotNull ServerConfigurationData getServerConfiguration() throws IOException {
    Path configFile = getConfigFolder().resolve("server-config.json");

    // Check if config.xml exists, create if not
    if (Files.exists(configFile)) {
      try {
        serverConfig = JsonFileReader.readJsonFile(configFile, ServerConfigurationData.class);
      } catch (IOException e) {
        logger.error("Server config invalid: {}", configFile, e);
        System.exit(1);
      }
    }
    if (serverConfig == null) {
      logger.error("Server config '{}' not found, create default server config ", configFile);
      // Just create default config
      serverConfig = new ServerConfigurationData();
      serverConfig.addSession(
          new ServerConfigurationData.DefaultSessionCfg()
              .setSharedSessionName(DEFAULT_SHARED_SESSION_NAME)
              .setDisplayName(DEFAULT_SHARED_SESSION_NAME)
              .setC2SimSchemaVersion(DEFAULT_SHARED_SESSION_SCHEMA_VERSION)
              .setDescription("Default shared session"));
      try {
        JsonFileReader.writeJsonFile(configFile, serverConfig);
      } catch (IOException e) {
        logger.error("Failed to write generated default server config.", e); // Not important
      }
    }
    return serverConfig;
  }

  /** {@inheritDoc} */
  @Override
  public Path getDocsDirectory() {
    // TODO Update this for now hardcoded
    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("linux")) {
      // Docker image
      return Path.of("/app/docs");
    }

    return Path.of("C:\\development\\c2sim-server\\docs\\site");
  }

  /** {@inheritDoc} */
  @Override
  public boolean getIsMetricsEnabled() {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public EAuthLevel getAuthMode() {
    return cfg.get(C2SIM_AUTH_MODE);
  }

  /** {@inheritDoc} */
  @Override
  public String getBearerPublicKey() {
    return cfg.get(C2SIM_BEARER_PUBLIC_KEY); // Empty is not used
  }

  /** {@inheritDoc} */
  @Override
  public String getIdentityProviderUrl() {
    return cfg.get(C2SIM_IDENTITY_PROVIDER_URL);
  }

  /**
   * Returns the path to the {@code config/} directory relative to the executing JAR, creating it if
   * it does not yet exist.
   *
   * @return the config directory path
   * @throws IOException if the directory cannot be created
   */
  public Path getConfigFolder() throws IOException {
    Path configDir = FileHelper.getExecutingDirectory().resolve("config");

    if (!Files.exists(configDir)) {
      Files.createDirectories(configDir);
    }
    return configDir;
  }

  /**
   * Returns the path to the internal (embedded) C2SIM XSD schema folder.
   *
   * @return path to {@code config/c2sim_schemas/internal}
   */
  public Path getC2SimXsdSchemaInternalFolder() {
    return FileHelper.getExecutingDirectory().resolve("config/c2sim_schemas/internal");
  }

  /**
   * Returns the path to the external (Docker-mounted) C2SIM XSD schema folder.
   *
   * @return path to {@code config/c2sim_schemas/external}
   */
  public Path getC2SimXsdSchemaExternalFolder() {
    return FileHelper.getExecutingDirectory().resolve("config/c2sim_schemas/external");
  }

  /** {@inheritDoc} */
  @Override
  public String getSystemNameServer() {
    return "C2SIM_SERVER";
  }
}
