package org.c2sim.cli.ui;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Central class for all console I/O.
 * Provides color output (via Jansi) and helpers for reading user input with
 * defaults.
 */
public final class Console {

    private static final BufferedReader READER = new BufferedReader(new InputStreamReader(System.in));

    private Console() {
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────

    public static void install() {
        AnsiConsole.systemInstall();
    }

    public static void uninstall() {
        AnsiConsole.systemUninstall();
    }

    // ── Screen ─────────────────────────────────────────────────────────────

    private static void print(String text) {
        System.out.print(text);
    }

    public static void clearScreen() {
        print(ansi().eraseScreen().cursor(1, 1).toString());
        System.out.flush();
    }

    // ── Structural output ──────────────────────────────────────────────────

    public static void printBanner(String title) {
        String line = "═".repeat(80);
        println(ansi().bold().fg(CYAN).a(line).reset().toString());
        println(ansi().bold().fg(CYAN).a(title).reset().toString());
        println(ansi().bold().fg(CYAN).a(line).reset().toString());
    }

    public static void printSection(String title) {
        int pad = Math.max(0, 80 - 4 - title.length());

        println(
                ansi().bold().fg(CYAN).a("─── ")
                        .bold().fg(WHITE).a(title + " ")
                        .fg(CYAN).a("─".repeat(pad)).reset().toString());
    }

    public static void newLine() {
        println("");
    }

    public static void println(String text) {
        System.out.println(text);
    }

    // ── Status messages ────────────────────────────────────────────────────

    public static void success(String msg) {
        println(ansi().fg(GREEN).bold().a(msg).reset().toString());
    }

    public static void error(String msg) {
        var ansiText = ansi().fg(RED).bold().a(msg).reset().toString();
        println(ansiText);
    }

    public static void info(String msg) {
        println(ansi().fg(WHITE).bold().a(msg).reset().toString());
    }

    public static void warning(String msg) {
        println(ansi().fg(YELLOW).bold().a(msg).reset().toString());
    }

    // ── Menu rendering ─────────────────────────────────────────────────────

    /**
     * Renders a numbered menu option.
     *
     * @param active true → (command available in current state)
     *               false → (command not available)
     */
    public static void printMenuOption(int num, String label, boolean active) {
        if (active) {
            print(ansi().fg(Ansi.Color.GREEN).toString());
        } else {
            print(ansi().fgBright(Ansi.Color.BLACK).toString()); // this is gray
        }
        println(
                ansi().bold()
                        .a("[").a(String.format("%2d", num)).a("] ")
                        .a(label).toString());
    }

    public static void printMenuOption(int num, String label) {
        printMenuOption(num, label, true);
    }

    // ── Input helpers ──────────────────────────────────────────────────────

    /**
     * Prints a prompt and reads a line. Pressing Enter returns
     * {@code defaultValue}.
     * The default is shown in yellow brackets next to the prompt.
     */
    public static String readLine(String prompt, String defaultValue) {
        String defaultDisplay = (defaultValue != null && !defaultValue.isEmpty())
                ? ansi().fg(
                        CYAN).a("[" + defaultValue + "] ").reset().toString()
                : "";
        print(
                ansi().bold().fg(WHITE).a(prompt + ": ").reset()
                        + defaultDisplay);
        try {
            String input = READER.readLine();
            if (input == null || input.isBlank()) {
                return defaultValue != null ? defaultValue : "";
            }
            return input.trim();
        } catch (IOException e) {
            return defaultValue != null ? defaultValue : "";
        }
    }

    public static String readLine(String prompt) {
        return readLine(prompt, null);
    }

    /**
     * Prints a yes/no prompt. The default choice is shown in uppercase.
     * Pressing Enter accepts the default.
     */
    public static boolean readYesNo(String prompt, boolean defaultValue) {
        String hint = defaultValue ? "Y/n" : "y/N";
        var ansiText = ansi()
                .bold().fg(WHITE).a(prompt + " ").reset()
                .fg(CYAN).a("[" + hint + "] ").reset().toString();
        print(ansiText);
        try {
            String input = READER.readLine();
            if (input == null || input.isBlank())
                return defaultValue;
            String t = input.trim().toLowerCase();
            return t.equals("y") || t.equals("yes");
        } catch (IOException e) {
            return defaultValue;
        }
    }

    /**
     * Reads an integer in [min, max]. Keeps asking until a valid number is entered.
     */
    public static int readChoice(int min, int max) {
        while (true) {
            print(
                    "\n  " + ansi().bold().fg(CYAN).a("Enter choice [" + min + "-" + max + "]: ").reset());
            try {
                String input = READER.readLine();
                if (input == null)
                    continue;
                int choice = Integer.parseInt(input.trim());
                if (choice >= min && choice <= max)
                    return choice;
                error("Please enter a number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                return -1;
            } catch (IOException e) {
                return min;
            }
        }
    }

    /**
     * Shows a numbered list of files and lets the user pick by number or by typing
     * the filename directly. Returns null if the user presses Enter without input.
     */
    public static String readListChoice(List<String> files) {
        print(
                "  " + ansi().bold().fg(WHITE).a("Number or name (Enter = cancel): ").reset());
        try {
            String input = READER.readLine();
            if (input == null || input.isBlank())
                return null;
            input = input.trim();
            try {
                int idx = Integer.parseInt(input) - 1;
                if (idx >= 0 && idx < files.size())
                    return files.get(idx);
                error("Number out of range");
                return null;
            } catch (NumberFormatException e) {
                return input; // treat as literal name
            }
        } catch (IOException e) {
            return null;
        }
    }

    /** Pauses until the user presses Enter. */
    public static void waitForEnter() {
        println("");
        println(ansi().fg(YELLOW).a("Press Enter to continue...").reset().toString());
        try {
            READER.readLine();
        } catch (IOException ignored) {
        }
    }
}
