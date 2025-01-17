
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Map;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Caden Finley
 */
public class TestSuite_DevTools {

    private ClockEngine timer;
    private ClockEngine stopwatch;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    private ImageToASCIIEngine img;
    private final String imagePathToVaevLogo = "vaevlogo.jpg";

    private OpenAIPromptEngine prompt;

    private final String API_KEY_FOR_TESTING = "sk-z3q9L-Lh39YYmooGmbPNAFlsaDywlFdRB-O1vFB4mYT3BlbkFJSSn6Um-zBw4r7fUB2H6dX3fhiOisNo8PFzy-fdKXwA";

    @Before
    public void setUp() {
        // Code to run before each test
        prompt = null;
        timer = null;
        stopwatch = null;
        Engine.TESTING = true;
    }

    @After
    public void tearDown() {
        // Code to run after each test
        Engine.TESTING = false;
    }

    @Test
    public void testStartClockTimer() throws InterruptedException {
        System.out.println("Starting testStartClockTimer");
        timer = new ClockEngine("timer", null);
        timer.startClock(2);
        Thread.sleep(3000); // Wait for the timer to finish
        assertFalse(timer.isRunning());
        assertEquals(0, timer.getRemainingTimeInSeconds());
    }

    @Test
    public void testStartClockStopwatch() throws InterruptedException {
        System.out.println("Starting testStartClockStopwatch");
        stopwatch = new ClockEngine("stopwatch", null);
        stopwatch.startClock(1);
        Thread.sleep(3000); // Wait for the stopwatch to run
        assertTrue(stopwatch.isRunning());
        stopwatch.stopClock();
        assertFalse(stopwatch.isRunning());
        assertTrue(stopwatch.getTimeElapsedInSeconds() >= 2);
    }

    @Test
    public void testStopClock() {
        System.out.println("Starting testStopClock");
        stopwatch = new ClockEngine("stopwatch", null);
        stopwatch.startClock(1);
        stopwatch.stopClock();
        assertFalse(stopwatch.isRunning());
    }

    @Test
    public void testReturnTime() {
        stopwatch = new ClockEngine("stopwatch", null);
        stopwatch.debugTime(3661); // 1 hour, 1 minute, 1 second
        assertEquals("01:01:01", stopwatch.returnTime());
    }

    @Test
    public void testGetTimeElapsedInSeconds() {
        stopwatch = new ClockEngine("stopwatch", null);
        stopwatch.debugTime(3661);
        assertEquals(3661, stopwatch.getTimeElapsedInSeconds());
    }

    @Test
    public void testGetRemainingTimeInSeconds() {
        timer = new ClockEngine("timer", null);
        timer.startClock(10);
        timer.addTimeToTimerInSeconds(5);
        assertTrue(timer.getRemainingTimeInSeconds() > 0);
    }

    @Test
    public void testSetSavedTimeInSeconds() {
        stopwatch = new ClockEngine("stopwatch", null);
        stopwatch.setSavedTimeInSeconds(500);
        assertEquals(500, stopwatch.getTimeElapsedInSeconds());
    }

    @Test
    public void testDebugTime() {
        stopwatch = new ClockEngine("stopwatch", null);
        stopwatch.debugTime(1000);
        assertEquals(1000, stopwatch.getTimeElapsedInSeconds());
    }

    @Test
    public void testAddTimeToTimerInSeconds() {
        timer = new ClockEngine("timer", null);
        timer.startClock(10);
        timer.addTimeToTimerInSeconds(100);
        assertTrue(timer.getRemainingTimeInSeconds() > 10);
    }

    @Test
    public void testOPENAICONNECTION() {
        assertTrue(OpenAIPromptEngine.testAPIKey(API_KEY_FOR_TESTING));
    }

    @Test
    public void testPromptEngineReceiveInput() {
        prompt = new OpenAIPromptEngine(API_KEY_FOR_TESTING);
        String message = "This is a test message to verify connection to OPENAI API.";
        assertTrue(prompt.buildPromptAndReturnResponce(message) != null && !"AI generation is disabled. You can enable it in settings.\n".equals(prompt.buildPromptAndReturnResponce(message)));
    }

    @Test
    public void testPromptCachedMessage() {
        prompt = new OpenAIPromptEngine(API_KEY_FOR_TESTING);
        String message = "This is a test message to verify connection to OPENAI API.";
        prompt.buildPromptAndReturnNoResponce(message);
        assertEquals(message, prompt.getLastPromptUsed());
    }

