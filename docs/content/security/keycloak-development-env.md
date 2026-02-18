# Keycloak Development Server

To test the **C2SIM client** in combination with an Identity Provider (IdP), a Keycloak server is included in the Docker Compose setup.

On startup:

* Realm `c2sim` is created
- Multiple **client accounts** are created automatically

- Each client is configured with different **C2SIM claims**

**Note** On restart, the keycloak database is cleared (not persistent)

## Test Data Initialization

Each time the Keycloak server restarts:

1. An initialization script runs automatically (node.js application)

2. The realm **`c2sim`** is created

3. The **`c2sim` scope** (containing claims) is added

4. Clients defined in:

`docker/init/kc-admin-ts/src/clients.config.ts`

are created and configured.

You can modify this file to adjust the required **C2SIM claims** per client.

---

## Dynamic Client Credentials

The file:

`docker/credentials/c2sim_client_info.json`

contains the generated client configuration. The `clientId` and `clientSecret` can be configured in the C2SIM client.

⚠ Important:

- The **client secret is randomly generated**

- It changes on every restart

- This is intentional for development security

Example of `c2sim_client_info.json` (depends on content of `clients.config.ts`)

```
[
  {
    "clientId": "pep-client",
    "clientSecret": "siJaOkqiwTlXPD7xsL4eWHD9blVhO4fI",
    "c2simClaims": {
      "fromSendingSystem": "C2SIM",
      "messageType": "ORDER",
      "securityClassificationCode": "UNCLASSIFIED"
    }
  },
  {
    "clientId": "c2sim-client",
    "clientSecret": "RQOwgVBNzS3frhpLqoFatVJ2xTyPQBDV",
    "c2simClaims": {
      "fromSendingSystem": "C2SIM",
      "messageType": "ORDER",
      "securityClassificationCode": "UNCLASSIFIED"
    }
  }
]
```

## Import Realm Data (Alternative Approach)

Instead of using the TypeScript initialization script, you can import a realm file directly using the built-in Keycloak import feature.

### Option: `--import-realm`

Place your realm file at:

`/opt/keycloak/data/import/realm.json`

Start Keycloak with:

`start-dev --import-realm`

In Docker:

`quay.io/keycloak/keycloak:latest start-dev --import-realm`

This imports the realm configuration during startup.

⚠ Notes:

- The realm must not already exist

- Import runs only at startup

- Useful for static configurations

- Less flexible than dynamic initialization scripts

Example of client section in `realm.json`:

```
 "clients": [
    {
      "clientId": "c2sim-client",
      "enabled": true,
      "protocol": "openid-connect",
      "publicClient": false,
      "secret": "CHANGE_ME_IN_PRODUCTION",
      "standardFlowEnabled": true,
      "directAccessGrantsEnabled": true,
      "serviceAccountsEnabled": true,
      "redirectUris": [
        "http://localhost:8081/*"
      ],
      "webOrigins": [
        "+"
      ],
      "defaultClientScopes": [
        "web-origins",
        "roles",
        "profile",
        "email",
        "c2sim"
      ]
    }
  ]
```

---

## Initialization Script vs `--import-realm`

| TypeScript Initialization   | `--import-realm`        |
| --------------------------- | ----------------------- |
| Dynamic setup               | Static JSON import      |
| Dynamic secrets (generated) | Uses predefined secrets |
| Programmatic control        | Configuration-based     |
| Good for dev automation     | Good for simple setups  |

## Keycloak web interface


