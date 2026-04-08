package org.c2sim.server.utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Immutable typed configuration container populated from environment variables.
 *
 * <p>Configuration options are declared as {@link Option} constants and registered with the {@link
 * Builder}. On {@link Builder#build()} each option is resolved from {@link System#getenv()} (or a
 * supplied map for testing): when the environment variable is set the raw string is parsed via the
 * option's {@code parser}; otherwise the {@code defaultValue} is used.
 *
 * <p>The resolved values are exposed via {@link #get(Option)} and can be pretty-printed as a table
 * via {@link #asTable()}.
 *
 * <p>This class is final and immutable; all nested types are also final.
 */
public final class Config {

  private static final Logger logger = LoggerFactory.getLogger(Config.class);

  private final List<Option<?>> options;
  private final Map<String, Value<?>> resolved;

  private Config(List<Option<?>> options, Map<String, Value<?>> resolved) {
    this.options = List.copyOf(options);
    this.resolved = Map.copyOf(resolved);
  }

  /**
   * Identity parser — returns the raw string unchanged.
   *
   * @param s the raw environment variable value
   * @return the same string
   */
  // handy parser helpers
  public static String asString(String s) {
    return s;
  }

  /**
   * Splits a semicolon-delimited string into a list of strings.
   *
   * @param s the raw environment variable value (may be {@code null})
   * @return the list of values, or an empty list if {@code s} is {@code null}
   */
  public static List<String> asStringList(String s) {
    return s == null ? Collections.emptyList() : Arrays.asList(s.split(";"));
  }

  /**
   * Parses a raw string as a {@code float}.
   *
   * @param s the raw environment variable value
   * @return the parsed float
   * @throws NumberFormatException if the string is not a valid float
   */
  public static float asFloat(String s) {
    return Float.parseFloat(s);
  }

  /**
   * Parses a raw string as an {@code Integer}.
   *
   * @param s the raw environment variable value
   * @return the parsed integer
   * @throws NumberFormatException if the string is not a valid integer
   */
  public static Integer asInt(String s) {
    return Integer.parseInt(s);
  }

  /**
   * Parses a raw string as a {@code Boolean}.
   *
   * <p>Recognised truthy values (case-insensitive): {@code "1"}, {@code "true"}, {@code "yes"},
   * {@code "on"}.
   *
   * @param s the raw environment variable value
   * @return {@code true} if the value matches a truthy string
   */
  public static Boolean asBool(String s) {
    return "1".equals(s)
        || "true".equalsIgnoreCase(s)
        || "yes".equalsIgnoreCase(s)
        || "on".equalsIgnoreCase(s);
  }

  /**
   * Returns a parser that converts a raw string to the given enum type.
   *
   * <p>The conversion is case-insensitive (the value is upper-cased before lookup).
   *
   * @param <E> the enum type
   * @param enumClass the enum class
   * @return a function that maps a string to an enum constant
   * @throws IllegalArgumentException if the string does not match any constant
   */
  public static <E extends Enum<E>> Function<String, E> asEnum(Class<E> enumClass) {
    return s -> {
      try {
        return Enum.valueOf(enumClass, s.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "Invalid value '"
                + s
                + "' for enum "
                + enumClass.getSimpleName()
                + ". Allowed: "
                + Arrays.toString(enumClass.getEnumConstants()));
      }
    };
  }

  /**
   * Returns the resolved value for the given option.
   *
   * @param <T> the value type
   * @param opt the option descriptor
   * @return the resolved value (never {@code null} for options with a default)
   */
  @SuppressWarnings("unchecked")
  public <T> T get(Option<T> opt) {

    return (T) resolved.get(opt.key).configValue;
  }

  /**
   * Returns a human-readable table of all resolved configuration values.
   *
   * <p>For enum-typed options the list of allowed values is appended.
   *
   * @return a multi-line string suitable for logging at startup
   */
  public String asTable() {
    StringBuilder sb = new StringBuilder();
    sb.append("Config:\n");

    for (Option<?> o : options) {
      Value<?> v = resolved.get(o.key);

      sb.append(String.format("- %s=%s (%s)", o.env, v.configValue, o.description));

      // 🔹 If enum → show allowed values
      if (o.dataType.isEnum()) {

        sb.append(" Allowed values: ");
        sb.append(
            Arrays.stream(o.dataType.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", ")));
      }

      sb.append("\n");
    }

    return sb.toString();
  }

  public String asMarkdownTable() {
    StringBuilder sb = new StringBuilder();

    sb.append("## Configuration\n\n");
    sb.append("| Environment | Description | Data Type | Default | Value | Allowed Values |\n");
    sb.append(
        "|-------------|--------|-----------|-------------|-------------|----------------|\n");

    for (Option<?> o : options) {
      Value<?> v = resolved.get(o.key);

      sb.append("| ")
          .append(escape(o.env))
          .append(" | ")
          .append(escape(o.description))
          .append(" | ")
          .append(o.dataType.getSimpleName())
          .append(" | ")
          .append(escape(String.valueOf(o.defaultValue)))
          .append(" | ")
          .append(escape(String.valueOf(v.configValue)))
          .append(" | ");

      if (o.dataType.isEnum()) {
        String allowed =
            Arrays.stream(o.dataType.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        sb.append(escape(allowed));
      } else {
        sb.append("-");
      }

      sb.append(" |\n");
    }

    return sb.toString();
  }

  private String escape(String input) {
    if (input == null) {
      return "";
    }
    return input.replace("|", "\\|").replace("\n", " ");
  }

  /** Indicates the source of a resolved configuration value. */
  public enum Source {
    /** Value was read from the environment. */
    ENV,
    /** Default value was used because the environment variable was absent. */
    DEFAULT
  }

  /**
   * Descriptor for a single typed configuration option backed by an environment variable.
   *
   * @param <T> the value type
   */
  public static final class Option<T> {

    /** The environment-variable name used as the map key. */
    public final String key;

    /** The environment-variable name. */
    public final String env;

    /** Human-readable description shown in the config table. */
    public final String description;

    /** The value used when the environment variable is not set. */
    public final T defaultValue;

    /** Parser that converts the raw environment-variable string to {@code T}. */
    public final Function<String, T> parser;

    /** The Java type of the resolved value. */
    public final Class<T> dataType;

    /**
     * Creates an option descriptor.
     *
     * @param env the environment-variable name
     * @param defaultValue the fallback value when the variable is absent
     * @param description a human-readable description for the config table
     * @param type the Java type of the resolved value
     * @param parser a function that parses the raw string to {@code T}
     */
    public Option(
        String env, T defaultValue, String description, Class<T> type, Function<String, T> parser) {
      this.key = env != null ? env : UUID.randomUUID().toString();
      this.env = env;
      this.defaultValue = defaultValue;
      this.parser = parser;
      this.description = description;
      this.dataType = type;
    }
  }

  /**
   * Holds the resolved value and its origin for a single {@link Option}.
   *
   * @param <T> the value type
   */
  public static final class Value<T> {

    /** The option this value belongs to. */
    public final Option<T> option;

    /** The resolved configuration value. */
    public final T configValue;

    /** Whether the value came from the environment or the option default. */
    public final Source source;

    private Value(Option<T> option, T value, Source source) {
      this.option = option;
      this.configValue = value;
      this.source = source;
    }
  }

  /**
   * Builder that collects {@link Option} declarations and resolves them against an environment map
   * to produce an immutable {@link Config}.
   */
  public static class Builder {

    /** Builder pattern class to generate config */
    public Builder() {
      super();
    }

    private final List<Option<?>> options = new ArrayList<>();

    private static <T> Value<T> resolve(Option<T> o, Map<String, String> env) {
      String raw = env.get(o.env);
      if (raw != null) {
        try {
          return new Value<>(o, o.parser.apply(raw), Source.ENV);
        } catch (Exception ex) {
          logger.error(
              "The ENV variable '{}' has INVALID value '{}' (datatype = {})",
              o.key,
              raw,
              o.dataType.getSimpleName());
          throw ex;
        }
      }
      return new Value<>(o, o.defaultValue, Source.DEFAULT);
    }

    /**
     * Registers an option with this builder.
     *
     * @param <T> the value type
     * @param opt the option to register
     * @return this builder for chaining
     */
    public <T> Builder add(Option<T> opt) {
      options.add(opt);
      return this;
    }

    /**
     * Builds the {@link Config} by resolving all registered options from {@link System#getenv()}.
     *
     * @return the immutable configuration
     */
    public Config build() {
      return build(System.getenv());
    }

    /**
     * Builds the {@link Config} by resolving all registered options from the supplied map.
     *
     * <p>Useful for unit tests that need to inject specific environment values.
     *
     * @param env the environment map
     * @return the immutable configuration
     */
    public Config build(Map<String, String> env) {
      Map<String, Value<?>> out = new LinkedHashMap<>();
      for (Option<?> o : options) {
        out.put(o.key, resolve(o, env));
      }
      return new Config(options, out);
    }
  }
}
