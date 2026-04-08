import KcAdminClient from "@keycloak/keycloak-admin-client";
import ClientRepresentation from "@keycloak/keycloak-admin-client/lib/defs/clientRepresentation";
import type ClientScopeRepresentation from "@keycloak/keycloak-admin-client/lib/defs/clientScopeRepresentation.js";
import jwt from "jsonwebtoken";
import { writeFile } from "fs/promises";
import { mkdirSync, existsSync } from "fs";
import { dirname } from "path";
import { CLIENTS } from "./clients.config.js";
import { C2SIM_CLAIM_KEYS, ClientConfig, HLA_CLAIM_KEYS } from "./types.js";

// --------------------------------------------------
// ENV
// --------------------------------------------------
const KEYCLOAK_URL = process.env.KEYCLOAK_URL ?? "http://localhost:8080";
const ADMIN_USER = process.env.KEYCLOAK_ADMIN ?? "admin";
const ADMIN_PASS = process.env.KEYCLOAK_ADMIN_PASSWORD ?? "admin";
const REALM = process.env.REALM ?? "c2sim";
const C2SIM_SCOPE_NAME = "c2sim";
const HLA_SCOPE_NAME = "hla";
const INFO_PATH = "/app/credentials/c2sim_client_info.json";

interface JsonClientInfo {
  clientId: string;
  clientSecret: string;
  c2simClaims: Record<string, string>;
}

async function waitForKeycloakReady(
  url: string,
  maxRetries = 30,
  delayMs = 2000,
): Promise<void> {
  await new Promise((resolve) => setTimeout(resolve, 5000));

  let attempt = 0;

  while (true) {
    try {
      const res = await fetch(`${url}/health/ready`);
      if (res.ok) {
        console.log("Keycloak is ready!");
        return; // success → stop waiting
      }
    } catch (_) {
      // ignore errors — we expect them until Keycloak starts
    }

    attempt++;
    if (attempt >= maxRetries) {
      throw new Error(
        `Keycloak did not become ready after ${maxRetries} attempts`,
      );
    }

    console.log(
      `Waiting for Keycloak to be ready... attempt ${attempt}/${maxRetries}`,
    );

    await new Promise((resolve) => setTimeout(resolve, delayMs));
  }
}

// --------------------------------------------------
const kc = new KcAdminClient({
  baseUrl: KEYCLOAK_URL,
  realmName: "master",
});

// --------------------------------------------------
const normalizeAttrs = (
  attrs: Record<string, string>,
): Record<string, string[]> =>
  Object.fromEntries(Object.entries(attrs).map(([k, v]) => [k, [v]]));

// --------------------------------------------------
// LOGIN
// --------------------------------------------------
async function login(): Promise<void> {
  await kc.auth({
    username: ADMIN_USER,
    password: ADMIN_PASS,
    grantType: "password",
    clientId: "admin-cli",
  });

  // Ensure target realm exists
  await ensureRealmExists(REALM);

  kc.setConfig({ realmName: REALM });
  console.log("Logged into Keycloak");
}

// --------------------------------------------------
// CLIENT SCOPES
// --------------------------------------------------
async function getOrCreateClientScope(
  name: string,
): Promise<ClientScopeRepresentation & { id: string }> {
  let scope = await kc.clientScopes.findOneByName({ name });

  if (!scope) {
    console.log(`Creating client scope '${name}'`);
    await kc.clientScopes.create({
      name,
      protocol: "openid-connect",
      description: "Scope for " + name + " claims (C2SIM-SERVER)",
    });
    scope = await kc.clientScopes.findOneByName({ name });
  }

  if (!scope?.id) {
    throw new Error(`Client scope '${name}' missing id`);
  }

  return scope as ClientScopeRepresentation & { id: string };
}

