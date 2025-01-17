
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
    private static boolean startupMode = false;
    private static boolean incognitoChatMode = false;
    private static List<String> savedChatCache = new ArrayList<>();

    private static WeatherAPIPromptEngine weatherAPIPromptEngine;
    private static OpenAIPromptEngine openAIPromptEngine;
    private static TimeEngine clockEngine;

    private static final Engine ENGINE_SERVICE = new Engine();
    private static boolean weatherRefresh = true;
    private static boolean locationOn = false;
    private static Queue<String> commandsQueue = null;
    private static String lastCommandParsed = null;

    private static final String GREEN_COLOR_BOLD = "\033[1;32m";
    private static final String RESET_COLOR = "\033[0m";
    private static final String MAIN_MENU_HEADER = GREEN_COLOR_BOLD + "Main Menu: " + RESET_COLOR;
    private static final String AI_CHAT_HEADER = GREEN_COLOR_BOLD + "AI Chat: " + RESET_COLOR;

    private static final File USER_DATA = new File("userData.json");

    private static List<String> startupCommands;

    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static void main(String[] args) {
        TextEngine.clearScreen();
        TextEngine.printWithDelays("Loading...", false);
        TextEngine.setWidth();
        weatherAPIPromptEngine = new WeatherAPIPromptEngine();
        openAIPromptEngine = new OpenAIPromptEngine();
        clockEngine = new TimeEngine("timer", ENGINE_SERVICE);
        if (!USER_DATA.exists()) {
            try {
                USER_DATA.createNewFile();
            } catch (IOException e) {
                TextEngine.printWithDelays(MAIN_MENU_HEADER + "An error occurred while creating the user data file.", false);
            }
        } else {
            loadUserData();
        }
        if (OpenAIPromptEngine.testAPIKey(openAIPromptEngine.getAPIKey()) && openAIPromptEngine.getAPIKey() != null) {
            TextEngine.printWithDelays(AI_CHAT_HEADER + "Successfully Connected to OpenAI servers!", false);
        } else {
            TextEngine.printWithDelays(AI_CHAT_HEADER + "An error occurred while connecting to OpenAI servers.", false);
            TextEngine.printWithDelays(AI_CHAT_HEADER + "Please check your internet connection and try again later.", false);
            TextEngine.enterToNext();
            return;
        }
        if (!startupCommands.isEmpty()) {
            startupMode = true;
            System.out.println("Running startup commands...");
            for (String command : startupCommands) {
                commandParser("." + command);
            }
            startupMode = false;
        }
        if (!openAIPromptEngine.getChatCache().isEmpty()) {
            System.out.println("Chat history:");
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
            TextEngine.printWithDelays(MAIN_MENU_HEADER, true);
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

    private static void commandParser(String command) {
        if (command == null || command.isEmpty()) {
            TextEngine.printWithDelays("Invalid input. Please try again.", true);
            return;
        }
        if (command.equals("exit")) {
            commandProcesser("exit");
        }
        if (command.equals("help")) {
            commandProcesser("help");
        }
        if (command.startsWith(".")) {
            commandProcesser(command.substring(1));
            return;
        }
        chatProcess(command);
    }

    private static void commandProcesser(String command) {
        commandsQueue = new LinkedList<>();
        commandsQueue.addAll(Arrays.asList(command.split(" ")));
        if (TESTING) {
            System.out.println(commandsQueue);
        }
        if (commandsQueue.isEmpty()) {
            TextEngine.printWithDelays("Unknown command. Please try again.", false);
        }
        getNextCommand();
        switch (lastCommandParsed) {
            case "weather" -> {
                getNextCommand();
                if (lastCommandParsed == null) {
                    TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                    return;
                }
                if (lastCommandParsed.equals("help")) {
                    TextEngine.printWithDelays("Commands: refresh, get, enable, disable, change", false);
                    return;
                }
                if (lastCommandParsed == null) {
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
                        return;
                    }
                    if (lastCommandParsed.equals("disable")) {
                        weatherRefresh = false;
                        TextEngine.printWithDelays("Weather auto-refresh disabled.", false);
                        return;
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
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
            }
            case "ai" -> {
                getNextCommand();
                if (lastCommandParsed == null) {
                    TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                    return;
                }
                if (lastCommandParsed.equals("help")) {
                    TextEngine.printWithDelays("Commands: chat, enable, disable", false);
                    return;
                }
                if (lastCommandParsed.equals("set")) {
                    getNextCommand();
                    if (lastCommandParsed == null) {
                        TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                        return;
                    }
                    if (lastCommandParsed.equals("apikey")) {
                        getNextCommand();
                        if (lastCommandParsed == null) {
                            TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                            return;
                        }
                        openAIPromptEngine.setAPIKey(lastCommandParsed);
                        if (OpenAIPromptEngine.testAPIKey(openAIPromptEngine.getAPIKey())) {
                            System.out.println("OpenAI API key set.");
                            return;
                        } else {
                            TextEngine.printWithDelays("Invalid API key. AI services have been disabled", false);
                            return;
                        }
                    }
                }
                if (lastCommandParsed == null) {
                    return;
                }
                if (lastCommandParsed.equals("apikey")) {
                    System.out.println(openAIPromptEngine.getAPIKey());
                    getNextCommand();
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
                    if (startupMode) {
                        TextEngine.printWithDelays(clockEngine.timeStamp() + " Sent message to GPT: " + lastCommandParsed, false);
                    }
                    chatProcess(lastCommandParsed);
                    return;
                }
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
            }
            case "user" -> {
                getNextCommand();
                if (lastCommandParsed == null) {
                    TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
                    return;
                }
                if (lastCommandParsed.equals("data")) {
                    System.out.println(readAndReturnUserDataFile());
                    return;
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
                            return;
                        }
                        if (lastCommandParsed.equals("enable")) {
                            incognitoChatMode = false;
                            TextEngine.printNoDelay("Incognito mode disabled.", false);
                            return;
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
                            return;
                        }
                        if (lastCommandParsed.equals("disable")) {
                            openAIPromptEngine.setUseCache(false);
                            TextEngine.printWithDelays("Chat cache disabled.", false);
                            return;
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
                TextEngine.printWithDelays("Unknown command. No given ARGS. Try 'help'", false);
            }
            case "exit" ->
                exit();
            case "help" ->
                TextEngine.printWithDelays("Commands: weather, ai, exit, help", false);
            default ->
                TextEngine.printWithDelays("Unknown command. Please try again.", false);
        }
    }

    private static void chatProcess(String message) {
        System.out.println();
        if (message == null || message.isEmpty()) {
            TextEngine.printWithDelays(AI_CHAT_HEADER + "Invalid input. Please try again.", false);
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
            weatherAPIPromptEngine.refreshWeather();
            weatherListener();
        }).start();
    }
}
