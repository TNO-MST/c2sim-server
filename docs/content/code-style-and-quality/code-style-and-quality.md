# Code Style & Quality

 The [Google Java Format](https://github.com/google/google-java-format) java code styling is applied to the code.



## Spotless – Google Java Format

 [Spotless Maven Plugin](https://github.com/diffplug/spotless) can be applied to configure source code according **Google Java Style** format. An alternative is [spotify fmt maven plugin](https://github.com/spotify/fmt-maven-plugin) (less option to configure)

| Maven cmd            | Description              |
| -------------------- | ------------------------ |
| `mvn spotless:check` | check formatting         |
| `mvn spotless:apply` | apply formatting changes |

## Checkstyle – Static Code Rules

We use [Maven Checkstyle Plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/) to enforce coding standards beyond formatting.

- [checkstyle:checkstyle](https://maven.apache.org/plugins/maven-checkstyle-plugin/checkstyle-mojo.html) is a reporting goal that performs Checkstyle analysis and generates a report on violations.
- [checkstyle:checkstyle-aggregate](https://maven.apache.org/plugins/maven-checkstyle-plugin/checkstyle-aggregate-mojo.html) is a reporting goal that performs Checkstyle analysis and generates an aggregate HTML report on violations in a multi-module reactor build.
- [checkstyle:check](https://maven.apache.org/plugins/maven-checkstyle-plugin/check-mojo.html) is a goal that performs Checkstyle analysis and outputs violations or a count of violations to the console, potentially failing the build. It can also be configured to re-use an earlier analysis.

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
