# Code Style & Quality

 The [Google Java Format](https://github.com/google/google-java-format) java code styling is applied to the code.

* Spotless = formatting  

* Checkstyle = structural/style enforcement

## Spotless – Google Java Format

 [Spotless Maven Plugin](https://github.com/diffplug/spotless) can be applied to configure source code according **Google Java Style** format. Also often used is [spotify fmt maven plugin](https://github.com/spotify/fmt-maven-plugin), but has less option for configuration.

| Maven cmd            | Description                                                                                                     |
| -------------------- | --------------------------------------------------------------------------------------------------------------- |
| `mvn spotless:check` | Check the formatting and throw errors when style is not correct. This is also done in the maven phase `verify`. |
| `mvn spotless:apply` | This will automatically apply the formatting (removes style errors if present)                                  |

## Checkstyle – Static Code Rules

[Maven Checkstyle Plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/) is used to enforce coding standards beyond formatting.

```
mvn clean verify
```

## SonarQube – Code Quality & Coverage

Tool to analyze source code, more information [here](sonar-qube.md)

## Auto-format in IDE

**IntelliJ IDEA**:

1. Settings → Editor → Code Style → Java
2. Scheme → Import Scheme → Checkstyle Configuration
3. Select `server/config/checkstyle/checkstyle.xml`

**VS Code**:

1. Install "Checkstyle for Java" extension
2. Configure: `"java.checkstyle.configuration": "server/config/checkstyle/checkstyle.xml"`
