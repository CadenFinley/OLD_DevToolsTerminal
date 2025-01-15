
import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;

/**
 * @author Caden Finley
 */
public class Engine {

    private static final Console console = System.console();
    public static boolean TESTING = false;
    private static String OpenAI_API_KEY;
    private static WeatherAPIPromptEngine weatherAPIPromptEngine;
    private static OpenAIPromptEngine openAIPromptEngine;
    private static ClockEngine clockEngine;
    private static final Engine ENGINE_SERVICE = new Engine();
    private static boolean weatherRefresh = true;
    private static boolean locationOn = false;
    private static boolean AIon = false;

    private static final File USER_DATA = new File("userData.json");

    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static String getOpenAI_API_KEY() {
        return OpenAI_API_KEY;
    }

    public static void main(String[] args) throws InterruptedException, TimeoutException {
        TextEngine.setWidth();
        if (TESTING) {
            System.out.println("Testing mode is enabled.");
        }
        weatherAPIPromptEngine = new WeatherAPIPromptEngine();
        openAIPromptEngine = new OpenAIPromptEngine(OpenAI_API_KEY, AIon);
        if (!USER_DATA.exists()) {
            firstBoot();
        }
        if (locationOn) {
            ENGINE_SERVICE.weatherListener();
        }
    }

    private static void firstBoot() throws InterruptedException, TimeoutException {
        TextEngine.printWithDelays("Welcome!", false);
        TextEngine.printWithDelays("This is your first time using this program.", false);
        setOpenAiAPIKey();
        setLocation();
        try {
            USER_DATA.createNewFile();
            writeUserData();
        } catch (IOException e) {
            TextEngine.printWithDelays("An error occurred while creating the user data file.", false);
            System.exit(1);
        }
    }

    private static void writeUserData() throws InterruptedException {
        try (FileWriter file = new FileWriter(USER_DATA)) {
            JSONObject userData = new JSONObject();
            userData.put("OpenAI_API_KEY", OpenAI_API_KEY);
            userData.put("latitude", weatherAPIPromptEngine.getLatitude());
            userData.put("longitude", weatherAPIPromptEngine.getLongitude());
            userData.put("Ai_Enabled", AIon);
            userData.put("Location_Enable", locationOn);

            file.write(userData.toString());
            file.flush();
        } catch (IOException e) {
            TextEngine.printWithDelays("An error occurred while writing to the user data file.", false);
        }
    }

    private static void setOpenAiAPIKey() throws InterruptedException, TimeoutException {
        TextEngine.printWithDelays("Please enter your OpenAI API key, or type 'n/a' if you do not have one.", true);
        OpenAI_API_KEY = System.console().readLine();
        if (OpenAI_API_KEY.equals("n/a")) {
            AIon = false;
            return;
        }
        if (OpenAIPromptEngine.testAPIKey(OpenAI_API_KEY)) {
            AIon = true;
            openAIPromptEngine.setAIEnabled(AIon);
        } else {
            setOpenAiAPIKey();
        }
    }

    private static void setLocation() throws InterruptedException {
        String latitude;
        String longitude;
        while (true) {
            TextEngine.printWithDelays("Would you like to turn on location based services? 'y' or 'n'", true);
            if ("y".equals(console.readLine().trim().toLowerCase())) {
                locationOn = true;
                break;
            }
            TextEngine.printWithDelays("Location based services have been turned off", false);
            TextEngine.enterToNext();
            locationOn = false;
            return;
        }
        while (true) {
            TextEngine.printWithDelays("Please enter your latitude.", true);
            latitude = console.readLine();
            TextEngine.printWithDelays("Please enter your longitude.", true);
            longitude = console.readLine();
            if (latitude == null || longitude == null) {
                TextEngine.printWithDelays("Invalid input. Please try again.", true);
            }
            try {
                float lat = Float.parseFloat(latitude);
                float lon = Float.parseFloat(longitude);
                if (lat > 90 || lat < -90 || lon > 180 || lon < -180) {
                    TextEngine.printWithDelays("Invalid input. Please try again.", true);
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                TextEngine.printWithDelays("Invalid input. Please try again.", true);
            }
        }
        weatherAPIPromptEngine.setLatitude(latitude);
        weatherAPIPromptEngine.setLongitude(longitude);
        TextEngine.printWithDelays("Location successfully set. Latitude:" + weatherAPIPromptEngine.getLatitude() + " Longetude: " + weatherAPIPromptEngine.getLongitude(), false);
        TextEngine.enterToNext();
    }

    private void weatherListener() {
        if (!weatherRefresh) {
            return;
        }
        clockEngine = new ClockEngine("timer", ENGINE_SERVICE);
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
            weatherAPIPromptEngine.recallWeather();
            weatherListener();
        }).start();
    }
}
