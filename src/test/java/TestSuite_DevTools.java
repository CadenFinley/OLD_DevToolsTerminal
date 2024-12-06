
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeoutException;

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

    @Before
    public void setUp() {
        // Code to run before each test
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
    public void testOPENAICONNECTION() throws TimeoutException {
        assertTrue(PromptEngine.testAPIKey(Engine.getUSER_API_KEY()));
    }

    @Test
    public void testPromptEngineReceiveInput() {
        PromptEngine prompt = new PromptEngine();
        prompt.setAIEnabled(true);
        prompt.setAPIKey(Engine.getUSER_API_KEY());
        prompt.buildPrompt();
        assertTrue(prompt.returnPrompt() != null && !"AI generation is disabled. Please enable it in settings.".equals(prompt.returnPrompt()));
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

}