    @Test
    public void testPromptCachedMessage0() {
        prompt = new OpenAIPromptEngine(API_KEY_FOR_TESTING);
        String message = "This is a test message to verify connection to OPENAI API.";
        prompt.buildPromptAndReturnNoResponce(message);
        assertEquals("This is a test message to verify connection to OPENAI API.", prompt.getLastPromptUsed());
    }

    @Test
    public void testPromptEngineReceiveInput1() {
        prompt = new OpenAIPromptEngine(API_KEY_FOR_TESTING);
        String message = "This is a test message to verify connection to OPENAI API.";
        String received = prompt.buildPromptAndReturnResponce(message);
        assertTrue(received != null && !received.equals("") && !received.equals("AI generation is disabled. You can enable it in settings.\n"));
    }

    @Test
    public void testCheckValidInput() {
        System.setOut(new PrintStream(outContent));
        assertTrue(TextEngine.checkValidInput("command"));
        assertFalse(TextEngine.checkValidInput(""));
        assertFalse(TextEngine.checkValidInput(null));
        System.setOut(originalOut);
    }

    @Test
    public void testParseCommand() {
        System.setOut(new PrintStream(outContent));
        String[] possibleCommands = {"look", "take", "open"};
        assertEquals("look", TextEngine.parseCommand("look", possibleCommands));
        assertEquals("take", TextEngine.parseCommand("take", possibleCommands));
        assertEquals("open", TextEngine.parseCommand("open", possibleCommands));
        assertEquals("unknown", TextEngine.parseCommand("unknown", possibleCommands));
        System.setOut(originalOut);
    }

    @Test
    public void testGetMatchLength() {
        System.setOut(new PrintStream(outContent));
        assertEquals(3, TextEngine.getMatchLength("look", "loo"));
        assertEquals(0, TextEngine.getMatchLength("look", "take"));
        System.setOut(originalOut);
    }

    @Test
    public void testHas() {
        System.setOut(new PrintStream(outContent));
        String[] possibleCommands = {"look", "take", "open"};
        assertTrue(TextEngine.has(possibleCommands, "look"));
        assertFalse(TextEngine.has(possibleCommands, "unknown"));
        System.setOut(originalOut);
    }

    @Test
    public void testParseCommand0() {
        System.setOut(new PrintStream(outContent));
        String[] possibleCommands = {"look", "take", "open"};
        assertEquals("look", TextEngine.parseCommand("look", possibleCommands));
        assertEquals("take", TextEngine.parseCommand("take", possibleCommands));
        assertEquals("open", TextEngine.parseCommand("open", possibleCommands));
        assertEquals("unknown", TextEngine.parseCommand("unknown", possibleCommands));
        assertEquals("look", TextEngine.parseCommand("LOOK", possibleCommands)); // Case sensitivity test
        assertEquals("look", TextEngine.parseCommand("lo", possibleCommands)); // Partial match test
        assertEquals("", TextEngine.parseCommand("", possibleCommands)); // Empty string test
        System.setOut(originalOut);
    }

    @Test
    public void testGetMatchLength0() {
        System.setOut(new PrintStream(outContent));
        assertEquals(3, TextEngine.getMatchLength("look", "loo"));
        assertEquals(0, TextEngine.getMatchLength("look", "take"));
        assertEquals(4, TextEngine.getMatchLength("look", "look"));
        assertEquals(0, TextEngine.getMatchLength("look", ""));
        assertEquals(0, TextEngine.getMatchLength("", "look"));
        assertEquals(0, TextEngine.getMatchLength("look", null));
        assertEquals(0, TextEngine.getMatchLength(null, "look"));
        System.setOut(originalOut);
    }

    @Test
    public void testHas0() {
        System.setOut(new PrintStream(outContent));
        String[] possibleCommands = {"look", "take", "open"};
        assertTrue(TextEngine.has(possibleCommands, "look"));
        assertFalse(TextEngine.has(possibleCommands, "unknown"));
        assertTrue(TextEngine.has(possibleCommands, "take"));
        assertTrue(TextEngine.has(possibleCommands, "open"));
        assertFalse(TextEngine.has(possibleCommands, "LOOK")); // Case sensitivity test
        assertFalse(TextEngine.has(possibleCommands, "")); // Empty string test
        assertFalse(TextEngine.has(possibleCommands, null)); // Null test
        System.setOut(originalOut);
    }

