
import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.json.JSONObject;

/**
 * @author Caden Finley
 */
public class Engine {

    private static final Console console = System.console();
    public static boolean TESTING = false;
    private static boolean startCommandsOn = true;
    private static boolean incognitoChatMode = false;
    private static boolean usingChatCache = true;
    private static List<String> savedChatCache = new ArrayList<>();

    private static OpenAIPromptEngine openAIPromptEngine;
    private static TerminalPassthrough terminal;

    private static Queue<String> commandsQueue = null;
    private static String lastCommandParsed = null;
    private static boolean defaultTextEntryOnAI = false;
    private static boolean textBuffer = true;
    private static boolean shotcutsEnabled = true;

    private static final String GREEN_COLOR_BOLD = "\033[1;32m";
    private static final String RESET_COLOR = "\033[0m";
    private static final String RED_COLOR_BOLD = "\033[1;31m";
    private static final String MAIN_MENU_HEADER = GREEN_COLOR_BOLD + "AI Menu: " + RESET_COLOR;
    private static final String AI_CHAT_HEADER = GREEN_COLOR_BOLD + "AI Chat: " + RESET_COLOR;

    private static final File USER_DATA = new File(".USER_DATA.json");

    private static List<String> startupCommands;
    private static Map<String, String> shortcuts;

    /**
     * Main method to start the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        System.out.println(TextEngine.setWidth());
        TextEngine.clearScreen();
        TextEngine.printWithDelays("Loading...", false, true);
        startupCommands = new ArrayList<>();
        shortcuts = new HashMap<>();
        terminal = new TerminalPassthrough();
        openAIPromptEngine = new OpenAIPromptEngine();
        if (!USER_DATA.exists()) {
            createNewUSER_DATAFile();
        } else {
            loadUserData();
        }
        if (openAIPromptEngine.getAPIKey() == null || openAIPromptEngine.getAPIKey().isEmpty()) {
            System.out.println("OpenAI API key not found.");
            defaultTextEntryOnAI = false;
        } else {
            if (openAIPromptEngine.testAPIKey(openAIPromptEngine.getAPIKey())) {
                defaultTextEntryOnAI = true;
                TextEngine.printWithDelays(AI_CHAT_HEADER + "Successfully Connected to OpenAI servers!", false, true);
            } else {
                TextEngine.printWithDelays(AI_CHAT_HEADER + "An error occurred while connecting to OpenAI servers.", false, true);
                TextEngine.printWithDelays(AI_CHAT_HEADER + "Please check your internet connection and try again later.", false, true);
                defaultTextEntryOnAI = false;
            }
        }
        if (startupCommands != null && !startupCommands.isEmpty() && startCommandsOn) {
            System.out.println("Running startup commands...");
            for (String command : startupCommands) {
                commandParser("." + command);
            }
        }
        mainProcessLoop();
    }

    /**
     * Main process loop to handle user input and command parsing.
     */
    private static void mainProcessLoop() {
        while (true) {
            if (TESTING) {
                System.out.println(RED_COLOR_BOLD + "DEV MODE" + RESET_COLOR);
            }
            if (defaultTextEntryOnAI) {
                TextEngine.printNoDelay(MAIN_MENU_HEADER, textBuffer, false);
            } else {
                TextEngine.printNoDelay(terminal.returnCurrentTerminalPosition(), textBuffer, false);
            }
            String command = console.readLine();
            commandParser(command);
        }
    }

    /**
     * Creates a new user data file if it does not exist.
     */
    private static void createNewUSER_DATAFile() {
        try {
            System.out.println("User data file not found. Creating new file...");
            USER_DATA.createNewFile();
            startupCommands.add("terminal cd /");
            writeUserData();
        } catch (IOException e) {
            TextEngine.printWithDelays("An error occurred while creating the user data file.", false, true);
        }
    }

