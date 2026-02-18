# 

### Logging framework

This project uses the **Logback Java logging framework** created by the same author as Log4j (successor to Log4j).

It works together with SLF4J, which acts as a logging API (facade), while Logback is the actual implementation.



## Configure logger

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
