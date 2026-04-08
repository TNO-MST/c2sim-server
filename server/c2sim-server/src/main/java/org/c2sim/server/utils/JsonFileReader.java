package org.c2sim.server.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for reading and writing JSON files using Jackson.
 *
 * <p>The internal {@link ObjectMapper} is configured with pretty-printing and lenient
 * deserialisation (unknown properties are ignored). This is intentional so that configuration files
 * written by an older server version can still be loaded by a newer version.
 *
 * <p>This is a utility class; instantiation is not allowed.
 */
public class JsonFileReader {

  // Prevent instantiation
  private JsonFileReader() {}

  private static final Logger logger = LoggerFactory.getLogger(JsonFileReader.class);

  private static final ObjectMapper objectMapper =
      new ObjectMapper()
          .enable(SerializationFeature.INDENT_OUTPUT)
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  /**
   * Reads a JSON file and deserialises its content into an instance of {@code valueType}.
   *
   * @param <T> the target type
   * @param filePath the path to the JSON file
   * @param valueType the class to deserialise into
   * @return the deserialised object, or {@code null} if the file does not exist
   * @throws IOException if the file is not readable or deserialisation fails
   */
  public static <T> T readJsonFile(Path filePath, Class<T> valueType) throws IOException {

    if (!Files.exists(filePath)) {
      logger.error("File not found: {} ", filePath);
      return null;
    }

    if (!Files.isReadable(filePath)) {
      throw new IOException("File is not readable: " + filePath);
    }

    return objectMapper.readValue(filePath.toFile(), valueType);
  }

  /**
   * Serialises {@code data} to JSON and writes it to the given file path.
   *
   * <p>Parent directories are created automatically if they do not exist.
   *
   * @param filePath the destination file path
   * @param data the object to serialise
   * @throws IOException if the file cannot be written
   */
  public static void writeJsonFile(Path filePath, Object data) throws IOException {

    Path parentDir = filePath.getParent();

    if (parentDir != null && !Files.exists(parentDir)) {
      Files.createDirectories(parentDir);
    }

    File file = filePath.toFile();
    objectMapper.writeValue(file, data);
    // file.getAbsolutePath()
  }
}