    /**
     * Loads user data from the user data file.
     */
    private static void loadUserData() {
        try {
            System.out.println("Loading user data from: " + USER_DATA.getAbsolutePath());
            JSONObject userData = new JSONObject(Files.readString(USER_DATA.toPath()));
            openAIPromptEngine.setAPIKey(userData.getString("OpenAI_API_KEY"));
            savedChatCache = new ArrayList<>();
            userData.getJSONArray("Chat_Cache").forEach(item -> savedChatCache.add((String) item));
            openAIPromptEngine.setChatCache(savedChatCache);
            startupCommands = new ArrayList<>();
            userData.getJSONArray("Startup_Commands").forEach(item -> startupCommands.add((String) item));
            TextEngine.setSpeedSetting(userData.getString("Text_Speed"));
            shotcutsEnabled = userData.getBoolean("Shortcuts_Enabled");
            shortcuts = new HashMap<>();
            userData.getJSONObject("Shortcuts").toMap().forEach((key, value) -> shortcuts.put(key, (String) value));
            textBuffer = userData.getBoolean("Text_Buffer");
        } catch (IOException e) {
            TextEngine.printWithDelays("An error occurred while reading the user data file.", false, true);
        }
    }

    /**
     * Writes user data to the user data file.
     */
    private static void writeUserData() {
        try (FileWriter file = new FileWriter(USER_DATA)) {
            JSONObject userData = new JSONObject();
            userData.put("OpenAI_API_KEY", openAIPromptEngine.getAPIKey());
            userData.put("Chat_Cache", savedChatCache);
            userData.put("Startup_Commands", startupCommands);
            userData.put("Text_Speed", TextEngine.getSpeedSetting());
            userData.put("Shortcuts_Enabled", shotcutsEnabled);
            userData.put("Shortcuts", shortcuts);
            userData.put("Text_Buffer", textBuffer);
            file.write(userData.toString());
            file.flush();
        } catch (IOException e) {
            TextEngine.printWithDelays("An error occurred while writing to the user data file.", false, true);
        }
    }

    /**
     * Reads and returns the content of the user data file.
     *
     * @return The content of the user data file as a String
     */
    private static String readAndReturnUserDataFile() {
        try {
            String userData = Files.readString(USER_DATA.toPath());
            if (userData == null || userData.isEmpty()) {
                return "No data found.";
            }
            return userData;
        } catch (IOException e) {
            TextEngine.printWithDelays("An error occurred while reading the user data file.", false, true);
            return null;
        }
    }

    /**
     * Splits a command string into an array of commands.
     *
     * @param command The command string to split
     * @return An array of commands
     */
    private static String[] commandSplicer(String command) {
        int numberOfWordsInSeparates = 0;
        String[] commands = command.split(" ");
        for (int i = 0; i < commands.length; i++) {
            if (commands[i].startsWith("'") || commands[i].startsWith("(")) {
                char startChar = commands[i].charAt(0);
                char endChar = startChar == '(' ? ')' : startChar == '[' ? ']' : '\'';
                commands[i] = commands[i].substring(1);
                StringBuilder combined = new StringBuilder(commands[i]);
                while (!commands[i].endsWith(String.valueOf(endChar))) {
                    i++;
                    numberOfWordsInSeparates++;
                    if (i >= commands.length) {
                        break;
                    }
                    combined.append(" ").append(commands[i]);
                }
                combined.deleteCharAt(combined.length() - 1);
                commands[i] = combined.toString();
            }
            commands = Arrays.stream(commands).filter(s -> !s.equals("'") && !s.equals("(") && !s.equals(")")).toArray(String[]::new);
        }
        int index = commands.length - 2;
        String bufferCommand = commands[commands.length - 1];
        for (int i = 1; i < numberOfWordsInSeparates; i++) {
            commands = Arrays.copyOf(commands, index);
            index--;
        }
        commands[commands.length - 1] = bufferCommand;
        return commands;
    }

