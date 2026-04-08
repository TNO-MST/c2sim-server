package org.c2sim.cli.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and saves {@link AppConfig} as pretty-printed JSON in {@code config.json}
 * (relative to the current working directory).
 */
public final class ConfigManager {

    public static final String CONFIG_FILE = "config.json";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private ConfigManager() {}

    /** Loads config from disk, or returns a fresh default config if the file is missing. */
    public static AppConfig load() {
        Path path = Path.of(CONFIG_FILE);
        if (Files.exists(path)) {
            try (Reader r = Files.newBufferedReader(path)) {
                AppConfig cfg = GSON.fromJson(r, AppConfig.class);
                return cfg != null ? cfg : new AppConfig();
            } catch (IOException e) {
                return new AppConfig();
            }
        }
        return new AppConfig();
    }

    /** Persists the config to disk. Silently ignores I/O errors. */
    public static void save(AppConfig config) {
        try (Writer w = Files.newBufferedWriter(Path.of(CONFIG_FILE))) {
            GSON.toJson(config, w);
        } catch (IOException ignored) {}
    }
}
