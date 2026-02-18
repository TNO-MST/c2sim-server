## Monitoring with docker (metrics)

The docker composition can also be used to test the `C2SIM server metrics`. Prometheus scrapes the metric collection endpoints and `grafana` can be used to visualize the metrics.

### Prometheus Configuration

**File**: `docker/prometheus/prometheus.yml`

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: "c2sim-server"
    static_configs:
      - targets: ["c2sim-server:7777"]
    metrics_path: "/metrics"
```

**Metrics URL**: http://localhost:9090

**Example queries**:

```promql
# HTTP request rate
rate(http_server_requests_seconds_count[5m])

# JVM memory usage
jvm_memory_used_bytes{area="heap"}

# Thread count
jvm_threads_live_threads
```

### Grafana Dashboard

**File**: `docker/grafana/dashboards/c2sim-dashboard.json`

Pre-configured dashboard showing:

- HTTP request rate/duration
- JVM memory usage
- Thread count
- GC activity
- WebSocket connections

## Health Checks

### Liveness Probe

```bash
curl http://localhost:7777/api/c2sim/session/list
```

**Expected**: HTTP 200 with JSON array

### Readiness Probe

```bash
curl http://localhost:7777/metrics
```

**Expected**: HTTP 200 with Prometheus metrics
