package org.c2sim.server.services.impl;

import com.google.inject.Inject;
import org.c2sim.lox.LoxSchemaInfo;
import org.c2sim.lox.exceptions.ValidationException;
import org.c2sim.lox.validation.LoxXsdValidator;
import org.c2sim.server.exceptions.C2SimException;
import org.c2sim.server.services.C2SimSchemaService;
import org.c2sim.server.services.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

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
    private final Map<String, SchemaInfo> schemaVersionFolders = new HashMap<>(); // schema version -> folder path

    /**
     * Creates the service, injecting the configuration and scanning schema directories.
     *
     * @param configService the configuration service (must not be {@code null})
     */
    @Inject
    public DefaultC2SimSchemaService(ConfigService configService) {
        this.configService = Objects.requireNonNull(configService, "Config service is null");
        initializeXsdFolder();
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

    private static long countXsdFiles(Path folder) {
            try (Stream<Path> paths = Files.list(folder)) {

                return paths
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().toLowerCase().endsWith(".xsd"))
                        .count();
            } catch (IOException e) {
                // dont care
            }
            return 0;

    }

    // Get namespace from XSD file
    // Not bullet prove, but does the job...
    private static String extractNamespaceFromXsdSchema(File xsdFile) throws Exception {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            Document doc = factory.newDocumentBuilder().parse(xsdFile);

            Element root = doc.getDocumentElement();

            // 1. Preferred: targetNamespace
            String ns = root.getAttribute("targetNamespace");

            if (!ns.isBlank()) {
                return ns;
            }

            // 2. Fallback: default xmlns
            ns = root.getAttribute(XMLConstants.XMLNS_ATTRIBUTE);

            if (!ns.isBlank()) {
                return ns;
            }
        } catch (Exception ex) {
            // don;t care
        }
        // 3. No namespace
        return "";
    }

    // Read XSD in folder and extract namespace
    private static XsdNamespaceInfo[] extractNamespacesFromFolder(File folder) {

        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return new XsdNamespaceInfo[0];
        }

        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".xsd"));

        if (files == null || files.length == 0) {
            return new XsdNamespaceInfo[0];
        }
        List<XsdNamespaceInfo> namespaces = new ArrayList<>();
        for (File file : files) {
            try {
                String namespace = extractNamespaceFromXsdSchema(file);
                namespaces.add(new XsdNamespaceInfo(file.toURI().toURL(), namespace));
            } catch (Exception e) {
                logger.warn("Failed to extract namespace from XSD file: {}", file.getAbsolutePath(), e);
            }
        }
        return namespaces.toArray(new XsdNamespaceInfo[0]);
    }

    private void initializeXsdFolder() {
        logger.info("The C2SIM server uses LOX library which is build (JAXB) against XSD namespace '{}' ",
                LoxSchemaInfo.getC2SimNamespace());
        logger.info("The XSD schema's must be compliant with this schema namespace (extend it)!");


        logger.info("Embedded C2Sim schema's (xsd): {} ", configService.getC2SimXsdSchemaInternalFolder());
        logger.info("External C2Sim schema's (xsd): {} ", configService.getC2SimXsdSchemaExternalFolder());

        // Get internal XSD
        try {
            if (Files.exists(configService.getC2SimXsdSchemaInternalFolder())) {
                var folders = findImmediateSubfoldersWithXsd(configService.getC2SimXsdSchemaInternalFolder());
                // Add schema folder
                folders.forEach(x -> registerSchema(x.getFileName().toString(), x.toAbsolutePath()));
            } else {
                logger.error("C2SIM Schema folder doesn't exist.");
            }
        } catch (IOException e) {
            logger.error("Error Internal C2SIM schema: {}", e.getMessage());
        }
        // Get external XSD
        try {
            if (Files.exists(configService.getC2SimXsdSchemaExternalFolder())) {
                var folders = findImmediateSubfoldersWithXsd(configService.getC2SimXsdSchemaExternalFolder());
                // Add schema folder
                folders.forEach(x -> registerSchema(x.getFileName().toString(), x.toAbsolutePath()));
            }
        } catch (IOException e) {
            logger.error("Error External C2SIM schema: {}", e.getMessage());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getSupportedSchemaVersions() {
        return schemaVersionFolders.keySet().toArray(new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LoxXsdValidator validate(String schemaVersion, InputStream xmlStream) throws ValidationException {
        Objects.requireNonNull(schemaVersion);
        var info = schemaVersionFolders.getOrDefault(schemaVersion, null);
        if (info != null) {
            return LoxXsdValidator.doValidation(schemaVersion, info.xsdFiles[0].fileName, ns -> null, xmlStream);

        } else {
            // use default schema of lox library
            return LoxXsdValidator.doValidation(xmlStream);
        }
    }

    /**
     * {@inheritDoc}
     */
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

    private void registerSchema(
            String version,
            Path folder
    ) {
        Objects.requireNonNull(version);
        Objects.requireNonNull(folder);

        // Check if key is already added
        if (schemaVersionFolders.containsKey(version)) {
            logger.warn("XSD schema {} already added, skip", version);
            return;
        }

        // Check if folder exist (is already done, safety check)
        var path = folder.toFile();
        if (!path.exists() || !path.isDirectory()) {
            logger.warn("Folder '{}' does not exist, skip version {}", folder, version);
            return;
        }

        // For now only one XSD files is supported....
        var count = countXsdFiles(folder);
        if (count  != 1) {
            logger.warn("The folder '{}' must contain exactly one XSD definition, skip version '{}'.", folder, version);
            return;
        }


        var xsdFiles = extractNamespacesFromFolder(folder.toFile());
        SchemaInfo schemaInfo = new SchemaInfo(version, xsdFiles, folder);
        schemaVersionFolders.put(version, schemaInfo);

        var namespaceInOneLine = String.join("|", Arrays.stream(xsdFiles).map(x -> x.namespace).toList()); // this is for now always 1 entry
        logger.info(
                "Registered xsd schema: version folder='{}'  namespace={} folder={}",
                version,
                namespaceInOneLine.isEmpty() ? "<no namespace found>" : namespaceInOneLine,
                folder
        );
    }

    /**
     * Internal immutable schema descriptor
     */
    private record SchemaInfo(
            String version,
            XsdNamespaceInfo[] xsdFiles,
            Path folder
    ) {
    }

    private record XsdNamespaceInfo(
            URL fileName,
            String namespace
    ) {
    }
}
