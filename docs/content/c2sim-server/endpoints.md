# C2SIM server endpoints

The `C2SIM-Server` exposes the following endpoints

| Endpoint         | Description                                                                                             | Condition                       |
| ---------------- | ------------------------------------------------------------------------------------------------------- | ------------------------------- |
| /api/*           | All `C2SIM OpenAPI RESTFul operation`. See this [link](https://tno-mst.github.io/c2sim-server/openapi/) |                                 |
| /                | Web page with shortcuts to important endpoint of C2SIM sever                                            |                                 |
| /openapi-ui.html | OpenAPI web interface (viewing and testing)                                                             |                                 |
| /status          | Active `shared sessions` and connected `C2SIM clients` (debug only)                                     |                                 |
| /configuration   | Shows all ENV options with current value in Mark Down notation                                          | ENV `C2SIM_EXPOSE_CFG_ENDPOINT` |
| /metrics         | Metrics endpoint                                                                                        |                                 |
| /health          | Health endpoint. Returns status code `200` when healthy.                                                |                                 |


