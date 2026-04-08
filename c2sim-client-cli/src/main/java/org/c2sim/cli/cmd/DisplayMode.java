package org.c2sim.cli.cmd;

/**
 * Controls which commands are shown in the menu.
 * Configured once at the root (MainMenu) level and propagated to sub-menus.
 */
public enum DisplayMode {
    /** Show all commands; inactive ones are displayed in a dimmed style. */
    ALL,
    /** Show only active (currently available) commands; inactive ones are hidden. */
    ACTIVE_ONLY
}
