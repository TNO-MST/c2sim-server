# Audit log

Startup C2SIM server, shutdown C2SIM server, Joining and Resigning from shared session are logged to file (success and failure). The data is logged in `Elastic Common Schema (ECS)` format and can be directly be imported into elastic Search.



## Audit properties

| Property                | Description                                                                  |
| ----------------------- | ---------------------------------------------------------------------------- |
| `c2sim.sharedSession`   | Shared Session identifier                                                    |
| `c2sim.systemName`      | Registered system name                                                       |
| `c2sim.action`          | Audit action performed [JOIN, RESIGN, AUTH_FAILURE, START_AUDIT, STOP_AUDIT] |
| `c2sim.status`          | Outcome of action (success / failure)                                        |
| `c2sim.clientIP`        | Client IP address                                                            |
| `c2sim.azp`             | Authorized party claim from JWT                                              |
| `c2sim.jwt`             | JWT token                                                                    |
| `c2sim.clientId`        | C2SIM Client identifier                                                      |
| `c2sim.trackingId`      | Request tracking identifier (C2SIM server)                                   |
| `c2sim.errorMsg`        | Error description (human readable )                                          |
| `c2sim.authFailureCode` | Authorization failure code                                                   |

---

## Status Values

| Value     | Meaning                          |
| --------- | -------------------------------- |
| `SUCCESS` | Operation completed successfully |
| `FAILURE` | Operation failed                 |

### Successful Shared Session Join

Fields populated:

```
c2sim.sharedSession
c2sim.systemName
c2sim.clientIP
c2sim.azp
c2sim.jwt
c2sim.action = JOIN
c2sim.status = SUCCESS
```

Message:

```
System '<systemName>' successfully joined Shared Session '<sharedSession>'
```

---

### Successful Shared Session Resignation

Fields populated:

```
c2sim.sharedSession
c2sim.systemName
c2sim.clientIP
c2sim.azp
c2sim.action = RESIGN
c2sim.status = SUCCESS
```

Message:

```
System '<systemName>' successfully resigned from Shared Session '<sharedSession>'
```

---

### Authorization Failure

Fields populated:

```
c2sim.sharedSession
c2sim.clientId
c2sim.trackingId
c2sim.errorMsg
c2sim.clientIP
c2sim.action = AUTH_FAILURE
c2sim.authFailureCode
c2sim.status = FAILURE
```

Message:

```
C2SIM client '<clientId>' with IP Address '<ip>'auth failure (error code '<errorCode>')
```

---

### Server Startup

Fields populated:

```
c2sim.action = START_AUDITc2sim.status = SUCCESS
```

Message:

```
C2SIM Server started, audit log started.
```

---

### Server Shutdown

Fields populated:

```
c2sim.action = STOP_AUDITc2sim.status = SUCCESS
```

Message:

```
C2SIM Server shutdown
```

## 