async function applyGlobalClientScope(
  scopeName: string,
  attributes: readonly string[],
): Promise<void> {
  console.log(`Processing scope '${scopeName}'`);
  const scope = await getOrCreateClientScope(scopeName);

  const mappers = await kc.clientScopes.listProtocolMappers({ id: scope.id });

  for (const attr of attributes) {
    if (!mappers.some((m) => m.name === attr)) {
      await kc.clientScopes.addProtocolMapper(
        { id: scope.id },
        {
          name: attr,
          protocol: "openid-connect",
          protocolMapper: "oidc-usermodel-attribute-mapper",
          config: {
            "user.attribute": attr,
            "claim.name": attr,
            "jsonType.label": "String",
            "access.token.claim": "true",
          },
        },
      );
    }
  }
  
  // Add claim: force-audience
  const existing = mappers.find(m => m.name === "force-audience");

  if (!existing) {
	  console.log('Add force audience');
  await kc.clientScopes.addProtocolMapper(
    { id: scope.id },
    {
      name: "force-audience",
      protocol: "openid-connect",
      protocolMapper: "oidc-audience-mapper",
      config: {
        "included.client.audience": "c2sim",
        "access.token.claim": "true",
        "id.token.claim": "false"
      }
    }
  );
}
}

// --------------------------------------------------
// CLIENT CREATE / UPDATE (CONFIDENTIAL CLIENT)
// --------------------------------------------------
async function getOrCreateClient(name: string): Promise<ClientRepresentation> {
  // Try to find by clientId
  const found = await kc.clients.find({ clientId: name });

  if (found.length > 0 && found[0].id) {
    // Re-fetch full details using internal id
    const existing = await kc.clients.findOne({ id: found[0].id });
    return existing ?? found[0];
  }

  // Create new client
  console.log(`Creating client '${name}'`);
  const created = await kc.clients.create({
    clientId: name,
    protocol: "openid-connect",
    enabled: true,
    publicClient: false,
    serviceAccountsEnabled: true,
    clientAuthenticatorType: "client-secret",

  });

  // Since Keycloak 21+ it is not possible to set client secret, only random generate new one

  // The create endpoint returns *only the internal id* and not the full object.
  // So we re-fetch the full representation now:
  if (!created.id) {
    throw new Error(`Failed to create client '${name}' — no id returned`);
  }

  const fullClient = await kc.clients.findOne({ id: created.id });

  if (!fullClient) {
    throw new Error(`Client created but failed to re-fetch: '${name}'`);
  }
  
  if (!fullClient.id) {
  throw new Error("Client ID is missing");
}
  
  // Add c2sim as default scope
  const scopes = await kc.clientScopes.find();
  const scope = scopes.find((s) => s.name === "c2sim");

  if (!scope || !scope.id) {
    throw new Error("Client scope 'c2sim' not found");
  }

  const existingScopes = await kc.clients.listDefaultClientScopes({
    id: fullClient.id,
  });

  const alreadyAssigned = existingScopes.some((s) => s.id === scope.id);

  if (!alreadyAssigned) {
    await kc.clients.addDefaultClientScope({
      id: fullClient.id,
      clientScopeId: scope.id,
    });
  }


  return fullClient;
}
// --------------------------------------------------
// SERVICE ACCOUNT ATTRIBUTES
// --------------------------------------------------
async function applyClientAttributes(
  clientId: string,
  cfg: ClientConfig,
): Promise<void> {
  const sa = await kc.clients.getServiceAccountUser({ id: clientId });
  if (!sa?.id) throw new Error("Service account missing");

  const merged = {
    ...(cfg.useHlaClaims ? cfg.hlaClaims : {}),
    ...(cfg.useC2simClaims ? cfg.c2simClaims : {}),
  };

  await kc.users.update({ id: sa.id }, { attributes: normalizeAttrs(merged) });
}

// --------------------------------------------------
// CLIENT SECRET + TOKEN
// --------------------------------------------------
async function getClientSecret(clientId: string): Promise<string> {
  const secret = await kc.clients.getClientSecret({ id: clientId });
  if (!secret.value) {
    throw new Error(`Client secret missing for ${clientId}`);
  }
  return secret.value;
}

async function getClientCredentialsToken(
  clientId: string,
  clientSecret: string,
): Promise<string | undefined> {
  const url =
    `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`;

  const body = new URLSearchParams({
    grant_type: "client_credentials",
    client_id: clientId,
    client_secret: clientSecret,
    scope: C2SIM_SCOPE_NAME,
  });

  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body,
  });

  if (!res.ok) return undefined;

  const json = await res.json();
  return json.access_token;
}

