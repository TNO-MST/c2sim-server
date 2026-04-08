# OpenID Connect (OIDC) Integration

This section describes how the C2SIM server integrates with OpenID Connect (OIDC) for authentication and authorization. 

OpenID Connect (OIDC) is an identity layer built on top of OAuth 2.0. While OAuth 2.0 is designed for authorization (granting access to resources), OpenID Connect adds authentication.

Within this project the [keycloak](https://www.keycloak.org/) implementation is used as Identity Provider (IDP).

![](images/SecurityFlow.png)

## Terminology

Authentication (Identity) - The process of verifying *who* is making the REST API call.

Authorization (Permission) - The process of verifying *what* the authenticated service is allowed to do.

## OIDC Client Credentials Flow

The **Client Credentials Flow** is used for **machine-to-machine (M2M) authentication**, where no human user interaction is involved.

This flow is used when:

- The client is a backend service  
- No browser login or user interaction is required  
- The service needs an access token to call protected APIs  
- Authentication is performed using a client secret  

### Recommended Client Configuration

Within `keycloak` a `client` should use the following settings (for client credential flow):

| Setting                  | Value          |
| ------------------------ | -------------- |
| Protocol                 | OpenID Connect |
| Client Type              | Confidential   |
| Client Authentication    | Client secret  |
| Service Accounts Enabled | Yes            |
| Public Client            | No             |

The difference between :

* `client` account: used for use without human interaction (automatic login)

* `user` account: used for use with human interaction (manual login)

## Obtaining an Access Token Manually

For testing purposes, the `Client Credentials Flow` can be tested using the  `curl` application, against the token endpoint (for example with Keycloak as IDP):

```bash
curl -X POST "https://your-keycloak.com/realms/YOUR_REALM/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=YOUR_CLIENT_ID" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "scope=c2sim"
```

To test the `Client Credential Flow` with the `client id` and `client secret`:

```bash
curl -X POST "http://localhost:8080/realms/c2sim/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=c2sim-client" \
  -d "client_secret=RQOwgVBNzS3frhpLqoFatVJ2xTyPQBDV" \
  -d "scope=c2sim"
```

The response will contain a JWT access token. A JWT decoder, like [JWT.IO](https://www.jwt.io/), can display the content of the `access token` (JSON notation).

---

## Client Secret

The client secret is a shared secret used by confidential clients to authenticate with the identity provider.

!!! warning

    **Security Notice**
    
    - The client secret must **never** be exposed in frontend or browser-based applications.
    - It must only be stored securely on backend systems.

---

## OpenID Configuration Discovery

The C2SIM client can use the [OpenID Connect Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html) instead of directly configuring the OIDC `token endpoint`.

For Keycloak, the discovery endpoint has the following format:

```
http://<hostname>:<port>/realms/<realm>/.well-known/openid-configuration
```

Example:

```
http://localhost:8080/realms/c2sim/.well-known/openid-configuration
```

Some libraries will automatically add `/.well-known/openid-configuration` to the URL.

The meta data response includes information like:

- `issuer`

- `authorization_endpoint`

- `token_endpoint`

- `userinfo_endpoint`

- `jwks_uri`

- `registration_endpoint`

- `scopes_supported`

- `response_types_supported`

- `grant_types_supported`

The `token_endpoint` contains the url to the `token endpoint`.

```json
"token_endpoint": "http://localhost:8080/realms/c2sim/protocol/openid-connect/token"
```

## C2SIM-Specific claims

The `c2sim` scope includes domain-specific claims required by the C2SIM server.

| Claim                      | Description                   |
| -------------------------- | ----------------------------- |
| communicativeActTypeCode   | Type of communicative act     |
| fromSendingSystem          | Originating system            |
| replyToSystem              | Reply destination system      |
| securityClassificationCode | Security classification level |
| toReceivingSystem          | Target receiving system       |
| messageType                | C2SIM message type            |
| systemMessageType          | System-level message type     |

## C2SIM Scope

The scope `c2sim` contains all claims related to C2SIM (see C2SIM specific claims)

---

## JWT Access Token

The access token in the RESTful API call is a bearer token, also known as JSON Web Token ([JWT](https://en.wikipedia.org/wiki/JSON_Web_Token)).

A JWT is Base64 URL-encoded and consists of three parts separated by a dot (`.`):

```
HEADER.PAYLOAD.SIGNATURE
```

### JWT Structure

| Part      | Description                             |
| --------- | --------------------------------------- |
| Header    | Metadata about the signing algorithm    |
| Payload   | Claims (token data)                     |
| Signature | Digital signature used for verification |

### Example decode payload

```json
{
  "exp": 1770833986,
  "iat": 1770832186,
  "jti": "69783bc1-1f78-4c80-a91e-e9ba0c2f7508",
  "iss": "http://localhost:8080/realms/c2sim",
  "sub": "d14adf87-b882-4e99-aeaf-b2e6d0ae8a10",
  "typ": "Bearer",
  "azp": "c2sim-client",
  "scope": "c2sim",
  "securityClassificationCode": "UNCLASSIFIED",
  "clientHost": "172.19.0.1",
  "messageType": "ORDER",
  "clientAddress": "172.19.0.1",
  "fromSendingSystem": "C2SIM",
  "client_id": "c2sim-client"
}
```

Important generic `claims` (not C2SIM specific)

|     |                                                                                                               |
| --- | ------------------------------------------------------------------------------------------------------------- |
| typ | The `Bearer` means the `access token` can be used for REST header  `Authorization: Bearer`                    |
| exp | Expiration time of the `access token` in epoch notation. After this timestamp the token is not valid anymore. |
| aud | Who the token is intended for (resource/server). The C2SIM server can demand the `c2sim` value here.          |

## Using the JWT in REST Calls

Every authenticated REST request must include the `Authorization` header:

```
Authorization: Bearer <JWT_ACCESS_TOKEN>
```

Example:

```http
GET /c2sim/api/messages HTTP/1.1
Host: your-server
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

Without this header, the C2SIM server will reject the request.

## Time server (NTP)

If the IDP provider (Keycloak) or any IDP consumer has an incorrect system time, a valid `access token` may be rejected as expired (“token expired”).

To prevent this issue, all participating systems should synchronize their clocks using an `NTP server`, ensuring consistent and accurate time across the environment.

## Token expiration

Each `access token` issued by the IDP includes its expiration time, which is defined by the IDP. The client is responsible for renewing the `access token` before it expires.

For `CWIX2025`, the expiration time was configured to last for the entire duration of the exercise, meaning the token only needed to be obtained once. In commercial systems, however, expiration times are typically much shorter—usually ranging from 1 to 30 minutes.

## Secure connection (TLS)

When using OIDC, **all** RESTful calls that include an `Authorization` header **must** be sent over TLS (HTTPS).

If an unencrypted HTTP connection is used, the `access token` can be intercepted using a network monitoring tool (e.g., Wireshark). Because a bearer token grants access to whoever possesses it, an attacker who captures the token could reuse it until it expires.

For convenience, some environments use unencrypted HTTP. However, this is insecure. TLS requires valid, trusted certificates, and self-signed certificates are generally not acceptable in production environments because they cannot be reliably validated by clients.

## Keycloak

The `C2SIM claims` are stored as `user attributes` (the values of the claim). A `client` in keycloak is assigned to a service-user-account.  

!!! warning

    In Keycloak version 21 and earlier, admins could freely edit user attributes directly in the Admin UI. The later release removed this feature. Attributes are now configured in `User Profile configuration`. The `C2SIM claims` are not default part of this.  The management of C2SIM Claims are done trough an external tool, and not the `Keyclaok Admin UI`.
