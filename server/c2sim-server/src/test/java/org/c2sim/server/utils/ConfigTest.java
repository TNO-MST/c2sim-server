package org.c2sim.server.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

@Epic("C2SIM Server")
@Feature("C2SIM Server module")
@Story("Typed config library")
class ConfigTest {

  // --- Test enum for enum parsing ---
  enum TestEnum {
    VALUE1,
    VALUE2
  }

  // ================================
  // 🔹 Parser tests
  // ================================

  @Test
  @Description("Config type TEXT")
  void asString() {
    assertEquals("abc", Config.asString("abc"));
  }

  @Test
  @Description("Config type STRING LIST")
  void asStringList() {
    List<String> result = Config.asStringList("a;b;c");
    assertEquals(List.of("a", "b", "c"), result);
  }

  @Test
  @Description("Config type STRING LIST (null value)")
  void asStringListNull() {
    assertTrue(Config.asStringList(null).isEmpty());
  }

  @Test
  @Description("Config type FLOAT")
  void asFloat() {
    assertEquals(1.5f, Config.asFloat("1.5"));
  }

  @Test
  @Description("Config type INTEGER")
  void asInt() {
    assertEquals(10, Config.asInt("10"));
  }

  @Test
  @Description("Config type BOOLEAN (true)")
  void asBoolTrue() {
    assertTrue(Config.asBool("true"));
    assertTrue(Config.asBool("TRUE"));
    assertTrue(Config.asBool("1"));
    assertTrue(Config.asBool("yes"));
    assertTrue(Config.asBool("on"));
  }

  @Test
  @Description("Config type BOOLEAN (false)")
  void asBoolFalse() {
    assertFalse(Config.asBool("false"));
    assertFalse(Config.asBool("random"));
  }

  @Test
  @Description("Config type ENUM ")
  void asEnumIgnoringCase() {
    Function<String, TestEnum> parser = Config.asEnum(TestEnum.class);
    assertEquals(TestEnum.VALUE1, parser.apply("value1"));
  }

  @Test
  @Description("Config type ENUM (invalid)")
  void asEnum_invalidValue() {
    Function<String, TestEnum> parser = Config.asEnum(TestEnum.class);

    Exception ex = assertThrows(IllegalArgumentException.class, () -> parser.apply("invalid"));

    assertTrue(ex.getMessage().contains("Invalid value"));
  }

  @Test
  @Description("Fallback value when ENV not set")
  void builder_shouldUseDefaultValueWhenEnvMissing() {
    Config.Option<Integer> opt =
        new Config.Option<>("PORT", 8080, "Port", Integer.class, Config::asInt);

    Config config = new Config.Builder().add(opt).build(Map.of());

    assertEquals(8080, config.get(opt));
  }

  @Test
  @Description("Should use ENV when set")
  void builder_shouldUseEnvValueWhenPresent() {
    Config.Option<Integer> opt =
        new Config.Option<>("PORT", 8080, "Port", Integer.class, Config::asInt);

    Config config = new Config.Builder().add(opt).build(Map.of("PORT", "9090"));

    assertEquals(9090, config.get(opt));
  }

  @Test
  @Description("ENV variable has invalid data format")
  void builder_shouldThrowOnInvalidValueInt() {
    Config.Option<Integer> opt =
        new Config.Option<>("PORT", 8080, "Port", Integer.class, Config::asInt);

    assertThrows(
        NumberFormatException.class,
        () -> new Config.Builder().add(opt).build(Map.of("PORT", "invalid")));
  }

  @Test
  @Description("Get config value (TEXT)")
  void getTextValue() {
    Config.Option<String> opt =
        new Config.Option<>("NAME", "default", "Name", String.class, Config::asString);

    Config config = new Config.Builder().add(opt).build(Map.of("NAME", "test"));

    String value = config.get(opt);

    assertEquals("test", value);
  }

  @Test
  @Description("Table config export")
  void asTable() {
    Config.Option<String> opt =
        new Config.Option<>("NAME", "default", "Test name", String.class, Config::asString);

    Config config = new Config.Builder().add(opt).build(Map.of("NAME", "value"));

    String table = config.asTable();

    assertTrue(table.contains("NAME=value"));
    assertTrue(table.contains("Test name"));
  }

  @Test
  @Description("Table export should show enum options")
  void asTableWithEnumValues() {
    Config.Option<TestEnum> opt =
        new Config.Option<>(
            "MODE", TestEnum.VALUE1, "Mode", TestEnum.class, Config.asEnum(TestEnum.class));

    Config config = new Config.Builder().add(opt).build(Map.of("MODE", "VALUE2"));

    String table = config.asTable();

    assertTrue(table.contains("VALUE1"));
    assertTrue(table.contains("VALUE2"));
  }

  @Test
  @Description("Export config as markdown")
  void asMarkdownTable() {
    Config.Option<String> opt =
        new Config.Option<>("NAME", "default", "desc", String.class, Config::asString);

    Config config = new Config.Builder().add(opt).build(Map.of("NAME", "value"));

    String markdown = config.asMarkdownTable();

    assertTrue(markdown.contains("| NAME |"));
    assertTrue(markdown.contains("| value |"));
    assertTrue(markdown.contains("Configuration"));
  }

  @Test
  @Description("Export as markdown with special characters")
  void asMarkdownTable_shouldEscapeSpecialCharacters() {
    Config.Option<String> opt =
        new Config.Option<>("NAME", "default|x", "desc|test", String.class, Config::asString);

    Config config = new Config.Builder().add(opt).build(Map.of("NAME", "value|test"));

    String markdown = config.asMarkdownTable();

    assertTrue(markdown.contains("\\|")); // escaped pipe
  }

  @Test
  @Description("Config builder multiple config items")
  void builder_shouldHandleMultipleOptions() {
    Config.Option<Integer> port =
        new Config.Option<>("PORT", 8080, "Port", Integer.class, Config::asInt);

    Config.Option<Boolean> enabled =
        new Config.Option<>("ENABLED", false, "Enabled", Boolean.class, Config::asBool);

    Config config =
        new Config.Builder()
            .add(port)
            .add(enabled)
            .build(
                Map.of(
                    "PORT", "9000",
                    "ENABLED", "true"));

    assertEquals(9000, config.get(port));
    assertTrue(config.get(enabled));
  }
}
