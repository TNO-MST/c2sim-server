package org.c2sim.cli.utils;

import org.c2sim.cli.ui.Console;
import org.c2sim.client.exceptions.C2SimRestException;
import org.c2sim.client.invoker.ApiException;
import org.c2sim.lox.exceptions.LoxException;

import java.util.Map;

public final class ExceptionHandler {
    private ExceptionHandler() {}

    public static void handleApiException(ApiException e) {
        Console.error("C2SIM API exception: ");

            Console.error("- Status code: " + e.getCode());
            Console.error("- Error message: " + e.getMessage());


    }

    public static void handleC2SimRestException(C2SimRestException e) {

        Console.error("C2SIM REST Error from C2SIM server: ");
        Console.error("- Error code: " + e.getError().getCode());
        Console.error("- Error msg: " + e.getError().getMessage());
        if (e.getError().getDetails() != null && !e.getError().getDetails().isEmpty()) {
            Console.error("- Details:");
            for (Map.Entry<String, Object> entry : e.getError().getDetails().entrySet()) {
                Console.error("   * " + entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    public static void handleLoxException(LoxException e) {
        Console.error("LOX Exception: " + e.getMessage());
    }

    public static void handle(Exception e) {
        switch (e) {
            case C2SimRestException rest -> handleC2SimRestException(rest);
            case LoxException lox -> handleLoxException(lox);
            case ApiException api -> handleApiException(api);
            default -> Console.error("Unknown error: " + e.getMessage());
        }    }
}
