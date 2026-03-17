# JAVA C2SIM client

The module `c2sim-client` can be used to create a `C2SIM client`.

The client handles:

- **Session Management**: Join/resign from shared sessions with other systems

- **Message Publishing**: Send C2SIM XML documents with XSD validation

- **Message Receiving**: Stream C2SIM messages via WebSocket with optional validation/decoding

- **State Synchronization**: Track and respond to C2SIM server state changes

- **OIDC Authentication**: Secure connections with JWT tokens from OIDC providers

- **Error Handling**: Comprehensive exception handling and retry logic

See [package c2sim-client](c2sim-server/module-c2sim-client.md) documentation.



## Example application

The folder `<root>\examples\c2sim-simple-client-example` contains a simple Java console application.

The application:

- Joins the Shared Session on the C2SIM server (this also establishes the WebSocket connection). If the Shared Session does not exist, it will be created automatically.

- Brings the C2SIM server into the `RUNNING` state if required. If the server is in the `UNINITIALIZED` state, a `C2SIMInitializationBody` is sent to initialize it.

- Sends a C2SIM position message at a fixed interval.

- Displays any received C2SIM messages in the console.

- Includes support for `OIDC` authorization for future use (currently not active).

!!! note

    The application is intended solely as an example of how to use the library. The use of a static main method and global exception handling in this context does not reflect best practices.

## C2SIM Client CLI

The `C2SIM Client CLI` uses the `C2SIM client library` and demonstrates a more production-oriented use case. 
