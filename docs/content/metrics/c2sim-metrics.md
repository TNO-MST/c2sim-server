# C2SIM Metrics

The C2SIM server exposes a `/metrics` endpoint that publishes runtime metrics using the **[OpenTelemetry](https://opentelemetry.io/)** standard.

## Docker Test Environment

To support local development and metrics validation, the development Docker environment includes a complete observability stack.

Included Services

| System     | Description                                                                    |
| ---------- | ------------------------------------------------------------------------------ |
| Prometheus | Scrapes the `/metrics` endpoint and stores time-series data                    |
| Grafana    | Open-source visualization platform for exploring and displaying metric history |


This setup allows:

* Verify that metrics are correctly exposed by the C2SIM server

* Inspect real-time metric values

* Analyze historical trends

* Build dashboards for performance monitoring

Prometheus periodically collects metrics from the C2SIM `/metrics` endpoint, while Grafana provides a user-friendly web interface for querying and visualizing the collected data.

⚠ *Development Use Only*

This Docker setup is intended **for development and testing purposes only**.


