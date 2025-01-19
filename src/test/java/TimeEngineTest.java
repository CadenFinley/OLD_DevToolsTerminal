
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class TimeEngineTest {

    private TimeEngine timerEngine;
    private TimeEngine stopwatchEngine;
    private final Object trigger = new Object();

    @Before
    public void setUp() {
        timerEngine = new TimeEngine("timer", trigger);
        stopwatchEngine = new TimeEngine("stopwatch", null);
    }

    @Test
    public void testStartTimer() throws InterruptedException {
        timerEngine.startClock(2);
        TimeUnit.SECONDS.sleep(3);
        assertFalse(timerEngine.isRunning());
        assertEquals(0, timerEngine.getRemainingTimeInSeconds());
    }

    @Test
    public void testStartStopwatch() throws InterruptedException {
        stopwatchEngine.startClock(1);
        TimeUnit.SECONDS.sleep(3);
        assertTrue(stopwatchEngine.isRunning());
        stopwatchEngine.stopClock();
        assertEquals(3, stopwatchEngine.getTimeElapsedInSeconds());
    }

    @Test
    public void testStopClock() {
        timerEngine.startClock(10);
        timerEngine.stopClock();
        assertFalse(timerEngine.isRunning());
    }

    @Test
    public void testReturnTime() {
        timerEngine.startClock(3600);
        assertEquals("01:00:00", timerEngine.returnTime());
    }

    @Test
    public void testGetTimeElapsedInSeconds() {
        stopwatchEngine.startClock(1);
        assertEquals(0, stopwatchEngine.getTimeElapsedInSeconds());
    }

    @Test
    public void testGetRemainingTimeInSeconds() {
        timerEngine.startClock(10);
        assertEquals(10, timerEngine.getRemainingTimeInSeconds());
    }

    @Test
    public void testSetSavedTimeInSeconds() {
        stopwatchEngine.setSavedTimeInSeconds(100);
        assertEquals(100, stopwatchEngine.getTimeElapsedInSeconds());
    }

    @Test
    public void testDebugTime() {
        stopwatchEngine.debugTime(50);
        assertEquals(50, stopwatchEngine.getTimeElapsedInSeconds());
    }

    @Test
    public void testAddTimeToTimerInSeconds() {
        timerEngine.startClock(10);
        timerEngine.addTimeToTimerInSeconds(5);
        assertEquals(15, timerEngine.getRemainingTimeInSeconds());
    }

    @Test
    public void testTimeStamp() {
        assertNotNull(timerEngine.timeStamp());
    }
}
