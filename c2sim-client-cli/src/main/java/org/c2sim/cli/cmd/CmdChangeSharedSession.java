package org.c2sim.cli.cmd;

import org.c2sim.cli.app.MainMenu;
import org.c2sim.cli.ui.Console;
import org.c2sim.cli.utils.ExceptionHandler;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.invoker.ApiException;
import org.c2sim.client.model.DynamicSessionInfo;

import java.util.ArrayList;
import java.util.List;

public class CmdChangeSharedSession extends MenuCommand {

    public CmdChangeSharedSession(MainMenu mainMenu) {
        super(mainMenu);
    }

    @Override public String getId()     { return "change_session"; }
    @Override public String getTitle()  { return "Change shared session name"; }
    @Override public boolean isActive() { return !mainMenu.getClient().isJoined(); }

    @Override
    public boolean execute() {

        Console.clearScreen();
        Console.printBanner("Change shared session name");

        var sharedSessions = getSharedSessionNames();
        Console.newLine();
        Console.success("Active shared sessions on C2SIM server (or enter a shared session name to be created)");
        printMenu(sharedSessions);
        String sharedSessionName = Console.readListChoice(getSharedSessionNames());
        if (sharedSessionName == null || sharedSessionName.isBlank()) {
            return true;
        }
        try {
            mainMenu.getClient().setSharedSessionName(sharedSessionName);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
        if (!sharedSessions.contains(sharedSessionName)) {
            Console.newLine();
            Console.info(String.format("Shared session '%s' does not exist on C2SIM server, " +
                            "will be created when joining shared session.",
                    sharedSessionName));
            mainMenu.schemaVersion = Console.readLine(
                    String.format("What C2SIM schema version should shared session '%s' use?",
                            sharedSessionName ), "1.0.2");
            mainMenu.newSharedSessionDescription = Console.readLine(
                    String.format("Description for shared session '%s'.", sharedSessionName), "TEST123");
        }
        return true;
    }

    private List<String> getSharedSessionNames() {
        try {
            List<DynamicSessionInfo> sessions = mainMenu.getClient().getSharedSessionsFromC2SimServer();
            return sessions.stream().map(DynamicSessionInfo::getSessionName).toList();
        } catch (ApiException | C2SimRestException e) {
            ExceptionHandler.handle(e);
        }
        return new ArrayList<>();
    }

    private void printMenu(List<String> files) {
        for (int i = 0; i < files.size(); i++) {
            Console.printMenuOption(i + 1, files.get(i));
        }
        Console.newLine();
    }
}
