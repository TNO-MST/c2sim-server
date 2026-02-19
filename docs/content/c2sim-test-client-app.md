# C2SIM example application

The **C2SIM Client App** is a test/demo application that demonstrates how to use the C2SIM Java client library to interact with the C2SIM server.

The application can be found in

```
server\c2sim-client-app
```

The application is used for:

- Stress testing (multiple `C2SIM clients` )

- Testing `authorization`

- Testing serialization and deserialization `C2SIM messages`

- Testing validation of `C2SIM messages`

- Testing `C2SIM orchestration` with multiple `C2SIM clients`

What demonstrates the application:

1. **OIDC Authentication**: 
   
   Authenticates with a Keycloak server using the OIDC client credentials flow to obtain JWT tokens with C2SIM-specific claims.

2. **Client Session Management**: 
   
   Creates one or more MonitorClient instances that each:
   
   - Connect to the C2SIM server at `http://localhost:7777/api`
   - Join a shared session named "default"
   - Represent distinct systems in the simulation (e.g., SYSTEM_0, SYSTEM_1, etc.)

3. **Server State Orchestration**:
   
   - Waits for all clients to connect and establish WebSocket streams
   - Brings the C2SIM server into the RUNNING state by sending initialization triggers (SUBMIT_INITIALIZATION, SHARE_SCENARIO, START_SCENARIO)
   - Monitors state changes across all connected clients

4. **Message Publishing**: 
   
   Each MonitorClient runs in its own virtual thread and periodically publishes C2SIM XML reports containing position/tracking updates every 5 seconds (once the server enters RUNNING state).