    /**
     * Parses and processes a command string.
     *
     * @param command The command string to parse
     */
    private static void commandParser(String command) {
        if (command == null || command.isEmpty()) {
            TextEngine.printWithDelays("Invalid input. Please try again.", false, true);
            return;
        }
        if (command.equals("restart")) {
            System.out.println("Restarting...");
            //add funtionality what all does this need to do
            //clear terminal chat cache
            //clear screen
            //rerun startup sequence
            return;
        }
        if (command.equals("clear")) {
            commandProcesser("clear");
            return;
        }
        if (command.equals("exit")) {
            commandProcesser("exit");
            return;
        }
        if (command.equals("aihelp")) {
            if (!defaultTextEntryOnAI && openAIPromptEngine.getAPIKey() != null && !openAIPromptEngine.getAPIKey().isEmpty()) {
                String message = ("I am encountering these errors in the " + terminal.getTerminalName() + " and would like some help solving these issues: " + terminal.getTerminalCache());
                TextEngine.printWithDelays(openAIPromptEngine.buildPromptAndReturnResponce(message, false), false, true);
                System.out.println();
                return;
            }
            commandProcesser("help");
            return;
        }
        if (command.startsWith("ss")) {
            shortcutProcesser(command);
            return;
        }
        if (command.startsWith(".")) {
            commandProcesser(command.substring(1));
            return;
        }
        if (defaultTextEntryOnAI) {
            chatProcess(command);
        } else {
            sendTerminalCommand(command);
        }
    }

    /**
     * Processes a shortcut command.
     *
     * @param command The shortcut command to process
     */
    private static void shortcutProcesser(String command) {
        if (!shotcutsEnabled) {
            System.out.println("Shortcuts are disabled.");
            return;
        }
        if (shortcuts != null && !shortcuts.isEmpty()) {
            command = command.substring(2);
            if (command.isBlank() || command.isEmpty() || command.equals(" ")) {
                System.out.println("No shortcut given.");
                return;
            }
            String strippedCommand = command.trim();
            if (shortcuts.containsKey(strippedCommand)) {
                commandProcesser(shortcuts.get(strippedCommand));
            } else {
                System.out.println("No command for given shortcut.");
            }
        } else {
            System.out.println("No shortcuts.");
        }
    }

    /**
     * Processes a command string.
     *
     * @param command The command string to process
     */
    private static void commandProcesser(String command) {
        commandsQueue = new LinkedList<>();
        commandsQueue.addAll(Arrays.asList(commandSplicer(command)));
        if (TESTING) {
            System.out.println(commandsQueue);
        }
        if (commandsQueue.isEmpty()) {
            TextEngine.printWithDelays("Unknown command. Please try again.", false, true);
        }
        getNextCommand();
        switch (lastCommandParsed) {
            case "clear" -> {
                System.out.println("Clearing screen and terminal cache...");
                TextEngine.clearScreen();
                terminal.clearTerminalCache();
            }
            case "ai" -> {
                aiSettingsCommands();
            }
            case "user" -> {
                userSettingsCommands();
            }
            case "terminal" -> {
                String strippedCommand;
                try {
                    strippedCommand = command.substring(9);
                    sendTerminalCommand(strippedCommand);
                } catch (StringIndexOutOfBoundsException e) {
                    defaultTextEntryOnAI = false;
                    return;
                }
            }
            case "exit" ->
                exit();
            case "help" -> {
                TextEngine.printWithDelays("Commands:", false, true);
                TextEngine.printWithDelays(".ai o[ARGS]", false, true);
                TextEngine.printWithDelays(".terminal o[ARGS]", false, true);
                TextEngine.printWithDelays(".user", false, true);
                TextEngine.printWithDelays(".exit", false, true);
                TextEngine.printWithDelays(".clear or clear", false, true);
                TextEngine.printWithDelays(".help", false, true);
            }
            default ->
                TextEngine.printWithDelays("Unknown command. Please try again. Type 'help' or '.help' if you need help", false, true);
        }
    }

