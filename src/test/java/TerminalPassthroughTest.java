
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class TerminalPassthroughTest {

    private TerminalPassthrough terminalPassthrough;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUp() {
        terminalPassthrough = new TerminalPassthrough();
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void testGetTerminalName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            assertEquals("cmd.exe", terminalPassthrough.getTerminalName());
        } else if (os.contains("nix") || os.contains("nux")) {
            assertEquals("bash", terminalPassthrough.getTerminalName());
        } else {
            assertEquals("sh", terminalPassthrough.getTerminalName());
        }
    }

    @Test
    public void testSetAndToggleDisplayWholePath() {
        assertFalse(terminalPassthrough.isDisplayWholePath());
        terminalPassthrough.setDisplayWholePath(true);
        assertTrue(terminalPassthrough.isDisplayWholePath());
        terminalPassthrough.toggleDisplayWholePath();
        assertFalse(terminalPassthrough.isDisplayWholePath());
    }

    @Test
    public void testPrintCurrentTerminalPosition() {
        terminalPassthrough.printCurrentTerminalPosition();
        assertTrue(outContent.toString().contains(terminalPassthrough.getTerminalName()));
    }

    @Test
    public void testReturnCurrentTerminalPosition() {
        String position = terminalPassthrough.returnCurrentTerminalPosition();
        assertNotNull(position);
        assertFalse(position.isEmpty());
    }

    @Test
    public void testExecuteCommand() throws InterruptedException {
        Thread commandThread = terminalPassthrough.executeCommand("echo Hello, World!", true);
        commandThread.join();
        assertTrue(outContent.toString().contains("Hello, World!"));
    }

    @Test
    public void testExecuteInvalidCommand() throws InterruptedException {
        Thread commandThread = terminalPassthrough.executeCommand("invalidcommand", true);
        commandThread.join();
        assertTrue(outContent.toString().contains("Error executing command"));
    }

    @Test
    public void testChangeDirectory() throws InterruptedException {
        String initialDirectory = terminalPassthrough.returnCurrentTerminalPosition();
        Thread commandThread = terminalPassthrough.executeCommand("cd /", true);
        commandThread.join();
        String newDirectory = terminalPassthrough.returnCurrentTerminalPosition();
        assertNotEquals(initialDirectory, newDirectory);
    }
}