    @Test
    public void Test_Load_Image_Vaevlogo() {
        img = new ImageToASCIIEngine(imagePathToVaevLogo);
        assertTrue(img != null);
    }

    @Test
    public void Test_Output_File_Exists() {
        img = new ImageToASCIIEngine(imagePathToVaevLogo);
        img.convertToASCIIInFile("vaevlogo.txt");
        assertTrue(img.getOutputPath().exists());
        File outputFile = img.getOutputPath();
        if (outputFile != null) {
            outputFile.delete();
        }
    }

    @Test
    public void testConvertDistance() {
        assertEquals(1.60934, UnitConversionEngine.convertUnit(1, "miles", "kilometers", "distance"), 0.00001);
        assertEquals(5280, UnitConversionEngine.convertUnit(1, "miles", "feet", "distance"), 0.00001);
        assertEquals(0.621371, UnitConversionEngine.convertUnit(1, "kilometers", "miles", "distance"), 0.00001);
    }

    @Test
    public void testConvertTemperature() {
        assertEquals(0, UnitConversionEngine.convertUnit(32, "fahrenheit", "celsius", "temperature"), 0.00001);
        assertEquals(273.15, UnitConversionEngine.convertUnit(0, "celsius", "kelvin", "temperature"), 0.00001);
        assertEquals(32, UnitConversionEngine.convertUnit(0, "celsius", "fahrenheit", "temperature"), 0.00001);
    }

    @Test
    public void testConvertWeight() {
        assertEquals(2.20462, UnitConversionEngine.convertUnit(1, "kilograms", "pounds", "weight"), 0.00001);
        assertEquals(1000, UnitConversionEngine.convertUnit(1, "kilograms", "grams", "weight"), 0.00001);
        assertEquals(0.453592, UnitConversionEngine.convertUnit(1, "pounds", "kilograms", "weight"), 0.00001);
    }

    @Test
    public void testConvertVolume() {
        assertEquals(3.78541, UnitConversionEngine.convertUnit(1, "gallons", "liters", "volume"), 0.00001);
        assertEquals(0.264172, UnitConversionEngine.convertUnit(1, "liters", "gallons", "volume"), 0.00001);
        assertEquals(4, UnitConversionEngine.convertUnit(1, "quarts", "cups", "volume"), 0.00001);
    }

    @Test
    public void testConvertSpeed() {
        assertEquals(1.60934, UnitConversionEngine.convertUnit(1, "miles per hour", "kilometers per hour", "speed"), 0.00001);
        assertEquals(0.44704, UnitConversionEngine.convertUnit(1, "miles per hour", "meters per second", "speed"), 0.00001);
        assertEquals(0.868976, UnitConversionEngine.convertUnit(1, "miles per hour", "knots", "speed"), 0.00001);
    }

    @Test
    public void testWeatherAPI() {
        WeatherAPIPromptEngine weather = new WeatherAPIPromptEngine();
        Object response = weather.getWeatherDataPart("temperature");
        System.out.println(response);
        assertTrue(response != null && !response.equals(""));
    }

    @Test
    public void testExtractWeatherData() {
        String jsonResponse = "{ \"timelines\": { \"minutely\": [ { \"time\": \"2025-01-15T15:09:00Z\", \"values\": { \"cloudBase\": null, \"cloudCeiling\": null, \"cloudCover\": 1, \"dewPoint\": -15.38, \"freezingRainIntensity\": 0, \"hailProbability\": 11.6, \"hailSize\": 3.46, \"humidity\": 39, \"precipitationProbability\": 0, \"pressureSurfaceLevel\": 1013.63, \"rainIntensity\": 0, \"sleetIntensity\": 0, \"snowIntensity\": 0, \"temperature\": -3.38, \"temperatureApparent\": -10.27, \"uvHealthConcern\": 0, \"uvIndex\": 1, \"visibility\": 16, \"weatherCode\": 1000, \"windDirection\": 315.19, \"windGust\": 11.63, \"windSpeed\": 7 } } ] } } }";
        WeatherAPIPromptEngine weatherEngine = new WeatherAPIPromptEngine();
        Map<String, Object> extractedData = weatherEngine.extractWeatherData(jsonResponse);
        assertEquals(-15.38, extractedData.get("dewPoint"));
        assertEquals(1.0, extractedData.get("cloudCover"));
        assertEquals(39, extractedData.get("humidity"));
    }
}
