# Code Style & Quality

This document describes the project code-style and static analysis setup using:

- **Spotless (Google Java Style)**
- **Maven Checkstyle Plugin**
- **SonarQube**

# 1. Spotless – Google Java Format

We use the Spotless Maven Plugin with **Google Java Style** to automatically format source code.

Usage
Format code:

Check formatting (CI mode):
mvn spotless:check

# 2. Checkstyle – Static Code Rules

We use Maven Checkstyle Plugin to enforce coding standards beyond formatting.

Run checkstyle:

```mvn
mvn validate
```

# 3. SonarQube – Code Quality & Coverage

We use SonarQube for:

* Code smells

* Bugs & vulnerabilities

* Test coverage

* Maintainability metrics

* Technical debt tracking

Run Sonar Analysis

```
mvn clean verify sonar:sonar Dsonar.projectKey=my-project \
   -Dsonar.host.url=http://localhost:9000 \
   -Dsonar.login=YOUR_TOKEN
```

## Maven code check

```mvn
mvn spotless:check
mvn spotless:apply
mvn validate (Checkstyle)
mvn test
mvn verify
mvn sonar:sonar
```

## Summary

* Spotless ensures consistent formatting (Google Style)

* Checkstyle enforces structural coding rules

* SonarQube monitors overall code quality and coverage

All tools are integrated into Maven lifecycle.



## Auto-format in IDE

**IntelliJ IDEA**:

1. Settings → Editor → Code Style → Java
2. Scheme → Import Scheme → Checkstyle Configuration
3. Select `server/config/checkstyle/checkstyle.xml`

**VS Code**:

1. Install "Checkstyle for Java" extension
2. Configure: `"java.checkstyle.configuration": "server/config/checkstyle/checkstyle.xml"`
