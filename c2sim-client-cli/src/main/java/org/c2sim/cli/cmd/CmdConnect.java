package org.c2sim.cli.cmd;

import org.c2sim.cli.app.MainMenu;
import org.c2sim.cli.ui.Console;
import org.c2sim.cli.utils.ExceptionHandler;
import org.c2sim.client.exceptions.C2ClientException;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.invoker.ApiException;

public class CmdConnect extends MenuCommand {

    public CmdConnect(MainMenu mainMenu) {
        super(mainMenu);
    }

    @Override public String getId()     { return "connect"; }
    @Override public String getTitle()  { return "Join shared session on C2SIM server (and setup stream)"; }
    @Override public boolean isActive() { return !mainMenu.getClient().isJoined(); }

    @Override
    public boolean execute() {

        Console.clearScreen();
        Console.printBanner(String.format("Connecting to C2SIM server on '%s' ...",
                mainMenu.getClient().getBasePathUrl()));
        Console.newLine();
        try {
            Console.info(String.format("Join shared session '%s', and connect stream.", mainMenu.getClient().getSharedSessionName()));
            mainMenu.getClient().connect();
            Console.newLine();
            Console.success("Joined and connected");
        } catch (C2SimRestException | C2ClientException | ApiException e) {
            ExceptionHandler.handle(e);
        }
        return true;
    }
}
