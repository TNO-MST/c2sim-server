package org.c2sim.cli.app;

import org.c2sim.cli.config.AppConfig;
import org.c2sim.cli.config.ConfigManager;
import org.c2sim.cli.ui.Console;

/**
 * Interactive wizard that walks the user through the full configuration
 */
public final class SetupWizard {

    private SetupWizard() {
    }

    public static AppConfig run(AppConfig config) {
        Console.clearScreen();
        Console.printBanner("Configure C2SIM Client");
        config.setServerUrl(Console.readLine("C2SIM server URL ", config.getServerUrl()));
        config.setSystemName(Console.readLine("C2SIM System name", config.getSystemName()));
        config.setClientDisplayName(Console.readLine(

                "Client display name (logging)", config.getClientDisplayName()));
        config.setSharedSessionName(Console.readLine(
                "Default shared session name", config.getSharedSessionName()));
        config.setWriteReceivedMsgToDisk(Console.readYesNo(
                "Save received C2SIM messages to disk ?", config.getWriteReceivedMsgToDisk()));
        boolean useOidc = Console.readYesNo("Use OpenID connect ?", config.isUseOidc());
        config.setUseOidc(useOidc);

        if (useOidc) {
            boolean useHardCodedBearer = Console.readYesNo("Use hardcoded bearer token ?", !config.isUseIdentityProvider());
            config.setUseIdentityProvider(!useHardCodedBearer);
            if (!useHardCodedBearer) {
                config.setOidcIdpUrl(Console.readLine(
                        "IDP token endpoint URL", config.getOidcIdpUrl()));
                Console.warning("See 'docker/credentials/c2sim_client_info.json' for keycloak accounts.");
                config.setOidcClientId(Console.readLine(
                        "OIDC Client ID", config.getOidcClientId()));
                config.setOidcClientSecret(Console.readLine(
                        "OIDC Client secret", config.getOidcClientSecret()));
            } else {
                config.seAuthFixedToken(Console.readLine("Bearer token", config.getAuthFixedToken()));
            }
        }


        // ── Save ──────────────────────────────────────────────────────────
        ConfigManager.save(config);
        Console.newLine();
        Console.success("Configuration saved to " + ConfigManager.CONFIG_FILE);

        return config;
    }
}
