
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class OpenAIPromptEngineTest {

    private OpenAIPromptEngine promptEngine;

    @Before
    public void setUp() {
        promptEngine = new OpenAIPromptEngine(null);
    }

    @Test
    public void testBuildPromptAndReturnResponce() {
        String response = promptEngine.buildPromptAndReturnResponce("Hello", false);
        assertNotNull(response);
    }

    @Test
    public void testBuildPromptAndReturnNoResponce() {
        promptEngine.buildPromptAndReturnNoResponce("Hello", false);
        List<String> chatCache = promptEngine.getChatCache();
        assertTrue(chatCache.contains("User: Hello"));
    }

    @Test
    public void testSetAndGetAPIKey() {
        promptEngine.setAPIKey("new-api-key");
        assertEquals("new-api-key", promptEngine.getAPIKey());
    }

    @Test
    public void testGetLastPromptUsed() {
        promptEngine.buildPromptAndReturnResponce("Hello", false);
        assertEquals("Hello", promptEngine.getLastPromptUsed());
    }

    @Test
    public void testGetLastResponseReceived() {
        promptEngine.buildPromptAndReturnResponce("Hello", false);
        assertNotNull(promptEngine.getLastResponseReceived());
    }

    @Test
    public void testGetChatCache() {
        promptEngine.buildPromptAndReturnResponce("Hello", true);
        List<String> chatCache = promptEngine.getChatCache();
        assertFalse(chatCache.isEmpty());
    }

    @Test
    public void testClearChatCache() {
        promptEngine.buildPromptAndReturnResponce("Hello", true);
        promptEngine.clearChatCache();
        assertTrue(promptEngine.getChatCache().isEmpty());
    }

    @Test
    public void testSetChatCache() {
        promptEngine.setChatCache(List.of("User: Test", "ChatGPT: Response"));
        List<String> chatCache = promptEngine.getChatCache();
        assertEquals(2, chatCache.size());
    }

    @Test
    public void testGetResponseData() {
        promptEngine.buildPromptAndReturnResponce("Hello", false);
        String responseData = promptEngine.getResponseData("all");
        assertNotNull(responseData);
    }

    @Test
    public void testTestAPIKey() {
        boolean isValid = promptEngine.testAPIKey("test-api-key");
        assertTrue(isValid);
    }
}
