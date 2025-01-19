
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 *
 * The TextEngine class provides methods for printing text with delays, clearing
 * the console screen, and waiting for user input. It also includes methods for
 * checking the validity of input commands, parsing commands, and matching
 * commands against a list of possible commands.
 *
 * @author Caden Finley
 * @version 1.0
 */
public abstract class TextEngine {

    private static String speedSetting = "normal";
    public final static Console console = System.console();
    public static String yellowColor = "\033[1;33m";
    public static String resetColor = "\033[0m";
    public static String greenColor = "\033[0;32m";

    public static int MAX_LINE_WIDTH = 50; // Define the maximum line width

    private static final String[] BREAK_COMMANDS = {};

    /**
     * Sets the terminal width based on the current terminal settings.
     *
     * @return a string indicating the terminal width
     */
    public static String setWidth() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;
            if (os.contains("win")) {
                processBuilder = new ProcessBuilder("cmd", "/c", "mode con");
            } else {
                processBuilder = new ProcessBuilder("sh", "-c", "tput cols");
            }
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            if (os.contains("win")) {
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Columns")) {
                        String[] parts = line.split(":");
                        if (parts.length > 1) {
                            line = parts[1].trim();
                            break;
                        }
                    }
                }
            } else {
                line = reader.readLine();
            }
            if (line != null && !line.isEmpty()) {
                MAX_LINE_WIDTH = Integer.parseInt(line) + 20;
                return ("Terminal width: " + line);
            } else {
                System.out.println("Could not get the terminal width, using default value");
                MAX_LINE_WIDTH = 30; // Default width if tput/mode con fails
                return ("Terminal width: " + "50");
            }
        } catch (IOException e) {
            System.out.println("Could not get the terminal width, using default value");
            MAX_LINE_WIDTH = 30; // Default width if an exception occurs
            return ("Terminal width: " + "50");
        }
    }

    /**
     * Prints text with delays between characters.
     *
     * @param data the text to print
     * @param inputBuffer whether to display an input buffer message
     * @param newLine whether to print a new line at the end
     */
    public static void printWithDelays(String data, boolean inputBuffer, boolean newLine) {
        boolean needToBreak = false;
        // Use inputBuffer if you are accepting input after the text is printed
        if (speedSetting.equals("nodelay") || Engine.TESTING) {
            printNoDelay(data, inputBuffer, newLine);
            return;
        }
        if (inputBuffer) {
            data = data + yellowColor + " (press enter to type)" + resetColor;
        }
        int currentLineWidth = 0; // Initialize the current line width
        String[] words = data.split(" "); // Split the data into words
        StringBuilder remainingChars = new StringBuilder(data); // Initialize remaining characters
        for (String word : words) {
            if (word.contains("\\")) {
                needToBreak = true;
            }
            if (inputBuffer) {
                if ((currentLineWidth + word.length() >= MAX_LINE_WIDTH + 30) && currentLineWidth != 0) {
                    System.out.print('\n');
                    currentLineWidth = 0;
                }
            } else {
                if ((currentLineWidth + word.length() >= MAX_LINE_WIDTH) && currentLineWidth != 0) {
                    System.out.print('\n');
                    currentLineWidth = 0;
                }
            }
            for (char ch : word.toCharArray()) {
                if (ch == '\n') {
                    System.out.print('\n');
                    currentLineWidth = 0;
                    needToBreak = false;
                    continue;
                }
                if (String.valueOf(ch).matches("^[a-zA-Z0-9]+$") && !String.valueOf(ch).matches(" ")) {
                    try {
                        switch (speedSetting) {
                            case "slow" ->
                                TimeUnit.MILLISECONDS.sleep(30);
                            case "fast" ->
                                TimeUnit.MILLISECONDS.sleep(10);
                            case "normal" -> {
                                TimeUnit.MILLISECONDS.sleep(20);
                            }
                            default -> {
                                //do nothing
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.print(ch);
                currentLineWidth++;
                remainingChars.deleteCharAt(0); // Remove the printed character from remainingChars
            }
            if (needToBreak) {
                System.out.print('\n');
                currentLineWidth = 0;
                needToBreak = false;
            }
            if (currentLineWidth > 0) {
                System.out.print(' ');
                currentLineWidth++;
                //remainingChars.deleteCharAt(0); // Remove the printed space from remainingChars
            }
        }
        if (newLine) {
            System.out.print('\n');
        }
        if (inputBuffer) {
            console.readLine();
            System.out.print(greenColor + "> " + resetColor);
        }
    }

    /**
     * Prints text without delays between characters.
     *
     * @param data the text to print
     * @param inputBuffer whether to display an input buffer message
     * @param newLine whether to print a new line at the end
     */
    public static void printNoDelay(String data, boolean inputBuffer, boolean newLine) { //use inputBuffer is you are accepting input after the text is printed
        boolean needToBreak = false;
        if (inputBuffer) {
            data = data + yellowColor + " (press enter to type)" + resetColor;
        }
        int currentLineWidth = 0; // Initialize the current line width
        String[] words = data.split(" "); // Split the data into words
        StringBuilder remainingChars = new StringBuilder(data); // Initialize remaining characters
        for (String word : words) {
            if (word.contains("\\")) {
                needToBreak = true;
            }
            if (inputBuffer) {
                if ((currentLineWidth + word.length() >= MAX_LINE_WIDTH + 30) && currentLineWidth != 0) {
                    System.out.print('\n');
                    currentLineWidth = 0;
                }
            } else {
                if ((currentLineWidth + word.length() >= MAX_LINE_WIDTH) && currentLineWidth != 0) {
                    System.out.print('\n');
                    currentLineWidth = 0;
                }
            }
            for (char ch : word.toCharArray()) {
                if (ch == '\n') {
                    System.out.print('\n');
                    currentLineWidth = 0;
                    needToBreak = false;
                    continue;
                }
                System.out.print(ch);
                currentLineWidth++;
                remainingChars.deleteCharAt(0); // Remove the printed character from remainingChars
            }
            if (needToBreak) {
                System.out.print('\n');
                currentLineWidth = 0;
                needToBreak = false;
            }
            if (currentLineWidth > 0) {
                System.out.print(' ');
                currentLineWidth++;
                //remainingChars.deleteCharAt(0); // Remove the printed space from remainingChars
            }
        }
        if (newLine) {
            System.out.print('\n');
        }
        if (inputBuffer) {
            console.readLine();
            System.out.print(greenColor + "> " + resetColor);
        }
    }

    /**
     * Clears the console screen. This method determines the operating system
     * and clears the screen accordingly. For Windows, it uses a combination of
     * ANSI escape codes and the "clear" command. For other operating systems,
     * it uses only ANSI escape codes.
     */
    public static void clearScreen() { //clears the screen
        String OS_Name = System.getProperty("os.name");
        try {
            if (OS_Name.contains("Windows")) {
                System.out.print("\033[H\033[2J");
                System.out.flush();
                Runtime.getRuntime().exec(new String[]{"clear"});
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (final IOException e) {
            //do nothing
        }
    }

    /**
     * Adds a pause and waits for the user to press Enter to continue.
     */
    public static void enterToNext() { //adds a pause and waits for enter
        if (Engine.TESTING) {
            return;
        }
        printNoDelay(yellowColor + "Press Enter to continue" + resetColor, false, false);
        console.readLine();
    }

    /**
     * Checks for valid input command.
     *
     * @param command the input command
     * @return true if the command is valid, false otherwise
     */
    public static Boolean checkValidInput(String command) { //checks for valid input command
        return command != null && !command.isEmpty() && !"".equals(command);
    }

    /**
     * Parses the input command and matches it against a list of possible
     * commands.
     *
     * @param command the input command
     * @param possibleCommands the list of possible commands
     * @return the matched command
     */
    public static String parseCommand(String command, String possibleCommands[]) {
        String matchedCommand = command;
        int maxMatchLength = 0;
        for (String illegalCommand : BREAK_COMMANDS) {
            if (command.equals(illegalCommand)) {
                return command;
            }
        }
        for (String possibleCommand : possibleCommands) {
            if (command.equals(possibleCommand)) {
                return command;
            }
            int matchLength = getMatchLength(command, possibleCommand);
            if (matchLength > maxMatchLength) {
                maxMatchLength = matchLength;
                matchedCommand = possibleCommand;
            }
        }
        return (maxMatchLength > 0 && has(possibleCommands, matchedCommand)) ? matchedCommand.toLowerCase() : command.toLowerCase();
    }

    /**
     * Gets the match length between the input command and a possible command.
     *
     * @param command the input command
     * @param possibleCommand the possible command
     * @return the match length
     */
    public static int getMatchLength(String command, String possibleCommand) {
        if (command == null || possibleCommand == null) {
            return 0;
        }
        int length = Math.min(command.length(), possibleCommand.length());
        int matchLength = 0;
        for (int i = 0; i < length; i++) {
            if (command.charAt(i) == possibleCommand.charAt(i)) {
                matchLength++;
            } else {
                break;
            }
        }
        return matchLength;
    }

    /**
     * Checks if the matched command is in the list of possible commands.
     *
     * @param possibleCommands the list of possible commands
     * @param matchedCommand the matched command
     * @return true if the matched command is in the list, false otherwise
     */
    public static boolean has(String[] possibleCommands, String matchedCommand) {
        for (String possibleCommand : possibleCommands) {
            if (possibleCommand.equals(matchedCommand)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the speed setting for printing text.
     *
     * @param speed the speed setting
     */
    public static void setSpeedSetting(String speed) {
        speedSetting = speed;
    }

    /**
     * Gets the speed setting for printing text.
     *
     * @return the speed setting
     */
    public static String getSpeedSetting() {
        return speedSetting;
    }
}
