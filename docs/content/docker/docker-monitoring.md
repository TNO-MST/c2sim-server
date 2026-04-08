## Monitoring with docker (metrics)

## Health Checks

To check the state of the `C2SIM-server` probe endpoints can be used.

| Name                          | Description                                         |
| ----------------------------- | --------------------------------------------------- |
| Liveness Probe (Health probe) | Checks if the container is still alive / not stuck  |
| Readiness Probe               | Checks if the container is ready to receive traffic |

### Liveness Probe

```bash
curl http://localhost:9999/status
```

**Expected**: HTTP 200 with `{"status":"UP","timestamp":<<epoch>>}`

### Readiness Probe

```bash
curl http://localhost:9999/api/c2sim/session/list
```

**Expected**: HTTP 200 with JSON array

## Metrics

See [documentation](./../metrics/c2sim-metrics.md)
