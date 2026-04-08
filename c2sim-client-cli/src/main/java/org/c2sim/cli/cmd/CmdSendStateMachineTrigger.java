package org.c2sim.cli.cmd;

import org.c2sim.cli.app.MainMenu;
import org.c2sim.cli.ui.Console;
import org.c2sim.cli.utils.ExceptionHandler;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.invoker.ApiException;
import org.c2sim.lox.exceptions.LoxException;
import org.c2sim.lox.exceptions.ValidationException;
import org.c2sim.statemachine.Trigger;

public class CmdSendStateMachineTrigger extends MenuCommand {

    private final Trigger trigger;
    public CmdSendStateMachineTrigger(MainMenu mainMenu, Trigger trigger) {
        super(mainMenu);
        this.trigger = trigger;
    }

    @Override public String getId()    { return "Trigger_" + trigger.toString(); }
    @Override public String getTitle() { return String.format("Send C2SIM message of type '%s'", trigger.toString()) ; }


    @Override
    public boolean isActive() {
        return mainMenu.isTriggerAllowed(trigger);
    }

    @Override
    public boolean execute()  {
        Console.clearScreen();
        Console.printSection("Send state machine trigger: " + trigger.toString());
        try {
            mainMenu.getClient().sendTrigger(trigger);

            Console.success("C2SIM message " + trigger.toString() + " accepted by server, " +
                    "new state: " + mainMenu.getClient().getCachedC2SimServerState().toString());

        } catch (ValidationException | C2SimRestException | ApiException | LoxException e) {
            ExceptionHandler.handle(e);
        }

        return true;
    }
}
