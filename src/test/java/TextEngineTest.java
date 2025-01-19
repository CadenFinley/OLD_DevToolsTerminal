
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class TextEngineTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @Test
    public void testSetWidth() {
        String result = TextEngine.setWidth();
        assertTrue(result.startsWith("Terminal width: "));
    }

    @Test
    public void testPrintWithDelays() {
        TextEngine.printWithDelays("Hello World", false, true);
        assertEquals("Hello World\n", outContent.toString());
    }

    @Test
    public void testPrintNoDelay() {
        TextEngine.printNoDelay("Hello World", false, true);
        assertEquals("Hello World\n", outContent.toString());
    }

    @Test
    public void testClearScreen() {
        TextEngine.clearScreen();
        assertTrue(outContent.toString().contains("\033[H\033[2J"));
    }

    @Test
    public void testEnterToNext() {
        System.setIn(new ByteArrayInputStream("\n".getBytes()));
        TextEngine.enterToNext();
        assertTrue(outContent.toString().contains("Press Enter to continue"));
    }

    @Test
    public void testCheckValidInput() {
        assertTrue(TextEngine.checkValidInput("command"));
        assertFalse(TextEngine.checkValidInput(""));
        assertFalse(TextEngine.checkValidInput(null));
    }

    @Test
    public void testParseCommand() {
        String[] possibleCommands = {"start", "stop", "pause"};
        assertEquals("start", TextEngine.parseCommand("sta", possibleCommands));
        assertEquals("stop", TextEngine.parseCommand("stop", possibleCommands));
        assertEquals("unknown", TextEngine.parseCommand("unknown", possibleCommands));
    }

    @Test
    public void testGetMatchLength() {
        assertEquals(3, TextEngine.getMatchLength("start", "sta"));
        assertEquals(0, TextEngine.getMatchLength("start", "xyz"));
    }

    @Test
    public void testHas() {
        String[] possibleCommands = {"start", "stop", "pause"};
        assertTrue(TextEngine.has(possibleCommands, "start"));
        assertFalse(TextEngine.has(possibleCommands, "unknown"));
    }

    @Test
    public void testSetSpeedSetting() {
        TextEngine.setSpeedSetting("fast");
        assertEquals("fast", TextEngine.getSpeedSetting());
    }

    @Test
    public void testGetSpeedSetting() {
        TextEngine.setSpeedSetting("slow");
        assertEquals("slow", TextEngine.getSpeedSetting());
    }
}
