package org.c2sim.cli;

import org.c2sim.cli.app.MainMenu;
import org.c2sim.cli.app.SetupWizard;
import org.c2sim.cli.cmd.DisplayMode;
import org.c2sim.cli.config.AppConfig;
import org.c2sim.cli.config.ConfigManager;
import org.c2sim.cli.ui.Console;

/**
 * Entry point for the C2SIM Client CLI.
 * 
 */
public class Main {

    public static void main(String[] args) {
        Console.install();
        Console.clearScreen();
        Console.printBanner("C2SIM Client CLI");
        Console.newLine();

        try {
            AppConfig config = ConfigManager.load();
            if (config.isComplete()) {
                Console.info("Configuration:");
                Console.info(config.getConfigSummary());
                Console.newLine();
                boolean useSettings = Console.readYesNo(
                        "Use these settings?", true);
                var main = new MainMenu(useSettings ? config : SetupWizard.run(config), DisplayMode.ALL);
                main.run();
            } else {
                Console.error("Configuration error");
            }
        } catch (Exception e) {
            Console.error("Fatal error: " + e.getMessage());
        } finally {
            Console.uninstall();
        }
    }
}
