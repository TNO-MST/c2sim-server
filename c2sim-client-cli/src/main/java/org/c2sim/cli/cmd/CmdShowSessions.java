package org.c2sim.cli.cmd;

import org.c2sim.cli.app.MainMenu;
import org.c2sim.cli.ui.Console;
import org.c2sim.cli.utils.ExceptionHandler;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.invoker.ApiException;
import org.c2sim.client.model.DynamicSessionInfo;

import java.util.List;

public class CmdShowSessions extends MenuCommand {

    public CmdShowSessions(MainMenu mainMenu) {
        super(mainMenu);
    }

    @Override
    public String getId() {
        return "show_sessions";
    }

    @Override
    public String getTitle() {
        return "Show shared sessions on C2SIM server";
    }



    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean execute() {
        Console.clearScreen();
        Console.printBanner("Getting shared sessions on C2SIM server");
        try {
            List<DynamicSessionInfo> sessions = mainMenu.getClient().getSharedSessionsFromC2SimServer();
            if (sessions.isEmpty()) {
                Console.error("No sessions exist yet.");
            } else {
                Console.printSection("Shared sessions");
                for (DynamicSessionInfo s : sessions) {
                    Console.info(String.format("- Shared session '%s'  (state: %-14s  schema version: '%s')",
                            s.getSessionName(),
                            s.getState(),
                            s.getInfo().getC2simSchemaVersion()));
                }
            }
        } catch (ApiException | C2SimRestException e) {
            ExceptionHandler.handle(e);
        }

        return true;
    }
}
