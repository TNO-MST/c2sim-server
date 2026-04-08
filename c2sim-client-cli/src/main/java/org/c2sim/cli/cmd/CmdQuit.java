package org.c2sim.cli.cmd;

import org.c2sim.cli.app.MainMenu;
import org.c2sim.cli.ui.Console;

public class CmdQuit extends MenuCommand {

    public CmdQuit(MainMenu mainMenu) {
        super(mainMenu);
    }

    @Override public String getId()     { return "quit"; }
    @Override public String getTitle()  { return "Quit"; }
    @Override public boolean isActive() { return true; }

    @Override
    public boolean execute() {
// TODO: for now just close app
        Console.success("Quit");
        System.exit(0);
        return true;
    }
}
