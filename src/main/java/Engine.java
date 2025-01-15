
/**
 * @author Caden Finley
 */
public abstract class Engine {

    public static boolean TESTING = false;
    private static String OpenAI_API_KEY = "sk-z3q9L-Lh39YYmooGmbPNAFlsaDywlFdRB-O1vFB4mYT3BlbkFJSSn6Um-zBw4r7fUB2H6dX3fhiOisNo8PFzy-fdKXwA";
    public static WeatherAPIPromptEngine weatherAPIPromptEngine;
    public static OpenAIPromptEngine openAIPromptEngine;

    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static String getOpenAI_API_KEY() {
        return OpenAI_API_KEY;
    }

    public static void main(String[] args) throws InterruptedException {
        if (TESTING) {
            System.out.println("Testing mode is enabled.");
        }
        weatherAPIPromptEngine = new WeatherAPIPromptEngine();
        openAIPromptEngine = new OpenAIPromptEngine(OpenAI_API_KEY, true);
        beginService();
    }

    private static void beginService() throws InterruptedException {
        TextEngine.printWithDelays("The current Tempurature is " + weatherAPIPromptEngine.getWeatherDataPart("temperature"), false);
        weatherAPIPromptEngine.setLocation();
        weatherAPIPromptEngine.recallWeather();
        TextEngine.printWithDelays("The current Tempurature is " + weatherAPIPromptEngine.getWeatherDataPart("temperature"), false);
        System.out.println(weatherAPIPromptEngine.getWeatherData());
    }
}
