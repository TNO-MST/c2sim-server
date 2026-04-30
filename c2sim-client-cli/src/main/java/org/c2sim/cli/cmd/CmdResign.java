package org.c2sim.cli.cmd;

import org.c2sim.cli.app.MainMenu;
import org.c2sim.cli.ui.Console;
import org.c2sim.cli.utils.ExceptionHandler;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.invoker.ApiException;

public class CmdResign extends MenuCommand {

    public CmdResign(MainMenu mainMenu) {
        super(mainMenu);
    }

    @Override public String getId()     { return "resign"; }
    @Override public String getTitle()  { return "Resign from shared session (disconnect stream)"; }

    @Override public boolean isActive() { return mainMenu.getClient().isJoined(); }

    @Override
    public boolean execute() {
        Console.clearScreen();
        Console.printBanner("Resign from shared session, and disconnect stream...");
        try {
            mainMenu.getClient().resignAndDisconnect();
            Console.success("Resigned.");
        } catch (C2SimRestException | ApiException e) {
            ExceptionHandler.handle(e);
        }
        return true;
    }
}
