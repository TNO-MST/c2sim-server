# Module c2sim-client

## Overview

`C2SimClient` is a Java client library that enables applications to connect to the C2SIM server, join shared simulation sessions, publish and receive C2SIM XML messages, and participate in collaborative command-and-control simulations.

The client handles:

- **Session Management**: Join/resign from shared sessions with other systems

- **Message Publishing**: Send C2SIM XML documents with XSD validation

- **Message Receiving**: Stream C2SIM messages via WebSocket with optional validation/decoding

- **State Synchronization**: Track and respond to C2SIM server state changes

- **OIDC Authentication**: Secure connections with JWT tokens from OIDC providers

- **Error Handling**: Comprehensive exception handling and retry logic

## Installation

Add the C2SIM client library to your Maven `pom.xml`:

```xml
<dependency>
    <groupId>org.c2sim</groupId>
    <artifactId>c2sim-client</artifactId>
    <version>1.0.2-federates</version>
</dependency>
```

## Quick Start

### Basic Connection

```java
import org.c2sim.client.C2SimClient;
import org.c2sim.client.security.OidcCredentialFlow;
import org.c2sim.client.security.OidcCredentialFlowConfig;
import java.net.URI;

// Set up OIDC authentication
OidcCredentialFlowConfig authCfg = new OidcCredentialFlowConfig(
    URI.create("http://localhost:8080/realms/c2sim/.well-known/openid-configuration"),
    "client",      // client ID
    "secret"       // client secret
);

OidcTokenProvider oidcProvider = new OidcCredentialFlow(authCfg);

// Create and connect client
C2SimClient client = new C2SimClient.Builder()
    .url("http://localhost:7777/api")
    .systemName("MY_SYSTEM")
    .clientIdDisplayName("MyClient")
    .oidcProvider(oidcProvider)
    .build();

client.connect();  // Joins the "default" shared session
```

## Builder Pattern

The `C2SimClient.Builder` provides a fluent API for configuring the client:

```java
C2SimClient client = new C2SimClient.Builder()
    .url("http://localhost:7777/api") // Server API endpoint (required)
    .systemName("SYSTEM_A")           // System name for this client (required)
    .clientIdDisplayName("Client-A")  // Display name (optional, auto-generated if omitted)
    .clientId("custom-client-id")     // Explicit client ID (optional)
    .sharedSessionName("default")     // Session to join (default: "default")
    .oidcProvider(oidcProvider)       // OIDC token provider (optional)
    .listener(new MyClientListener())// Lifecycle callbacks (optional)
    .enableSendMessageValidation()   // Validate XML before sending (optional)
    .enableReceivedMessageValidation() // Validate received XML (optional)
    .enableReceivedMessageDecode()   // Decode XML to Java POJOs (optional)
    .beautifyXml()                   // Pretty-print sent XML (optional)
    .build();
```

## Core Concepts

### Shared Sessions

A shared session allows multiple clients (systems) to collaborate in a simulation:

```java
// Join a session
client.connect();  // or client.connectAsync() for non-blocking
// Check connection status
boolean isJoined = client.isJoined();
boolean hasStream = client.hasStreamToSharedSession();
// Get session information
List<DynamicSessionInfo> sessions = client.getSharedSessionsFromC2SimServer();
DynamicSessionInfo sessionInfo = client.getSessionInfoFromC2SimServer("default");
// Disconnect
client.disconnect();
```

### State Management

Track the C2SIM server state:

```java
// Get cached state

StateType state = client.getCachedC2SimServerState();
// Listen for state changes with a listener
client.setC2simClientListener(new C2SimClient.C2SimClientListener() {
    @Override
    public void onStateChanged(C2SimClient client, StateType oldState, StateType newState) {
        System.out.println("State changed: " + oldState + " -> " + newState);
    }
});
```

**State Transitions**: `UNINITIALIZED` → `INITIALIZING` → `INITIALIZED` → `RUNNING` (↔ `PAUSED`)

### Message Publishing

Send C2SIM XML documents to the server:

```java
// Publish from a Java POJO (requires JAXB bindings)

MessageType message = createMyC2SimMessage();
client.publishC2SimDocument(message);
// Publish raw XML string
String xmlDocString = "<C2SIM>...</C2SIM>";
client.publishC2SimDocument(xmlDocString);
// Publish with explicit validation
LoxXsdValidator validation = client.publishC2SimDocument(xmlString, true);
if (validation != null && !validation.isValid()) {
    System.out.println("Validation errors: " + validation.getValidationErrors());
}
```

### Message Receiving

Consume C2SIM messages via WebSocket stream:

