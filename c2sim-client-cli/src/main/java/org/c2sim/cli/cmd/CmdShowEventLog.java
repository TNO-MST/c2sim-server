package org.c2sim.cli.cmd;

import org.c2sim.cli.app.MainMenu;
import org.c2sim.cli.ui.Console;
import org.c2sim.cli.utils.ExceptionHandler;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.invoker.ApiException;

public class CmdShowEventLog  extends MenuCommand {

    public CmdShowEventLog(MainMenu mainMenu) {
        super(mainMenu);
    }

    @Override public String getId()     { return "eventlog"; }
    @Override public String getTitle()  { return "Show Event Log"; }

    @Override public boolean isActive() { return true; }

    @Override
    public boolean execute() {
        Console.clearScreen();
        Console.printSection("Event Log:");
        for(var eventText : mainMenu.getEvents()) {
            Console.info(eventText);
        }

        return true;
    }
}

