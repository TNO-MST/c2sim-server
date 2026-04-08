package org.c2sim.cli.cmd;

import java.util.List;


/**
 * Shared rendering logic used by MainMenu and ContainerCommand.
 * Handles filtering by DisplayMode and rendering with group section headers.
 */
public final class MenuRenderer {

    private MenuRenderer() {}

    /**
     * Filters a command list according to the given DisplayMode.
     * In ALL mode the original list is returned unchanged.
     * In ACTIVE_ONLY mode only commands where isActive() is true are included.
     */
    public static List<MenuCommand> filter(List<MenuCommand> commands, DisplayMode mode) {
        if (mode == DisplayMode.ALL) {
            return commands;
        }
        return commands.stream().filter(MenuCommand::isActive).toList();
    }

    /**
     * Renders a list of commands with sequential numbering
     */
    public static void render(List<MenuCommand> visible) {
        int number = 1;
        for (MenuCommand cmd : visible) {
            cmd.print(number);
            number++;
        }
    }
}
