package org.c2sim.cli.cmd;

import org.c2sim.cli.app.MainMenu;
import org.c2sim.cli.ui.Console;

import java.util.ArrayList;
import java.util.List;

/**
 * A command that, when executed, opens a nested sub-menu of child commands.
 * Contains the single loop handler used by both sub-menus and the root menu.
 */
public class ContainerCommand extends MenuCommand {

    private final MenuCommand BACK;

    private final String id;
    private final String title;
    private final DisplayMode displayMode;
    private final List<MenuCommand> children;
    private final Runnable preRender;

    /** Sub-menu constructor (no extra header content). */
    public ContainerCommand(
            MainMenu mainMenu,
            String id, String title,
                            DisplayMode displayMode, List<MenuCommand> children) {
        this(mainMenu, id, title, displayMode, children, null);
    }

    /** Root constructor — {@code preRender} is called after the banner on every iteration. */
    public ContainerCommand(MainMenu mainMenu,
                            String id, String title,
                            DisplayMode displayMode, List<MenuCommand> children,
                            Runnable preRender) {
        super(mainMenu);
        this.id          = id;
        this.title       = title;
        this.displayMode = displayMode;
        this.children    = children;
        this.preRender   = preRender;
        BACK = new MenuCommand(mainMenu) {
            @Override public String getId()     { return "back"; }
            @Override public String getTitle()  { return "Back"; }
            @Override public boolean isActive() { return true; }
            @Override public boolean execute()  { return false; }
        };
    }

    @Override public String getId()     { return id; }
    @Override public String getTitle()  { return title; }

    @Override
    public boolean isActive() {
        return children.stream().anyMatch(MenuCommand::isActive);
    }

    // ── Single loop handler ────────────────────────────────────────────────

    private void loop(boolean addBack) {
        List<MenuCommand> base = new ArrayList<>(children);
        if (addBack) {
            base.add(BACK);
        }

        boolean running = true;
        while (running) {
            Console.clearScreen();
            Console.printBanner(title);
            if (preRender != null) preRender.run();

            List<MenuCommand> visible = MenuRenderer.filter(base, displayMode);
            MenuRenderer.render(visible);

            int choice = Console.readChoice(1, visible.size());
            if (choice != -1) {
                try {
                    running = visible.get(choice - 1).execute();
                } catch (Exception e) {
                    Console.error("Error: " + e.getMessage());
                }
                if (running) {
                    Console.waitForEnter();
                }
            }
        }
    }

    /** Sub-menu: adds Back; returns true so the parent loop continues. */
    @Override
    public boolean execute() {
        loop(true);
        return true;
    }

    /** Root entry point: no Back option; loop exit ends the application. */
    public void runAsRoot() {
        loop(false);
    }
}