```java
// Register callback for received messages
client.onReceivedMessage(msg -> {
    String messageType = msg.kind();
    String xmlContent = msg.xmlMessage();
    // Check validation result (if enabled)
    if (msg.validationException() != null) {
        System.err.println("Validation error: " + msg.validationException());
    } else if (msg.validation() != null) {
        System.out.println("Valid: " + msg.validation().isValid());
    }
    // Decoded message (if decoding enabled)
    // MessageType decodedMsg = msg.decodedMsg();

});
```

Message types include: `START_SCENARIO`, `PAUSE_SCENARIO`, `RESUME_SCENARIO`, `RESET_SCENARIO`, `SHARE_SCENARIO`, `SUBMIT_INITIALIZATION`, `C2SIM_INITIALIZATION`, and domain-specific orders/reports.

## State Machine Triggers

Trigger state transitions by sending state machine commands:

```java
import org.c2sim.statemachine.Trigger;
client.sendTrigger(Trigger.SUBMIT_INITIALIZATION);  // Move to INITIALIZING
client.sendTrigger(Trigger.SHARE_SCENARIO);         // Move to INITIALIZED
client.sendTrigger(Trigger.START_SCENARIO);         // Move to RUNNING
client.sendTrigger(Trigger.PAUSE_SCENARIO);         // Pause the simulation
client.sendTrigger(Trigger.RESUME_SCENARIO);        // Resume from pause
```

## Listener Pattern

Implement `C2SimClientListener` for lifecycle callbacks:

```java
public class MyClientListener implements C2SimClient.C2SimClientListener {
    @Override
    public void onJoined(C2SimClient client, DynamicSessionInfo info) {
        System.out.println("Joined session: " + info.getSessionName());
    }

    @Override
    public void onResigned(C2SimClient client) {
        System.out.println("Resigned from session");
    }

    @Override
    public void onStreamConnected(C2SimClient client) {
        System.out.println("WebSocket stream connected");
    }

    @Override
    public void onStreamDisconnected(C2SimClient client, int code, String reason) {
        System.out.println("Stream disconnected: " + code + " - " + reason);
    }

    @Override
    public void onStateChanged(C2SimClient client, StateType oldState, StateType newState) {
        System.out.println("State: " + oldState + " -> " + newState);
    }

    @Override
    public void onC2SIMInitialization(C2SimClient client, C2SIMInitializationBodyType init) {
        System.out.println("Received initialization");
    }
}
// Attach listener during build or later
client.setC2simClientListener(new MyClientListener());
```

## OIDC Authentication

For secure connections, configure OIDC:

```java
import org.c2sim.client.security.OidcCredentialFlow;

import org.c2sim.client.security.OidcCredentialFlowConfig;

OidcCredentialFlowConfig config = new OidcCredentialFlowConfig(
    URI.create("http://keycloak:8080/realms/c2sim/.well-known/openid-configuration"),
    "my-client-id",
    "my-client-secret"
);

OidcTokenProvider tokenProvider = new OidcCredentialFlow(config);

C2SimClient client = new C2SimClient.Builder()
    .url("http://localhost:7777/api")
    .systemName("MY_SYSTEM")
    .oidcProvider(tokenProvider)
    .build();
```

The token provider automatically:

- Obtains JWT tokens from the OIDC provider

- Refreshes tokens when expired

- Injects tokens into request headers via `AuthInterceptor`

## Message Validation and Processing

### Client-Side XSD Validation

Validate messages against the C2SIM schema:

```java
C2SimClient client = new C2SimClient.Builder()
    .url("http://localhost:7777/api")
    .systemName("MY_SYSTEM")
    .enableSendMessageValidation()       // Validate before sending
    .enableReceivedMessageValidation()   // Validate after receiving
    .build();
// Publishing with validation
try {
   client.publishC2SimDocument(xmlString);
} catch (ValidationException e) {
    e.getValidationResult().getValidationErrors().forEach(System.out::println);
}
```

### Message Decoding

Enable automatic XML-to-Java conversion:

```java
C2SimClient client = new C2SimClient.Builder()
    .url("http://localhost:7777/api")
    .systemName("MY_SYSTEM")
    .enableReceivedMessageDecode()
    .build();
 // Messages are decoded to MessageType POJO
client.onReceivedMessage(msg -> {
    if (msg.decodedMsg() != null) {
        MessageType decoded = msg.decodedMsg();
        // Work with typed objects
    }

});
```

## Session Creation

Automatically create a shared session if it doesn't exist:

```java
C2SimClient client = new C2SimClient.Builder()
   .url("http://localhost:7777/api")
   .systemName("MY_SYSTEM")
   .sharedSessionName("my-custom-session")
   .build();

// Provide session metadata when creating
client.whenCreatingSharedSession(() -> new RequestCreateSession()
    .data(new SessionInfo()
       .displayName("My Simulation")
       .description("Test scenario")
       .c2simSchemaVersion("1.0.2")));
client.connect();  // Creates session if needed
```

