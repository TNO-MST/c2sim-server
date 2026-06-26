package org.c2sim.cli.cmd;

import org.c2sim.cli.app.MainMenu;
import org.c2sim.cli.ui.Console;
import org.c2sim.cli.utils.ExceptionHandler;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.invoker.ApiException;

public class CmdLateJoinRequestInit extends MenuCommand {

    public CmdLateJoinRequestInit(MainMenu mainMenu) {
        super(mainMenu);
    }

    @Override public String getId()     { return "RequestC2SimInit"; }
    @Override public String getTitle()  { return "Request C2SIM initialization (late join)"; }

    @Override public boolean isActive() { return mainMenu.getClient().isJoined(); }

    @Override
    public boolean execute() {
        Console.clearScreen();
        Console.printBanner("Request C2SIM initialization...");
        try {
            var xml = mainMenu.getClient().getSessionInitializationFromC2SimServer();
            var text = xml.length() > 500 ? xml.substring(0, 500) + "......<truncated>" : xml;
            Console.success("C2SIMInitialization: " + text);
        } catch (C2SimRestException | ApiException e) {
            ExceptionHandler.handle(e);
        }
        return true;
    }
}
