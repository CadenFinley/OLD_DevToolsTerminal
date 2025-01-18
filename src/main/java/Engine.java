
import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
    private static List<String> savedChatCache = new ArrayList<>();

    private static WeatherAPIPromptEngine weatherAPIPromptEngine;
    private static OpenAIPromptEngine openAIPromptEngine;
    private static TimeEngine clockEngine;
    private static final TerminalPassthrough terminal = new TerminalPassthrough();
    private static final Engine ENGINE_SERVICE = new Engine();

    private static boolean weatherRefresh = true;
    private static boolean locationOn = false;
    private static Queue<String> commandsQueue = null;
    private static String lastCommandParsed = null;
    private static boolean showChatHistoryOnLaunch = true;
    private static boolean defaultTextEntryOnAI = false;

    private static final String GREEN_COLOR_BOLD = "\033[1;32m";
    private static final String RED_COLOR_BOLD = "\033[1;31m";
    private static final String RESET_COLOR = "\033[0m";
    private static final String MAIN_MENU_HEADER = GREEN_COLOR_BOLD + "Main Menu: " + RESET_COLOR;
    private static final String TERMINAL_HEADER = RED_COLOR_BOLD + "Terminal: " + RESET_COLOR;
    private static final String AI_CHAT_HEADER = GREEN_COLOR_BOLD + "AI Chat: " + RESET_COLOR;

    private static final File USER_DATA = new File("userData.json");

    private static List<String> startupCommands;

    public static void main(String[] args) {
        TextEngine.clearScreen();
        TextEngine.printWithDelays("Loading...", false);
        TextEngine.setWidth();
        weatherAPIPromptEngine = new WeatherAPIPromptEngine();
        openAIPromptEngine = new OpenAIPromptEngine();
        clockEngine = new TimeEngine("timer", ENGINE_SERVICE);
        if (!USER_DATA.exists()) {
            try {
                System.out.println("User data file not found. Creating new file...");
                USER_DATA.createNewFile();
                writeUserData();
            } catch (IOException e) {
                TextEngine.printWithDelays(MAIN_MENU_HEADER + "An error occurred while creating the user data file.", false);
            }
        } else {
            loadUserData();
        }
        if (openAIPromptEngine.getAPIKey() == null || openAIPromptEngine.getAPIKey().isEmpty()) {
            System.out.println("OpenAI API key not found.");
            defaultTextEntryOnAI = false;
        } else {
            if (openAIPromptEngine.testAPIKey(openAIPromptEngine.getAPIKey())) {
                defaultTextEntryOnAI = true;
                TextEngine.printWithDelays(AI_CHAT_HEADER + "Successfully Connected to OpenAI servers!", false);
            } else {
                TextEngine.printWithDelays(AI_CHAT_HEADER + "An error occurred while connecting to OpenAI servers.", false);
                TextEngine.printWithDelays(AI_CHAT_HEADER + "Please check your internet connection and try again later.", false);
                defaultTextEntryOnAI = false;
            }
        }
        if (startupCommands != null && !startupCommands.isEmpty() && startCommandsOn) {
            System.out.println(MAIN_MENU_HEADER + "Running startup commands...");
            for (String command : startupCommands) {
                commandParser("." + command);
            }
        }
        if (!openAIPromptEngine.getChatCache().isEmpty() && showChatHistoryOnLaunch && defaultTextEntryOnAI) {
            System.out.println();
            System.out.println("Chat history:");
            System.out.println();
            for (String message : openAIPromptEngine.getChatCache()) {
                TextEngine.printNoDelay(message, false);
                System.out.println();
            }
        }
        while (true) {
            if (weatherRefresh && locationOn) {
                ENGINE_SERVICE.weatherListener();
            }
            if (TESTING) {
                System.out.println("Testing mode is enabled.");
            }
            if (defaultTextEntryOnAI) {
                TextEngine.printNoDelay(MAIN_MENU_HEADER, true);
            } else {
                TextEngine.printNoDelay(terminal.returnCurrentTerminalPosition(), true);
            }
            String command = console.readLine();
            commandParser(command);
        }
    }

    private static void loadUserData() {
        try {
            JSONObject userData = new JSONObject(Files.readString(USER_DATA.toPath()));
            openAIPromptEngine.setAPIKey(userData.getString("OpenAI_API_KEY"));
            locationOn = userData.getBoolean("Location_Enable");
            weatherAPIPromptEngine.setLatitude(userData.getString("latitude"));
            weatherAPIPromptEngine.setLongitude(userData.getString("longitude"));
            savedChatCache = new ArrayList<>();
            userData.getJSONArray("Chat_Cache").forEach(item -> savedChatCache.add((String) item));
            openAIPromptEngine.setChatCache(savedChatCache);
            startupCommands = new ArrayList<>();
            userData.getJSONArray("Startup_Commands").forEach(item -> startupCommands.add((String) item));
            TextEngine.setSpeedSetting(userData.getString("Text_Speed"));
        } catch (IOException e) {
            TextEngine.printWithDelays("An error occurred while reading the user data file.", false);
        }
    }

    private static void writeUserData() {
        try (FileWriter file = new FileWriter(USER_DATA)) {
            JSONObject userData = new JSONObject();
            userData.put("OpenAI_API_KEY", openAIPromptEngine.getAPIKey());
            userData.put("latitude", weatherAPIPromptEngine.getLatitude());
            userData.put("longitude", weatherAPIPromptEngine.getLongitude());
            userData.put("Location_Enable", locationOn);
            userData.put("Chat_Cache", savedChatCache);
            userData.put("Startup_Commands", startupCommands);
            userData.put("Text_Speed", TextEngine.getSpeedSetting());
            file.write(userData.toString());
            file.flush();
        } catch (IOException e) {
            TextEngine.printWithDelays("An error occurred while writing to the user data file.", false);
        }
    }

    private static String readAndReturnUserDataFile() {
        try {
            String userData = Files.readString(USER_DATA.toPath());
            if (userData == null || userData.isEmpty()) {
                return "No data found.";
            }
            return userData;
        } catch (IOException e) {
            TextEngine.printWithDelays("An error occurred while reading the user data file.", false);
            return null;
        }
    }

    private static String[] commandSplicer(String command) {
        int numberOfWordsInSeparates = 0;
        String[] commands = command.split(" ");
        //recombine sets of words in quotes and parentheses
        for (int i = 0; i < commands.length; i++) {
            if (commands[i].startsWith("'") || commands[i].startsWith("(")) {
                //remove the starting quote or parenthesis
                commands[i] = commands[i].substring(1);
                StringBuilder combined = new StringBuilder(commands[i]);
                while (!commands[i].endsWith("'") && !commands[i].endsWith(")")) {
                    i++;
                    numberOfWordsInSeparates++;
                    if (i >= commands.length) {
                        break;
                    }
                    combined.append(" ").append(commands[i]);
                }
                //remove the ending quote or parenthesis
                combined.deleteCharAt(combined.length() - 1);
                commands[i] = combined.toString();
            }
            commands = Arrays.stream(commands).filter(s -> !s.equals("'") && !s.equals("(") && !s.equals(")")).toArray(String[]::new);
        }
        //remove the previous n-1 words from the array
        int index = commands.length - 2;
        String bufferCommand = commands[commands.length - 1];
        for (int i = 1; i < numberOfWordsInSeparates; i++) {
            commands = Arrays.copyOf(commands, index);
            index--;
        }
        commands[commands.length - 1] = bufferCommand;
        return commands;
    }

    private static void commandParser(String command) {
        if (command == null || command.isEmpty()) {
            TextEngine.printWithDelays("Invalid input. Please try again.", false);
            return;
        }
        if (command.equals("exit")) {
            commandProcesser("exit");
            return;
        }
        if (command.equals("help")) {
            commandProcesser("help");
            return;
        }
        if (command.startsWith(".")) {
            commandProcesser(command.substring(1));
            return;
        }
        if (defaultTextEntryOnAI) {
            chatProcess(command);
        } else {
            Thread commandThread = terminal.executeCommand(command, true);
            try {
                commandThread.join(); // Wait for the thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread Interrupted");
            }
            System.out.println();
        }
    }

    private static void commandProcesser(String command) {
        commandsQueue = new LinkedList<>();
        commandsQueue.addAll(Arrays.asList(commandSplicer(command)));
        if (TESTING) {
            System.out.println(commandsQueue);
        }
        if (commandsQueue.isEmpty()) {
            TextEngine.printWithDelays("Unknown command. Please try again.", false);
        }
        getNextCommand();
        switch (lastCommandParsed) {
            case "weather" -> {
                weatherSettingsCommands();
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
                } catch (StringIndexOutOfBoundsException e) {
                    defaultTextEntryOnAI = false;
                    return;
                }
                terminal.executeCommand(strippedCommand, true);
            }
            case "exit" ->
                exit();
            case "help" -> {
                TextEngine.printWithDelays("Commands:", false);
                TextEngine.printWithDelays(".weather", false);
                TextEngine.printWithDelays(".ai", false);
                TextEngine.printWithDelays(".user", false);
                TextEngine.printWithDelays(".exit", false);
                TextEngine.printWithDelays(".help", false);
            }
            default ->
                TextEngine.printWithDelays("Unknown command. Please try again. Type 'help' or '.help' if you need help", false);
        }
    }

    private static void weatherSettingsCommands() {
        getNextCommand();
        if (lastCommandParsed == null) {
            TextEngine.printWithDelays("Unknown command. No given ARGS.", false);
            return;
        }
        if (lastCommandParsed.equals("refresh")) {
            weatherAPIPromptEngine.refreshWeather();
            return;
        }
        if (lastCommandParsed.equals("get")) {
            if (!locationOn) {
                TextEngine.printWithDelays("Location based services are disabled.", false);
                return;
            }
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                return;
            }
            String recievedWeatherData = weatherAPIPromptEngine.getWeatherDataPart(lastCommandParsed);
            if (recievedWeatherData == null || recievedWeatherData.isEmpty() || recievedWeatherData.equals("No weather data available.") || lastCommandParsed.equals("all")) {
                TextEngine.printWithDelays(recievedWeatherData, false);
                return;
            }
            TextEngine.printWithDelays("The current " + lastCommandParsed + " is " + recievedWeatherData, false);
            return;
        }
        if (lastCommandParsed.equals("autorefresh")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                return;
            }
            if (lastCommandParsed.equals("enable")) {
                weatherRefresh = true;
                TextEngine.printWithDelays("Weather auto-refresh enabled.", false);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
            if (lastCommandParsed.equals("disable")) {
                weatherRefresh = false;
                TextEngine.printWithDelays("Weather auto-refresh disabled.", false);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
        }
        if (lastCommandParsed.equals("change")) {
            getNextCommand();
            if (lastCommandParsed.equals("location")) {
                getNextCommand();
                if (lastCommandParsed == null) {
                    TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                    return;
                }
                weatherAPIPromptEngine.setLatitude(lastCommandParsed);
                getNextCommand();
                if (lastCommandParsed == null) {
                    TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                    return;
                }
                weatherAPIPromptEngine.setLongitude(lastCommandParsed);
                TextEngine.printWithDelays("Location successfully set. Latitude: " + weatherAPIPromptEngine.getLatitude() + " Longetude: " + weatherAPIPromptEngine.getLongitude(), false);
            }
            TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
        }
        if (lastCommandParsed.equals("help")) {
            System.out.println("Commands: ");
            System.out.println("refresh");
            System.out.println("get: [ARGS]");
            System.out.println("autorefresh: enable, disable");
            System.out.println("change: location [ARGS] [ARGS]");
            return;
        }
        TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
    }

    private static void aiSettingsCommands() {
        getNextCommand();
        if (lastCommandParsed == null) {
            defaultTextEntryOnAI = true;
            return;
        }
        if (lastCommandParsed.equals("apikey")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                return;
            }
            if (lastCommandParsed.equals("set")) {
                getNextCommand();
                if (lastCommandParsed == null) {
                    TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                    return;
                }
                openAIPromptEngine.setAPIKey(lastCommandParsed);
                if (openAIPromptEngine.testAPIKey(openAIPromptEngine.getAPIKey())) {
                    System.out.println("OpenAI API key set.");
                    return;
                } else {
                    TextEngine.printWithDelays("Invalid API key. AI services have been disabled", false);
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
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                return;
            }
            TextEngine.printWithDelays(clockEngine.timeStamp() + " Sent message to GPT: " + lastCommandParsed, false);
            chatProcess(lastCommandParsed);
            return;
        }
        if (lastCommandParsed.equals("get")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
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
            return;
        }
        TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
    }

    private static void userSettingsCommands() {
        getNextCommand();
        if (lastCommandParsed == null) {
            TextEngine.printWithDelays("Unknown command. No given ARGS.", false);
            return;
        }
        if (lastCommandParsed.equals("startup")) {
            if (startupCommands != null && !startupCommands.isEmpty()) {
                System.out.println("Startup commands:");
                for (String command : startupCommands) {
                    TextEngine.printNoDelay(command, false);
                }
            } else {
                System.out.println("No startup commands.");
            }
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                return;
            }
            if (lastCommandParsed.equals("add")) {
                getNextCommand();
                if (lastCommandParsed == null) {
                    TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                    return;
                }
                startupCommands.add(lastCommandParsed);
                String commandAdded = lastCommandParsed;
                TextEngine.printWithDelays("Command added to startup commands.", false);
                if (startupCommands != null && !startupCommands.isEmpty()) {
                    System.out.println("Startup commands:");
                    for (String command : startupCommands) {
                        TextEngine.printNoDelay(command, false);
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
                    TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                    return;
                }
                startupCommands.remove(lastCommandParsed);
                TextEngine.printWithDelays("Command removed from startup commands.", false);
                if (startupCommands != null && !startupCommands.isEmpty()) {
                    System.out.println("Startup commands:");
                    for (String command : startupCommands) {
                        TextEngine.printNoDelay(command, false);
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
                TextEngine.printWithDelays("Startup commands cleared.", false);
                if (startupCommands != null && !startupCommands.isEmpty()) {
                    System.out.println("Startup commands:");
                    for (String command : startupCommands) {
                        TextEngine.printNoDelay(command, false);
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
                TextEngine.printWithDelays("Startup commands enabled.", false);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
            if (lastCommandParsed.equals("disable")) {
                startCommandsOn = false;
                TextEngine.printWithDelays("Startup commands disabled.", false);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
            if (lastCommandParsed.equals("list")) {
                if (startupCommands != null && !startupCommands.isEmpty()) {
                    System.out.println("Startup commands:");
                    for (String command : startupCommands) {
                        TextEngine.printNoDelay(command, false);
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
            TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
        }
        if (lastCommandParsed.equals("data")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                return;
            }
            if (lastCommandParsed.equals("get")) {
                System.out.println(readAndReturnUserDataFile());
                return;
            }
            System.out.println("Unknown command. No given ARGS. Try 'help'");
        }
        if (lastCommandParsed.equals("location")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                return;
            }
            if (lastCommandParsed.equals("enable")) {
                locationOn = true;
                TextEngine.printWithDelays("Location based services have been enabled.", false);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
            if (lastCommandParsed.equals("disable")) {
                locationOn = false;
                TextEngine.printWithDelays("Location based services have been disabled.", false);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
            if (lastCommandParsed.equals("set")) {
                getNextCommand();
                if (lastCommandParsed == null) {
                    TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                    return;
                }
                weatherAPIPromptEngine.setLatitude(lastCommandParsed);
                getNextCommand();
                if (lastCommandParsed == null) {
                    TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                    return;
                }
                weatherAPIPromptEngine.setLongitude(lastCommandParsed);
                weatherAPIPromptEngine.refreshWeather();
                TextEngine.printWithDelays("Location successfully set. Latitude: " + weatherAPIPromptEngine.getLatitude() + " Longetude: " + weatherAPIPromptEngine.getLongitude(), false);
                return;
            }
            TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
        }
        if (lastCommandParsed.equals("chat")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                return;
            }
            if (lastCommandParsed.equals("history")) {
                getNextCommand();
                if (lastCommandParsed == null) {
                    TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                    return;
                }
                if (lastCommandParsed.equals("disable")) {
                    incognitoChatMode = true;
                    savedChatCache = new ArrayList<>();
                    openAIPromptEngine.setChatCache(savedChatCache);
                    TextEngine.printNoDelay("Incognito mode enabled.", false);
                    getNextCommand();
                    if (lastCommandParsed == null) {
                        return;
                    }
                }
                if (lastCommandParsed.equals("enable")) {
                    incognitoChatMode = false;
                    TextEngine.printNoDelay("Incognito mode disabled.", false);
                    getNextCommand();
                    if (lastCommandParsed == null) {
                        return;
                    }
                }
                if (lastCommandParsed.equals("save")) {
                    savedChatCache = openAIPromptEngine.getChatCache();
                    TextEngine.printWithDelays("Chat history saved.", false);
                    return;
                }
                if (lastCommandParsed.equals("clear")) {
                    openAIPromptEngine.clearChatCache();
                    savedChatCache = new ArrayList<>();
                    TextEngine.printWithDelays("Chat history cleared.", false);
                    return;
                }
                if (lastCommandParsed.equals("showonlaunch")) {
                    getNextCommand();
                    if (lastCommandParsed == null) {
                        TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                        return;
                    }
                    if (lastCommandParsed.equals("enable")) {
                        showChatHistoryOnLaunch = true;
                        TextEngine.printWithDelays("Chat history will be shown on launch.", false);
                        getNextCommand();
                        if (lastCommandParsed == null) {
                            return;
                        }
                    }
                    if (lastCommandParsed.equals("disable")) {
                        showChatHistoryOnLaunch = false;
                        TextEngine.printWithDelays("Chat history will not be shown on launch.", false);
                        getNextCommand();
                        if (lastCommandParsed == null) {
                            return;
                        }
                    }
                    System.out.println("Unknown command. No given ARGS. Try 'help'");
                }
                System.out.println("Unknown command. No given ARGS. Try 'help'");
            }
            if (lastCommandParsed.equals("cache")) {
                getNextCommand();
                if (lastCommandParsed == null) {
                    TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                    return;
                }
                if (lastCommandParsed.equals("enable")) {
                    openAIPromptEngine.setUseCache(true);
                    TextEngine.printWithDelays("Chat cache enabled.", false);
                    getNextCommand();
                    if (lastCommandParsed == null) {
                        return;
                    }
                }
                if (lastCommandParsed.equals("disable")) {
                    openAIPromptEngine.setUseCache(false);
                    TextEngine.printWithDelays("Chat cache disabled.", false);
                    getNextCommand();
                    if (lastCommandParsed == null) {
                        return;
                    }
                }
                if (lastCommandParsed.equals("clear")) {
                    openAIPromptEngine.clearChatCache();
                    savedChatCache = new ArrayList<>();
                    TextEngine.printWithDelays("Chat history cleared.", false);
                    return;
                }
            }
            TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
        }
        if (lastCommandParsed.equals("testing")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                return;
            }
            if (lastCommandParsed.equals("enable")) {
                TESTING = true;
                TextEngine.printWithDelays("Testing mode enabled.", false);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
            if (lastCommandParsed.equals("disable")) {
                TESTING = false;
                TextEngine.printWithDelays("Testing mode disabled.", false);
                getNextCommand();
                if (lastCommandParsed == null) {
                    return;
                }
            }
        }
        if (lastCommandParsed.equals("textspeed")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                return;
            }
            TextEngine.setSpeedSetting(lastCommandParsed);
            System.out.println("Text speed set to " + TextEngine.getSpeedSetting());
            return;
        }
        if (lastCommandParsed.equals("defaultentry")) {
            getNextCommand();
            if (lastCommandParsed == null) {
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
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
        }
        if (lastCommandParsed.equals("help")) {
            System.out.println("Commands: ");
            System.out.println("startup: add [ARGS] [ARGS], remove [ARGS], clear, enable, disable, list, runall");
            System.out.println("data: get");
            System.out.println("location: enable, disable, set [ARGS] [ARGS]");
            System.out.println("chat: history, cache, testing, textspeed");
            System.out.println("defaultentry: ai, terminal");
            return;
        }
        TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
    }

    private static void chatProcess(String message) {
        System.out.println();
        if (message == null || message.isEmpty()) {
            TextEngine.printWithDelays(AI_CHAT_HEADER + "Invalid input. Please try again.", false);
            return;
        }
        if (openAIPromptEngine.getAPIKey() == null || openAIPromptEngine.getAPIKey().isEmpty()) {
            TextEngine.printWithDelays(AI_CHAT_HEADER + "There is no OpenAPI key set.", false);
            return;
        }
        String response = openAIPromptEngine.buildPromptAndReturnResponce(message);
        TextEngine.printWithDelays(GREEN_COLOR_BOLD + "ChatGPT: " + RESET_COLOR + response, false);
        System.out.println();
    }

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

    private static void exit() {
        if (!incognitoChatMode) {
            if (!openAIPromptEngine.getChatCache().isEmpty()) {
                TextEngine.printWithDelays(AI_CHAT_HEADER + "Would you like to save the chat history? 'y' or 'n'", true);
                if ("y".equals(console.readLine().trim().toLowerCase())) {
                    savedChatCache = openAIPromptEngine.getChatCache();
                    TextEngine.printWithDelays(AI_CHAT_HEADER + "Chat history saved.", false);
                } else {
                    openAIPromptEngine.clearChatCache();
                    savedChatCache = new ArrayList<>();
                    TextEngine.printWithDelays(AI_CHAT_HEADER + "Chat history cleared.", false);
                }
            }
        } else {
            openAIPromptEngine.clearChatCache();
            savedChatCache = new ArrayList<>();
        }
        writeUserData();
        TextEngine.printWithDelays("Exiting...", false);
        TextEngine.clearScreen();
        System.exit(0);
    }

    private void weatherListener() {
        clockEngine = new TimeEngine("timer", ENGINE_SERVICE);
        if (!weatherRefresh) {
            return;
        }
        clockEngine.startClock(60 * 15);
        new Thread(() -> {
            while (clockEngine.isRunning()) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Thread Interrupted");
                }
            }
            clockEngine.stopClock();
            weatherAPIPromptEngine.refreshWeather();
            weatherListener();
        }).start();
    }
}