    /**
     * Sends a command to the terminal.
     *
     * @param command The command to send
     */
    private static void sendTerminalCommand(String command) {
        if (TESTING) {
            System.out.println("Sending Command: " + command);
        }
        Thread commandThread = terminal.executeCommand(command, true);
        try {
            commandThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread Interrupted");
        }
    }

    /**
     * Processes AI settings commands.
     */
    private static void aiSettingsCommands() {
        getNextCommand();
        if (lastCommandParsed == null) {
            defaultTextEntryOnAI = true;
            showChatHistory();
            return;
        }
        if (lastCommandParsed.equals("log")) {
            String lastChatSent = openAIPromptEngine.getLastPromptUsed();
            String lastChatRecieved = openAIPromptEngine.getLastResponseReceived();
            File fileName = new File("OpenAPI_Chat_" + TimeEngine.timeStamp() + ".txt");
            try {
                fileName.createNewFile();
            } catch (IOException e) {
                TextEngine.printWithDelays("An error occurred while creating the chat file.", false, true);
            }
            try (FileWriter file = new FileWriter(fileName)) {
                file.write("Chat Sent: " + lastChatSent + "\n");
                file.write("Chat Recieved: " + lastChatRecieved + "\n");
                file.flush();
                TextEngine.printWithDelays("Chat log saved to " + fileName.getName(), false, true);
            } catch (IOException e) {
                TextEngine.printWithDelays("An error occurred while writing to the chat file.", false, true);
            }
            getNextCommand();
            if (lastCommandParsed == null) {
                return;
            }
            if (lastCommandParsed.equals("extract")) {
                extractCodeSnippet(fileName);
                return;
            }
            System.out.println("Unknown command. No given ARGS. Try 'help'");
            return;
        }
        if (lastCommandParsed.equals("apikey")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                return;
            }
            if (lastCommandParsed.equals("set")) {
                getNextCommand();
                if (lastCommandParsed == null) {
                    TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                    return;
                }
                openAIPromptEngine.setAPIKey(lastCommandParsed);
                if (openAIPromptEngine.testAPIKey(openAIPromptEngine.getAPIKey())) {
                    System.out.println("OpenAI API key set.");
                    return;
                } else {
                    TextEngine.printWithDelays("Invalid API key. AI services have been disabled", false, true);
                    return;
                }
            }
            if (lastCommandParsed.equals("get")) {
                System.out.println(openAIPromptEngine.getAPIKey());
                return;
            }
            System.out.println("Unknown command. No given ARGS. Try 'help'");
        }
        if (lastCommandParsed == null) {
            return;
        }
        if (lastCommandParsed.equals("chat")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                return;
            }
            TextEngine.printWithDelays(TimeEngine.timeStamp() + " Sent message to GPT: " + lastCommandParsed, false, true);
            chatProcess(lastCommandParsed);
            return;
        }
        if (lastCommandParsed.equals("get")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                return;
            }
            System.out.println(openAIPromptEngine.getResponseData(lastCommandParsed));
            return;
        }
        if (lastCommandParsed.equals("dump")) {
            System.out.println(openAIPromptEngine.getResponseData("all"));
            System.out.println(openAIPromptEngine.getLastPromptUsed());
            return;
        }
        if (lastCommandParsed.equals("help")) {
            System.out.println("Commands: ");
            System.out.println("apikey: set [ARGS], get");
            System.out.println("chat: [ARGS]");
            System.out.println("get: [ARGS]");
            System.out.println("dump");
            return;
        }
        TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
    }

    /**
     * Processes user settings commands.
     */
    private static void userSettingsCommands() {
        getNextCommand();
        if (lastCommandParsed == null) {
            TextEngine.printWithDelays("Unknown command. No given ARGS.", false, true);
            return;
        }
        if (lastCommandParsed.equals("startup")) {
            startupCommands();
            return;
        }
        if (lastCommandParsed.equals("chat")) {
            aiChatCommands();
            return;
        }
        if (lastCommandParsed.equals("text")) {
            textCommands();
            return;
        }
        if (lastCommandParsed.equals("shortcut")) {
            shortcutCommands();
            return;
        }
        if (lastCommandParsed.equals("testing")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                return;
            }
            if (lastCommandParsed.equals("enable")) {
                TESTING = true;
                TextEngine.printWithDelays("Testing mode enabled.", false, true);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
            if (lastCommandParsed.equals("disable")) {
                TESTING = false;
                TextEngine.printWithDelays("Testing mode disabled.", false, true);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
            System.out.println("Unknown command. No given ARGS. Try 'help'");
            return;
        }
        if (lastCommandParsed.equals("data")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                return;
            }
            if (lastCommandParsed.equals("get")) {
                System.out.println(readAndReturnUserDataFile());
                return;
            }
            if (lastCommandParsed.equals("clear")) {
                try {
                    Files.delete(USER_DATA.toPath());
                    createNewUSER_DATAFile();
                    TextEngine.printWithDelays("User data file cleared.", false, true);
                    return;
                } catch (IOException e) {
                    TextEngine.printWithDelays("An error occurred while clearing the user data file.", false, true);
                    return;
                }
            }
            TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
            return;
        }
        if (lastCommandParsed.equals("help")) {
            System.out.println("Commands: ");
            System.out.println("startup: add [ARGS], remove [ARGS], clear, enable, disable, list, runall");
            System.out.println("chat: history enable, history disable, history save, history clear, cache enable, cache disable, cache clear");
            System.out.println("text: textspeed [ARGS], textbuffer enable, textbuffer disable, defaultentry ai, defaultentry terminal");
            System.out.println("shortcut: clear, enable, disable, add [ARGS], remove [ARGS], list");
            System.out.println("testing: enable, disable");
            System.out.println("data: get, clear");
            return;
        }
        TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
    }

    /**
     * Processes startup commands.
     */
    private static void startupCommands() {
        getNextCommand();
        if (lastCommandParsed == null) {
            TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
            return;
        }
        if (lastCommandParsed.equals("add")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                return;
            }
            startupCommands.add(lastCommandParsed);
            String commandAdded = lastCommandParsed;
            TextEngine.printWithDelays("Command added to startup commands.", false, true);
            if (startupCommands != null && !startupCommands.isEmpty()) {
                System.out.println("Startup commands:");
                for (String command : startupCommands) {
                    TextEngine.printNoDelay(command, false, true);
                }
            } else {
                System.out.println("No startup commands.");
            }
            getNextCommand();
            if (lastCommandParsed == null) {
                return;
            }
            if (lastCommandParsed.equals("run")) {
                commandParser("." + commandAdded);
                return;
            }
            System.out.println("Unknown command. No given ARGS. Try 'help'");
        }
        if (lastCommandParsed.equals("remove")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                return;
            }
            startupCommands.remove(lastCommandParsed);
            TextEngine.printWithDelays("Command removed from startup commands.", false, true);
            if (startupCommands != null && !startupCommands.isEmpty()) {
                System.out.println("Startup commands:");
                for (String command : startupCommands) {
                    TextEngine.printNoDelay(command, false, true);
                }
            } else {
                System.out.println("No startup commands.");
            }
            getNextCommand();
            if (lastCommandParsed == null) {
                return;
            }
        }
        if (lastCommandParsed.equals("clear")) {
            startupCommands = new ArrayList<>();
            TextEngine.printWithDelays("Startup commands cleared.", false, true);
            if (startupCommands != null && !startupCommands.isEmpty()) {
                System.out.println("Startup commands:");
                for (String command : startupCommands) {
                    TextEngine.printNoDelay(command, false, true);
                }
            } else {
                System.out.println("No startup commands.");
            }
            getNextCommand();
            if (lastCommandParsed == null) {
                return;
            }
        }
        if (lastCommandParsed.equals("enable")) {
            startCommandsOn = true;
            TextEngine.printWithDelays("Startup commands enabled.", false, true);
            getNextCommand();
            if (lastCommandParsed == null) {
                return;
            }
        }
        if (lastCommandParsed.equals("disable")) {
            startCommandsOn = false;
            TextEngine.printWithDelays("Startup commands disabled.", false, true);
            getNextCommand();
            if (lastCommandParsed == null) {
                return;
            }
        }
        if (lastCommandParsed.equals("list")) {
            if (startupCommands != null && !startupCommands.isEmpty()) {
                System.out.println("Startup commands:");
                for (String command : startupCommands) {
                    TextEngine.printNoDelay(command, false, true);
                }
            } else {
                System.out.println("No startup commands.");
            }
            return;
        }
        if (lastCommandParsed.equals("runall")) {
            if (startupCommands != null && !startupCommands.isEmpty()) {
                System.out.println("Running startup commands...");
                for (String command : startupCommands) {
                    commandParser("." + command);
                }
            } else {
                System.out.println("No startup commands.");
            }
            return;
        }
        if (lastCommandParsed.equals("help")) {
            System.out.println("Commands:");
            System.out.println("add [ARGS], remove [ARGS], clear, enable, disable, list, runall");
        }
    }

    /**
     * Processes AI chat commands.
     */
    private static void aiChatCommands() {
        getNextCommand();
        if (lastCommandParsed == null) {
            TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
            return;
        }
        if (lastCommandParsed.equals("history")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                return;
            }
            if (lastCommandParsed.equals("disable")) {
                incognitoChatMode = true;
                savedChatCache = new ArrayList<>();
                openAIPromptEngine.setChatCache(savedChatCache);
                TextEngine.printNoDelay("Incognito mode enabled.", false, true);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
            if (lastCommandParsed.equals("enable")) {
                incognitoChatMode = false;
                TextEngine.printNoDelay("Incognito mode disabled.", false, true);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
            if (lastCommandParsed.equals("save")) {
                savedChatCache = openAIPromptEngine.getChatCache();
                TextEngine.printWithDelays("Chat history saved.", false, true);
                return;
            }
            if (lastCommandParsed.equals("clear")) {
                openAIPromptEngine.clearChatCache();
                savedChatCache = new ArrayList<>();
                TextEngine.printWithDelays("Chat history cleared.", false, true);
                return;
            }
        }
        if (lastCommandParsed.equals("cache")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                return;
            }
            if (lastCommandParsed.equals("enable")) {
                usingChatCache = true;
                TextEngine.printWithDelays("Chat cache enabled.", false, true);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
            if (lastCommandParsed.equals("disable")) {
                usingChatCache = false;
                TextEngine.printWithDelays("Chat cache disabled.", false, true);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
            if (lastCommandParsed.equals("clear")) {
                openAIPromptEngine.clearChatCache();
                savedChatCache = new ArrayList<>();
                TextEngine.printWithDelays("Chat history cleared.", false, true);
                return;
            }
        }
        if (lastCommandParsed.equals("help")) {
            System.out.println("Commands:");
            System.out.println("history: disable, enable, save, clear");
            System.out.println("cache: enable, disable, clear");
        }
    }

    /**
     * Processes shortcut commands.
     */
    private static void shortcutCommands() {
        getNextCommand();
        if (lastCommandParsed == null) {
            TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
            return;
        }
        if (lastCommandParsed.equals("clear")) {
            shortcuts = new HashMap<>();
            TextEngine.printWithDelays("Shortcuts cleared.", false, true);
            getNextCommand();
            if (lastCommandParsed == null) {
                return;
            }
        }
        if (lastCommandParsed.equals("enable")) {
            shotcutsEnabled = true;
            TextEngine.printWithDelays("Shortcuts enabled.", false, true);
            getNextCommand();
            if (lastCommandParsed == null) {
                return;
            }
        }
        if (lastCommandParsed.equals("disable")) {
            shotcutsEnabled = false;
            TextEngine.printWithDelays("Shortcuts disabled.", false, true);
            getNextCommand();
            if (lastCommandParsed == null) {
                return;
            }
        }
        if (lastCommandParsed.equals("add")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                return;
            }
            String shortcut = lastCommandParsed;
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                return;
            }
            String command = lastCommandParsed;
            shortcuts.put(shortcut, command);
            TextEngine.printWithDelays("Shortcut added.", false, true);
            getNextCommand();
            if (lastCommandParsed == null) {
                return;
            }
        }
        if (lastCommandParsed.equals("remove")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                return;
            }
            shortcuts.remove(lastCommandParsed);
            TextEngine.printWithDelays("Shortcut removed.", false, true);
            getNextCommand();
            if (lastCommandParsed == null) {
                return;
            }
        }
        if (lastCommandParsed.equals("list")) {
            if (shortcuts != null && !shortcuts.isEmpty()) {
                System.out.println("Shortcuts:");
                for (Map.Entry<String, String> entry : shortcuts.entrySet()) {
                    TextEngine.printNoDelay(entry.getKey() + " = " + entry.getValue(), false, true);
                }
            } else {
                System.out.println("No shortcuts.");
            }
            return;
        }
        if (lastCommandParsed.equals("help")) {
            System.out.println("Commands:");
            System.out.println("clear, enable, disable, add [ARGS], remove [ARGS], list");
        }
    }

    /**
     * Processes text commands.
     */
    private static void textCommands() {
        getNextCommand();
        if (lastCommandParsed == null) {
            TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
            return;
        }
        if (lastCommandParsed.equals("textspeed")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                return;
            }
            TextEngine.setSpeedSetting(lastCommandParsed);
            System.out.println("Text speed set to " + TextEngine.getSpeedSetting());
            getNextCommand();
            if (lastCommandParsed == null) {
                return;
            }
        }
        if (lastCommandParsed.equals("textbuffer")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                return;
            }
            if (lastCommandParsed.equals("enable")) {
                textBuffer = true;
                TextEngine.printWithDelays("Text buffer enabled.", false, true);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
            if (lastCommandParsed.equals("disable")) {
                textBuffer = false;
                TextEngine.printWithDelays("Text buffer disabled.", false, true);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
        }
        if (lastCommandParsed.equals("defaultentry")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                return;
            }
            if (lastCommandParsed.equals("ai")) {
                System.out.println("Default text entry set to AI.");
                defaultTextEntryOnAI = true;
                return;
            }
            if (lastCommandParsed.equals("terminal")) {
                System.out.println("Default text entry set to terminal.");
                defaultTextEntryOnAI = false;
                return;
            }
            System.out.println("Unknown command. No given ARGS. Try 'help'");
            return;
        }
        if (lastCommandParsed.equals("displayfullfilepath")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false, true);
                return;
            }
            if (lastCommandParsed.equals("enable")) {
                terminal.setDisplayWholePath(true);
                TextEngine.printWithDelays("Displaying full file path enabled.", false, true);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
            if (lastCommandParsed.equals("disable")) {
                terminal.setDisplayWholePath(false);
                TextEngine.printWithDelays("Displaying full file path disabled.", false, true);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
        }
        if (lastCommandParsed.equals("help")) {
            System.out.println("Commands:");
            System.out.println("textspeed [ARGS], textbuffer [ARGS], defaultentry [ARGS], displayfullfilepath [ARGS]");
        }
    }

    /**
     * Processes a chat message.
     *
     * @param message The chat message to process
     */
    private static void chatProcess(String message) {
        System.out.println();
        if (message == null || message.isEmpty()) {
            TextEngine.printWithDelays(AI_CHAT_HEADER + "Invalid input. Please try again.", false, true);
            return;
        }
        if (openAIPromptEngine.getAPIKey() == null || openAIPromptEngine.getAPIKey().isEmpty()) {
            TextEngine.printWithDelays(AI_CHAT_HEADER + "There is no OpenAPI key set.", false, true);
            return;
        }
        String response = openAIPromptEngine.buildPromptAndReturnResponce(message, usingChatCache);
        TextEngine.printWithDelays(GREEN_COLOR_BOLD + "ChatGPT: " + RESET_COLOR + response, false, true);
        System.out.println();
    }

    /**
     * Retrieves the next command from the command queue.
     */
    private static void getNextCommand() {
        if (!commandsQueue.isEmpty()) {
            lastCommandParsed = commandsQueue.poll();
            if (TESTING) {
                System.out.println("Processed Command: " + lastCommandParsed);
            }
        } else {
            lastCommandParsed = null;
        }
    }

    /**
     * Exits the application, optionally saving chat history.
     */
    private static void exit() {
        if (!incognitoChatMode) {
            if (!openAIPromptEngine.getChatCache().isEmpty()) {
                TextEngine.printWithDelays(AI_CHAT_HEADER + "Would you like to save the chat history? 'y' or 'n'", textBuffer, false);
                if ("y".equals(console.readLine().trim().toLowerCase())) {
                    savedChatCache = openAIPromptEngine.getChatCache();
                    TextEngine.printWithDelays(AI_CHAT_HEADER + "Chat history saved.", false, true);
                } else {
                    openAIPromptEngine.clearChatCache();
                    savedChatCache = new ArrayList<>();
                    TextEngine.printWithDelays(AI_CHAT_HEADER + "Chat history cleared.", false, true);
                }
            }
        } else {
            openAIPromptEngine.clearChatCache();
            savedChatCache = new ArrayList<>();
        }
        writeUserData();
        TextEngine.printWithDelays("Exiting...", false, true);
        TextEngine.clearScreen();
        System.exit(0);
    }

    /**
     * Displays the chat history.
     */
    private static void showChatHistory() {
        if (!openAIPromptEngine.getChatCache().isEmpty()) {
            System.out.println();
            System.out.println("Chat history:");
            System.out.println();
            for (String message : openAIPromptEngine.getChatCache()) {
                TextEngine.printNoDelay(message, false, true);
                System.out.println();
            }
        }
    }

    /**
     * Extracts the code snippet from a logged chat file and saves it to a new
     * file with the correct file extension.
     *
     * @param logFile The log file containing the chat
     * @param outputDir The directory to save the extracted code file
     */
    private static File extractCodeSnippet(File logFile) {
        try {
            List<String> lines = Files.readAllLines(logFile.toPath());
            StringBuilder codeSnippet = new StringBuilder();
            String fileExtension = null;
            boolean inCodeBlock = false;
            for (String line : lines) {
                if (line.startsWith("```")) {
                    if (inCodeBlock) {
                        break;
                    } else {
                        inCodeBlock = true;
                        String language = line.substring(3).trim();
                        fileExtension = getFileExtensionForLanguage(language);
                    }
                } else if (inCodeBlock) {
                    codeSnippet.append(line).append(System.lineSeparator());
                }
            }
            if (fileExtension != null && !codeSnippet.toString().isEmpty()) {
                File outputFile = new File("extracted_code." + fileExtension);
                try (FileWriter writer = new FileWriter(outputFile)) {
                    writer.write(codeSnippet.toString());
                    System.out.println("Code snippet extracted and saved to " + outputFile.getAbsolutePath());
                    return outputFile;
                }
            } else {
                System.out.println("No code snippet found in the log file.");
            }
        } catch (IOException e) {
            System.err.println("An error occurred while extracting the code snippet: " + e.getMessage());
        }
        return null;
    }

    private static String getFileExtensionForLanguage(String language) {
        return switch (language.toLowerCase()) {
            case "java" ->
                "java";
            case "python" ->
                "py";
            case "javascript" ->
                "js";
            case "typescript" ->
                "ts";
            case "csharp" ->
                "cs";
            case "cpp" ->
                "cpp";
            case "c" ->
                "c";
            case "html" ->
                "html";
            case "css" ->
                "css";
            case "json" ->
                "json";
            case "xml" ->
                "xml";
            default ->
                "txt";
        };
    }
}
