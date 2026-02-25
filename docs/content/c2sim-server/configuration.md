# C2SIM configuration

## Environment setting

Most configuration settings are done trough `ENVIRONMENT VARIABLE`. Can be easily adjusted in a docker / Kubernetes deployment.  The file `org.c2sim.server.services.impl.DefaultConfigService` defines all config settings.

## Server configuration

The file `server-config.json` is loaded by the C2SIM-server on startup. It contains the predefined `default` shared session. If the file is not found, the default (hardcoded) settings will be used. This all to have multiple pre-created shared sessions.

## Configuration

This table is `auto generated`from C2SIM server endpoint `/configuration`.  This endpoint is available when ENV `C2SIM_EXPOSE_CFG_ENDPOINT` is set to`true`. For example `http://localhost:7777/configuration`.

| Environment                  | Description                                                                                       | Data Type  | Default    | Value      | Allowed Values                   |
| ---------------------------- | ------------------------------------------------------------------------------------------------- | ---------- | ---------- | ---------- | -------------------------------- |
| C2SIM_XSD_VALIDATION_ENABLED | Xsd validation for C2SIM XML enabled                                                              | Boolean    | true       | true       | -                                |
| C2SIM_MAX_MSG_SIZE_MB        | Max REST body size (MB) for C2SIM messages                                                        | Float      | 10.0       | 10.0       | -                                |
| C2SIM_EXTERNAL_HOSTNAME      | External hostname or ip address of C2SIM server (when empty ignore)                               | String     | 127.0.0.1  | 127.0.0.1  | -                                |
| C2SIM_EXTERNAL_PORT_NUMBER   | External port number of C2SIM server                                                              | Integer    | 7777       | 7777       | -                                |
| C2SIM_AUTH_MODE              | Bearer tokens (not required (disables all auth) / mandatory / mixed)                              | EAuthLevel | MIXED_AUTH | MIXED_AUTH | NO_AUTH, MIXED_AUTH, STRICT_AUTH |
| C2SIM_BEARER_PUBLIC_KEY      | When empty: public key fetched from Identity Provider. When set, this key is used (no IDP needed) | String     |            |            | -                                |
| C2SIM_IDENTITY_PROVIDER_URL  | The URL of the Identity Provider                                                                  | String     |            |            | -                                |
| C2SIM_EXPOSE_CFG_ENDPOINT    | Configuration endpoint /configuration is be exposed                                               | Boolean    | true       | true       | -                                |