## Error Handling

The client throws checked exceptions for API failures:

```java
import org.c2sim.client.exceptions.C2ClientException;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.invoker.ApiException;
import org.c2sim.lox.exceptions.ValidationException;

try {
   client.connect();
} catch (C2SimRestException e) {
    System.err.println("REST API error: " + e.getMessage());
} catch (C2ClientException e) {
    System.err.println("Client error: " + e.getMessage());
    System.err.println("Error code: " + e.getErrorCode());
} catch (ApiException e) {
    System.err.println("API invocation failed");
}

try {
    client.publishC2SimDocument(xml);

} catch (ValidationException e) {
    System.err.println("XML validation failed");
    e.getValidationResult().getValidationErrors().forEach(System.err::println);
}
```

## Async Connection

For non-blocking connection attempts with automatic retry:

```java
// Attempt connection asynchronously with exponential backoff

client.connectAsync();
```

The client will retry every 5 seconds if the connection fails.

## Late-Join Scenarios

When a client joins a session already in progress, it automatically receives the current scenario initialization:

```java
client.setC2simClientListener(new C2SimClient.C2SimClientListener() {

    @Override
    public void onC2SIMInitialization(C2SimClient client, C2SIMInitializationBodyType init) {
        // Late-joined clients automatically receive init
        System.out.println("Scenario initialized: " + init);
    }
});



client.connect();  // Will fetch initialization if joining in-progress session
```

## Complete Example

```java
import org.c2sim.client.C2SimClient;
import org.c2sim.client.security.OidcCredentialFlow;
import org.c2sim.client.security.OidcCredentialFlowConfig;
import org.c2sim.statemachine.Trigger;
import java.net.URI;

public class C2SimExample {
    public static void main(String[] args) throws Exception {
        // Setup OIDC
        OidcTokenProvider tokens = new OidcCredentialFlow(
            new OidcCredentialFlowConfig(
               URI.create("http://localhost:8080/realms/c2sim/.well-known/openid-configuration"),
                "client",
                "secret"
            )
        );

        // Create client
        C2SimClient client = new C2SimClient.Builder()
            .url("http://localhost:7777/api")
            .systemName("COMMAND_CENTER")
            .clientIdDisplayName("CC-Client-1")
            .oidcProvider(tokens)
            .enableSendMessageValidation()
            .enableReceivedMessageValidation()
            .listener(new C2SimClient.C2SimClientListener() {
                @Override
                public void onStateChanged(C2SimClient c, StateType old, StateType neu) {
                    System.out.println(">> State: " + old + " -> " + neu);
                }
            })
            .build();

        // Register message handler
        client.onReceivedMessage(msg -> {
            System.out.println("Received: " + msg.kind());
        });

        // Connect to shared session
        client.connect();
        System.out.println("Connected to session");

        // Trigger state transitions
        client.sendTrigger(Trigger.SUBMIT_INITIALIZATION);
        client.sendTrigger(Trigger.SHARE_SCENARIO);
        client.sendTrigger(Trigger.START_SCENARIO);

        // Publish messages
        String myXml = "<C2SIM>...</C2SIM>";
        client.publishC2SimDocument(myXml);

        // Keep running
        Thread.currentThread().join();

    }

}
```

## Threading Model

- **WebSocket streams run in background threads** managed by `OkHttpWebSocketManager`

- **Message processing runs in a dedicated thread pool** (non-blocking message queue)

- **Client methods are thread-safe** - can be called from multiple threads

- **Listeners are invoked on background threads** - avoid blocking operations

- **Virtual threads supported** (Java 21+) for concurrent client simulations

## Performance Considerations

- **Message Validation**: XSD validation adds latency; consider disabling in production if validation is done server-side

- **Message Decoding**: Decoding XML to POJOs adds CPU overhead; enable only when needed

- **Connection Pooling**: OkHttpClient reuses connections - single client instance per system recommended

- **Rate Limiting**: Respect server rate limits when publishing messages rapidly

## Troubleshooting

### Connection Fails

```java
// Check event logs and enable debug logging
// src/main/resources/logback.xml:
// <logger name="org.c2sim.client" level="DEBUG"/>
// Try async connection for automatic retry
client.connectAsync();
```

### Messages Not Received

```java
// Verify WebSocket stream is connected
if (!client.hasStreamToSharedSession()) {
    System.err.println("Stream not connected");
}
// Check listener is registered
client.onReceivedMessage(msg -> {
    System.out.println("Got: " + msg.kind());
});
```

### Validation Failures

```java
try {

    client.publishC2SimDocument(xml);

} catch (ValidationException e) {
   // Print detailed validation errors
    e.getValidationResult().getValidationErrors()
       .forEach(error -> System.err.println(error.getMessage()));

}
```

## 
