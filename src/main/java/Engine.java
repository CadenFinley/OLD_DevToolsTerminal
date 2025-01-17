
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
    private static boolean AIon = false;
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
        if (!startupCommands.isEmpty()) {
            startupMode = true;
            System.out.println("Running startup commands...");
            for (String command : startupCommands) {
                commandParser(command);
            }
            startupMode = false;
        }
        TextEngine.clearScreen();
        while (true) {
            if (weatherRefresh && locationOn) {
                ENGINE_SERVICE.weatherListener();
            }
            if (TESTING) {
                System.out.println("Testing mode is enabled.");
            }
            TextEngine.printWithDelays(MAIN_MENU_HEADER + "Please enter a command.", true);
            String command = console.readLine();
            commandParser(command);
        }
    }

    private static void loadUserData() {
        try {
            JSONObject userData = new JSONObject(Files.readString(USER_DATA.toPath()));
            openAIPromptEngine.setAPIKey(userData.getString("OpenAI_API_KEY"));
            AIon = userData.getBoolean("Ai_Enabled");
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
            userData.put("Ai_Enabled", AIon);
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
        if (command == null) {
            TextEngine.printWithDelays("Invalid input. Please try again.", true);
            return;
        }
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
                    TextEngine.printWithDelays(weatherAPIPromptEngine.getWeatherDataPart(lastCommandParsed), false);
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
                if (lastCommandParsed.equals("enable")) {
                    System.out.println("AI services are enabled.");
                    AIon = true;
                    getNextCommand();
                    if (lastCommandParsed == null) {
                        return;
                    }
                    if (openAIPromptEngine.getAPIKey() == null && !lastCommandParsed.equals("set")) {
                        System.out.println("API key not set.");
                        return;
                    }
                }
                if (lastCommandParsed.equals("disable")) {
                    AIon = false;
                    System.out.println("AI services are disabled.");
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
                            AIon = false;
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
                    chatProcess();
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
                        weatherAPIPromptEngine.refreshWeather();
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

    private static void chatProcess() {
        TextEngine.clearScreen();
        if (!AIon) {
            TextEngine.printWithDelays(AI_CHAT_HEADER + "AI services are disabled. Please enable them to use this feature.", false);
            TextEngine.enterToNext();
            return;
        }
        TextEngine.printNoDelay("Loading...", false);
        if (OpenAIPromptEngine.testAPIKey(openAIPromptEngine.getAPIKey())) {
            TextEngine.clearScreen();
            TextEngine.printWithDelays(AI_CHAT_HEADER + "Successfully Connected to OpenAI servers!", false);
            if (!openAIPromptEngine.getChatCache().isEmpty()) {
                System.out.println("Chat history:");
                for (String message : openAIPromptEngine.getChatCache()) {
                    TextEngine.printNoDelay(message, false);
                    System.out.println();
                }
            }
        } else {
            TextEngine.clearScreen();
            AIon = false;
            TextEngine.printWithDelays(AI_CHAT_HEADER + "An error occurred while connecting to OpenAI servers.", false);
            TextEngine.printWithDelays(AI_CHAT_HEADER + "Please check your internet connection and try again later.", false);
            TextEngine.enterToNext();
            return;
        }
        while (true) {
            TextEngine.printWithDelays(AI_CHAT_HEADER, true);
            String message = console.readLine();
            System.out.println();
            if (message == null || message.isEmpty()) {
                TextEngine.printWithDelays(AI_CHAT_HEADER + "Invalid input. Please try again.", false);
                continue;
            }
            if (message.equals("exit")) {
                if (openAIPromptEngine.getChatCache().isEmpty()) {
                    TextEngine.printWithDelays(AI_CHAT_HEADER + "Exiting chat.", false);
                    TextEngine.clearScreen();
                    return;
                }
                TextEngine.printWithDelays(AI_CHAT_HEADER + "Would you like to save the chat history? 'y' or 'n'", true);
                if ("y".equals(console.readLine().trim().toLowerCase())) {
                    savedChatCache = openAIPromptEngine.getChatCache();
                    TextEngine.printWithDelays(AI_CHAT_HEADER + "Chat history saved.", false);
                    TextEngine.clearScreen();
                    return;
                } else {
                    openAIPromptEngine.clearChatCache();
                    savedChatCache = new ArrayList<>();
                    TextEngine.printWithDelays(AI_CHAT_HEADER + "Chat history cleared.", false);
                    TextEngine.clearScreen();
                    return;
                }
            }
            if (message.equals("help")) {
                TextEngine.printWithDelays(AI_CHAT_HEADER + "Commands: exit, help, new chat", false);
                continue;
            }
            if (message.equals("new chat")) {
                chatProcess();
                openAIPromptEngine.clearChatCache();
                savedChatCache = new ArrayList<>();
            }
            String response = openAIPromptEngine.buildPromptAndReturnResponce(message);
            TextEngine.printWithDelays(GREEN_COLOR_BOLD + "ChatGPT: " + RESET_COLOR + response, false);
            System.out.println();
        }
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
        writeUserData();
        TextEngine.printWithDelays("Exiting...", false);
        TextEngine.clearScreen();
        System.exit(0);
    }

    private void weatherListener() {
        if (!weatherRefresh) {
            return;
        }
        clockEngine.startClock(900);
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
