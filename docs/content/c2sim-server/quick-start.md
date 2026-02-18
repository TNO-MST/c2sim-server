# Quick start C2-SIM Server

## Prerequisites (running locally)

### Required Development Software

| Tool         | Version                           | Purpose                      |
| ------------ | --------------------------------- | ---------------------------- |
| **Java JDK** | 21                                | Primary development language |
| **Maven**    | 3.13.0+                           | Build system                 |
| **Git**      | Latest                            | Version control              |
| **Docker**   | Latest                            | Containerization and testing |
| **IDE**      | IntelliJ IDEA / VS Code / eclipse | Development environment      |

## Project Setup

### 1. Clone Repository

```bash
git clone <repository-url>
```

### 2. Build Project (locally)

```bash
cd server
mvn clean package
```

**Expected output**:

```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO]
[INFO] c2sim ............................................. SUCCESS [  0.5s]
[INFO] c2sim-statemachine ................................ SUCCESS [  2.1s]
[INFO] lox ............................................... SUCCESS [  3.2s]
[INFO] open-api-javalin-server-stub ...................... SUCCESS [  1.8s]
[INFO] c2sim-authorizer .................................. SUCCESS [  1.5s]
[INFO] c2sim-server ...................................... SUCCESS [  4.3s]
[INFO] c2sim-client ...................................... SUCCESS [  2.1s]
[INFO] c2sim-client-app .................................. SUCCESS [  0.8s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### 3. Run Tests

```bash
mvn test
```

### 4. Code Style Check

```bash
mvn checkstyle:check
```

# Docker build

Automated build in docker can de done trough the docker compose. 

```bash
cd docker
docker compose build
```

To start c2sim server server

```bash
docker compose up -d
```

More information in docker section.

# 

### Logging Configuration

**File**: `server/c2sim-server/src/main/resources/logback.xml`

```xml
<configuration>
    <!-- Set log level for C2SIM classes -->
    <logger name="org.c2sim" level="DEBUG"/>

    <!-- Suppress verbose libraries -->
    <logger name="org.eclipse.jetty" level="WARN"/>
    <logger name="io.javalin" level="INFO"/>

    <!-- Root logger -->
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

**Change levels at runtime** (in code):

```java
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

Logger logger = (Logger) LoggerFactory.getLogger("org.c2sim");
logger.setLevel(Level.TRACE);
```
