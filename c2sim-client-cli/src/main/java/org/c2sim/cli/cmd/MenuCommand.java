package org.c2sim.cli.cmd;

import org.c2sim.cli.app.MainMenu;
import org.c2sim.cli.ui.Console;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Abstract base for all menu commands.
 * Subclasses that need application services receive a {@link MainMenu} reference
 * via the constructor.
 */
public abstract class MenuCommand {

    @NotNull
    protected final MainMenu mainMenu;

    /** For commands that require application services. */
    protected MenuCommand(MainMenu mainMenu) {
        this.mainMenu = Objects.requireNonNull(mainMenu,
                "mainMenu must not be null");

    }

    /** Short unique identifier. */
    public abstract String getId();

    /** Text displayed in the menu. */
    public abstract String getTitle();

    /** True when the command is valid/available in the current application state. */
    public abstract boolean isActive();

    /**
     * Execute this command.
     *
     * @return true  – remain in the current menu loop
     *         false – exit the current menu loop
     */
    public abstract boolean execute();

    /** Renders this entry as a numbered menu option. */
    public void print(int number) {
        Console.printMenuOption(number, getTitle(), isActive());
    }
}