// --------------------------------------------------
// TOKEN INFO
// --------------------------------------------------
async function showAccessTokenInfo(
  client: ClientRepresentation,
): Promise<void> {
  if (!client.id) {
    console.error("Client missing id ");
    return;
  }
  if (!client.clientId) {
    console.error("Client missing  clientId");
    return;
  }
  const secret = await getClientSecret(client.id);

  console.log("--------------------------------------------------");
  console.log(`Client: ${client.clientId}`);
  console.log(`Client secret: ${secret}`);

  const token = await getClientCredentialsToken(client.clientId, secret);

  if (!token) {
    console.error(`Failed to get token for ${client.clientId}`);
    return;
  }

  const decoded = jwt.decode(token, { complete: true });

  let expires = "unknown";
  if (
    decoded?.payload &&
    typeof decoded.payload === "object" &&
    "exp" in decoded.payload &&
    typeof decoded.payload.exp === "number"
  ) {
    expires = new Date(decoded.payload.exp * 1000).toISOString();
  }

  console.log(`Expires: ${expires}`);
  console.log("JWT payload:");
  console.log(JSON.stringify(decoded?.payload, null, 2));
}

async function ensureRealmExists(realmName: string): Promise<void> {
  const realms = await kc.realms.find();
  const exists = realms.some((r) => r.realm === realmName);

  if (exists) {
    console.log(`Realm '${realmName}' already exists`);
    return;
  }

  console.log(`Creating realm '${realmName}'`);
  await kc.realms.create({
    realm: realmName,
    enabled: true,
  });
}

async function getClientInfoFromKeycloak(
  client: ClientRepresentation,
): Promise<JsonClientInfo> {
  if (!client.id || !client.clientId) {
    throw new Error("Client missing id");
  }

  const secret = await kc.clients.getClientSecret({ id: client.id });

  const serviceAccount = await kc.clients.getServiceAccountUser({
    id: client.id,
  });

  const attributes = serviceAccount?.attributes ?? {};

  const c2simClaims: Record<string, string> = {};

  for (const key of C2SIM_CLAIM_KEYS) {
    const value = attributes[key];
    if (Array.isArray(value) && value.length > 0) {
      c2simClaims[key] = value[0];
    }
  }

  // ✅ Return JSON object
  return {
    clientId: client.clientId,
    clientSecret: secret.value || "",
    c2simClaims,
  };
}

async function writeClientInfoListToFile(
  filePath: string,
  clients: JsonClientInfo[],
): Promise<void> {
  const folder = dirname(filePath);

  // Ensure directory exists
  if (!existsSync(folder)) {
    mkdirSync(folder, { recursive: true });
  }

  const json = JSON.stringify(clients, null, 2);

  writeFile(filePath, json, "utf-8")
    .then(() => console.log("done"))
    .catch(console.error);

  console.log(`Wrote ${clients.length} clients to ${filePath}`);
}

// --------------------------------------------------
// MAIN
// --------------------------------------------------
async function main(): Promise<void> {
  console.log(
    `KEYCLOAK_URL: ${KEYCLOAK_URL}  REALM: ${REALM}  ADMIN_USER: ${ADMIN_USER}  `,
  );
  let clientInfoList: JsonClientInfo[] = [];

  await waitForKeycloakReady(KEYCLOAK_URL);
  await login();

  await applyGlobalClientScope(C2SIM_SCOPE_NAME, C2SIM_CLAIM_KEYS);

  await applyGlobalClientScope(HLA_SCOPE_NAME, HLA_CLAIM_KEYS);

  for (const cfg of CLIENTS) {
    console.log("====================================");
    console.log(`Processing client '${cfg.name}'`);

    const client = await getOrCreateClient(cfg.name);
    if (!client.id) throw new Error("Client missing ID");

    await kc.clients.update(
      { id: client.id },
      {
        ...client,
        attributes: {
          ...(client.attributes ?? {}),
          "access.token.lifespan": cfg.expireTimeInSeconds,
        },
      },
    );

    await applyClientAttributes(client.id, cfg);

    // 🔹 SHOW TOKEN INFO (CONFIRMS SECRET WORKS)
    await showAccessTokenInfo(client);
    const clientInfo = await getClientInfoFromKeycloak(client);
    clientInfoList.push(clientInfo);
  }

  writeClientInfoListToFile(INFO_PATH, clientInfoList);
  console.log("Done");
}

main().catch((err) => {
  console.error("Fatal error:", err);
  process.exit(1);
});
