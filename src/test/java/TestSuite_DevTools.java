
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

    @Before
    public void setUp() {
        // Code to run before each test
        timer = null;
        stopwatch = null;
    }

    @After
    public void tearDown() {
        // Code to run after each test
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

}
