package org.c2sim.cli.config;

/**
 * Persisted application configuration. Saved as config.json in the working directory.
 * All fields have sensible defaults so the app works out of the box.
 */
public class AppConfig {

    private boolean useOidc            = false;
    private boolean useIdentityProvider = true;
    private String  oidcIdpUrl = "http://localhost:8080/realms/c2sim";
    private String oidcStaticAuthToken = "";
    private String  oidcClientId       = "c2sim-client";
    private String  oidcClientSecret   = "";

    private String  serverUrl          = "http://localhost:9999/api";
    private String  systemName         = "NLD-DEMO";
    private String  clientDisplayName  = "NLD-DEMO-CLIENT";
    private String  sharedSessionName  = "default";
    private boolean writeReceivedMsgToDisk = true;

    // ── OIDC ──────────────────────────────────────────────────────────────

    public boolean isUseOidc()                        { return useOidc; }
    public void    setUseOidc(boolean v)              { useOidc = v; }

    public boolean isUseIdentityProvider()            { return useIdentityProvider; }
    public void    setUseIdentityProvider(boolean v)  { useIdentityProvider = v; }

    public String getOidcIdpUrl()                     { return oidcIdpUrl; }
    public void setOidcIdpUrl(String v)               { oidcIdpUrl = v; }

    public String getAuthFixedToken()                 { return oidcStaticAuthToken; }
    public void seAuthFixedToken(String v)            { oidcStaticAuthToken = v; }

    public String  getOidcClientId()                  { return oidcClientId; }
    public void    setOidcClientId(String v)          { oidcClientId = v; }

    public String  getOidcClientSecret()              { return oidcClientSecret; }
    public void    setOidcClientSecret(String v)      { oidcClientSecret = v; }



    // ── Server ────────────────────────────────────────────────────────────

    public String  getServerUrl()                     { return serverUrl; }
    public void    setServerUrl(String v)             { serverUrl = v; }

    // ── Client identity ───────────────────────────────────────────────────

    public String  getSystemName()                    { return systemName; }
    public void    setSystemName(String v)            { systemName = v; }

    public String  getClientDisplayName()             { return clientDisplayName; }
    public void    setClientDisplayName(String v)     { clientDisplayName = v; }

    // ── Session ───────────────────────────────────────────────────────────

    public String  getSharedSessionName()             { return sharedSessionName; }
    public void    setSharedSessionName(String v)     { sharedSessionName = v; }


    public boolean  getWriteReceivedMsgToDisk()          { return writeReceivedMsgToDisk; }
    public void setWriteReceivedMsgToDisk(boolean v) { writeReceivedMsgToDisk = v; }

    // ── Helper ────────────────────────────────────────────────────────────

    /**
     * Returns true when the minimum required fields are filled so the app can
     * offer a quick-connect without re-running the setup wizard.
     */
    public boolean isComplete() {
        return notBlank(serverUrl) && notBlank(systemName) && notBlank(clientDisplayName);
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    public String getConfigSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("C2SIM sever            : ").append(getServerUrl()).append("\n");
        sb.append("System name            : ").append(getSystemName()).append("\n");
        sb.append("Display name (logging) : ").append(getClientDisplayName()).append("\n");

        if (useOidc) {
            sb.append("Authorize              : enabled").append("\n");

            if (useIdentityProvider) {

                sb.append("Identity provider      : ").append(getOidcIdpUrl()).append("\n");
                sb.append("Client ID              : ").append(getOidcClientId()).append("\n");
                sb.append("Client Secret          : ").append(getOidcClientSecret()).append("\n");
            } else {
                sb.append("Bearer token (fixed)   : ").append(getAuthFixedToken()).append("\n");
            }

        } else {
            sb.append("Authorize              : disabled").append("\n");
        }

        sb.append("Write msg to disk      : ").append(getWriteReceivedMsgToDisk()).append("\n");
        return sb.toString();

    }
}
