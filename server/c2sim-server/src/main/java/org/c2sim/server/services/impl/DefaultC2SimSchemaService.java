package org.c2sim.server.services.impl;

import com.google.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import org.c2sim.lox.validation.LoxXsdValidator;
import org.c2sim.server.exceptions.C2SimException;
import org.c2sim.server.services.C2SimSchemaService;
import org.c2sim.server.services.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link C2SimSchemaService} implementation that discovers supported C2SIM XSD schema
 * versions from the filesystem.
 *
 * <p>On construction, both the internal (embedded) and external (Docker-mounted) schema directories
 * are scanned. A subdirectory is considered a supported schema version if it contains at least one
 * {@code .xsd} file. The subdirectory name is used as the version identifier.
 */
public class DefaultC2SimSchemaService implements C2SimSchemaService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultC2SimSchemaService.class);

  private final ConfigService configService;
  private final String[] supportedC2SimSchemaVersions;

  /**
   * Creates the service, injecting the configuration and scanning schema directories.
   *
   * @param configService the configuration service (must not be {@code null})
   */
  @Inject
  public DefaultC2SimSchemaService(ConfigService configService) {
    this.configService = Objects.requireNonNull(configService, "Config service is null");
    this.supportedC2SimSchemaVersions = detectXsdVersion();
  }

  /**
   * Returns the immediate subdirectories of {@code rootDir} that contain at least one {@code .xsd}
   * file.
   *
   * @param rootDir the directory to search
   * @return a list of paths to subdirectories that contain XSD files
   * @throws IOException if {@code rootDir} is not a directory or cannot be read
   */
  public static List<Path> findImmediateSubfoldersWithXsd(Path rootDir) throws IOException {
    if (!Files.isDirectory(rootDir)) {
      throw new IOException("Not a directory: " + rootDir);
    }

    try (Stream<Path> subfolders = Files.list(rootDir)) {
      return subfolders
          .filter(Files::isDirectory)
          .filter(DefaultC2SimSchemaService::containsXsdFile)
          .toList();
    }
  }

  private static boolean containsXsdFile(Path folder) {
    try (Stream<Path> files = Files.list(folder)) {
      return files
          .filter(Files::isRegularFile)
          .anyMatch(p -> p.toString().toLowerCase().endsWith(".xsd"));
    } catch (IOException e) {
      return false;
    }
  }

  private String[] detectXsdVersion() {
    logger.info(
        "Embedded C2Sim schema's (xsd): {} ", configService.getC2SimXsdSchemaInternalFolder());
    logger.info(
        "External C2Sim schema's (xsd): {} ", configService.getC2SimXsdSchemaExternalFolder());
    var c2simSchemaVersions = new ArrayList<String>();
    // Get internal XSD
    try {
      if (Files.exists(configService.getC2SimXsdSchemaInternalFolder())) {
        var folders =
            findImmediateSubfoldersWithXsd(configService.getC2SimXsdSchemaInternalFolder());
        c2simSchemaVersions.addAll(folders.stream().map(x -> x.getFileName().toString()).toList());
      } else {
        logger.error("C2SIM Schema folder doesn't exist.");
      }
    } catch (IOException e) {
      logger.error("Error Internal C2SIM schema: {}", e.getMessage());
    }
    // Get external XSD
    try {
      if (Files.exists(configService.getC2SimXsdSchemaExternalFolder())) {
        var folders =
            findImmediateSubfoldersWithXsd(configService.getC2SimXsdSchemaExternalFolder());
        c2simSchemaVersions.addAll(folders.stream().map(x -> x.getFileName().toString()).toList());
      }
    } catch (IOException e) {
      logger.error("Error External C2SIM schema: {}", e.getMessage());
    }
    return c2simSchemaVersions.toArray(new String[0]);
  }

  /** {@inheritDoc} */
  @Override
  public String[] getSupportedSchemaVersions() {
    return supportedC2SimSchemaVersions;
  }

  /** {@inheritDoc} */
  @Override
  public LoxXsdValidator validate(String schemaVersion, InputStream xmlStream) {
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public boolean checkIfSchemaVersionIsSupported(String schemaVersion, boolean throwException) {
    boolean isSupported = Arrays.asList(getSupportedSchemaVersions()).contains(schemaVersion);
    if (!isSupported && throwException) {
      throw new C2SimException(
          C2SimException.ErrorCode.C2SIM_SCHEMA_NOT_SUPPORTED,
          String.format(
              "C2Schema '%s' not supported by C2SIM sever, " + "supported schema's: '%s'.",
              schemaVersion, String.join("','", getSupportedSchemaVersions())),
          new HashMap<>(
              Map.of(
                  C2SimException.PROP_SUPPORTED_SCHEMA_VERSIONS,
                  String.join(";", getSupportedSchemaVersions()))));
    }
    return isSupported;
  }
}
