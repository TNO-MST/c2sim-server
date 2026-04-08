package org.c2sim.server.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Root configuration data object deserialized from the server configuration file.
 *
 * <p>Contains the list of {@link DefaultSessionCfg} entries that the server creates automatically
 * at startup. Mapped from the JSON property {@code "default-sessions"}.
 */
public class ServerConfigurationData {

  @JsonProperty("default-sessions")
  private List<DefaultSessionCfg> defaultSessions = new ArrayList<>();

  /** No-arg constructor required for Jackson deserialization. */
  @JsonCreator
  public ServerConfigurationData() {
    /* Required for deserialization */
  }

  /**
   * Returns the list of default shared-session configurations.
   *
   * @return the default session configurations
   */
  public List<DefaultSessionCfg> getDefaultSessions() {
    return defaultSessions;
  }

  /**
   * Appends a default session configuration to the list.
   *
   * @param defaultSession the session configuration to add
   */
  public void addSession(DefaultSessionCfg defaultSession) {
    this.defaultSessions.add(defaultSession);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "Config{" + "sessions=" + defaultSessions + '}';
  }

  /**
   * Configuration for a single default shared session that is created at server startup.
   *
   * <p>Deserialized from a JSON object within the {@code "default-sessions"} array.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class DefaultSessionCfg {

    /** DefaultSessionCfg object */
    public DefaultSessionCfg() {
      super();
    }

    @JsonProperty("shared-session-name")
    private String sharedSessionName;

    @JsonProperty("c2sim-schema-version")
    private String c2SimSchemaVersion;

    @JsonProperty("description")
    private String description = "";

    @JsonProperty("display-name")
    private String displayName = "";

    /**
     * Returns the name of the shared session.
     *
     * @return the shared session name
     */
    public String getSharedSessionName() {
      return sharedSessionName;
    }

    /**
     * Sets the name of the shared session.
     *
     * @param sessionName the shared session name
     * @return this object for chaining
     */
    public DefaultSessionCfg setSharedSessionName(String sessionName) {
      this.sharedSessionName = sessionName;
      return this;
    }

    /**
     * Returns the C2SIM schema version for this session.
     *
     * @return the schema version string
     */
    public String getC2SimSchemaVersion() {
      return c2SimSchemaVersion;
    }

    /**
     * Sets the C2SIM schema version for this session.
     *
     * @param schemaVersion the schema version string
     * @return this object for chaining
     */
    public DefaultSessionCfg setC2SimSchemaVersion(String schemaVersion) {
      this.c2SimSchemaVersion = schemaVersion;
      return this;
    }

    /**
     * Returns the human-readable description of the session.
     *
     * @return the description string
     */
    public String getDescription() {
      return description;
    }

    /**
     * Sets the human-readable description of the session.
     *
     * @param description the description string
     * @return this object for chaining
     */
    public DefaultSessionCfg setDescription(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the display name shown in the server UI.
     *
     * @param displayName the display name
     * @return this object for chaining
     */
    public DefaultSessionCfg setDisplayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    /**
     * Returns the display name shown in the server UI.
     *
     * @return the display name
     */
    public String getDisplayName() {
      return displayName;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
      return "Session{"
          + "default-session-name='"
          + sharedSessionName
          + '\''
          + "c2sim-schema-version='"
          + c2SimSchemaVersion
          + '\''
          + '}';
    }
  }
}
