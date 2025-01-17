
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * The PromptEngine class generates prompts using an OpenAI API key.
 *
 * @author Caden Finley
 * @version 1.0
 */
public class OpenAIPromptEngine {

    private String USER_API_KEY = null;
    private String lastPromptUsed = "";
    private String lastResponseReceived = "";
    List<String> chatCache = new ArrayList<>();

    /**
     * Constructs a PromptEngine object with the specified API key.
     */
    public OpenAIPromptEngine(String apiKey) {
        this.USER_API_KEY = apiKey;
    }

    /**
     * Constructs a PromptEngine object.
     */
    public OpenAIPromptEngine() {
        // Do nothing
    }

    /**
     * The function `buildAndReturnPrompt` generates a prompt for the OpenAI API
     *
     * @param message
     */
    public String buildPromptAndReturnResponce(String message) {
        if (USER_API_KEY == null) {
            return "API key not set.";
        }
        String response = chatGPT(message);
        chatCache.add("User: " + message);
        chatCache.add("ChatGPT: " + message);
        return response;
    }

    /**
     * The function `buildPromptAndReturnNoResponce` generates a prompt for the
     * OpenAI API without returning a response.
     *
     * @param message
     */
    public void buildPromptAndReturnNoResponce(String message) {
        if (USER_API_KEY == null) {
            System.out.println("OpenAI: " + System.currentTimeMillis() + "API key not set.");
            return;
        }
        chatGPT(message);
    }

    /**
     * The function `chatGPT` sends a message to the OpenAI API for chat
     * completions using a specified model and API key, and returns the
     * extracted content from the response.
     *
     * @param message The `chatGPT` method you provided is a Java method that
     * interacts with the OpenAI GPT-3.5 API to generate chat completions based
     * on the input message.
     * @return The `chatGPT` method returns the extracted contents of the
     * response from the OpenAI API after processing the input message through
     * the GPT-3.5 model.
     */
    private String chatGPT(String message) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            String url = "https://api.openai.com/v1/chat/completions";
            String apiKey = USER_API_KEY; // API key goes here
            String model = "gpt-3.5-turbo";
            String sentMessage;
            if (!lastPromptUsed.equals("")) {
                sentMessage = "These are the previous messages: " + chatCache.toString() + "This is the users responce based on the previous conversation: " + message;
            } else {
                sentMessage = message;
            }
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Authorization", "Bearer " + apiKey);
                con.setRequestProperty("Content-Type", "application/json");
                String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + sentMessage + "\"}]}";
                con.setDoOutput(true);
                try (OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream())) {
                    writer.write(body);
                    writer.flush();
                }
                StringBuilder response;
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
                // returns the extracted contents of the response.
                lastPromptUsed = message;
                lastResponseReceived = extractContentFromResponse(response.toString());
                return lastResponseReceived;
            } catch (IOException e) {
                System.out.println("OpenAI API connection failed. Please check your internet connection and try again later. " + System.currentTimeMillis());
                System.out.println("OpenAI: " + System.currentTimeMillis() + " AI generation has been disabled.");
                return null;
            }
        });
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("OpenAI API connection timed out." + System.currentTimeMillis());
            System.out.println("OpenAI: " + System.currentTimeMillis() + " AI generation has been disabled. You can renable it in settings.");
            return null;
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("OpenAI: " + System.currentTimeMillis() + " An error occurred while processing the request.");
            return null;
        } finally {
            executor.shutdown();
        }
    }

    /**
     * This function extracts content from a response string based on specific
     * markers.
     *
     * @param response The `extractContentFromResponse` method takes a `String`
     * parameter named `response`, which is the input string from which we want
     * to extract the content. The method then finds the starting and ending
     * markers within the response string to extract the content between them.
     * @return The method `extractContentFromResponse` returns a substring
     * containing the content extracted from the input response string.
     */
    private static String extractContentFromResponse(String response) {
        int startMarker = response.indexOf("content") + 11; // Marker for where the content starts.
        int endMarker = response.indexOf("\"", startMarker); // Marker for where the content ends.
        return response.substring(startMarker, endMarker); // Returns the substring containing only the response.
    }

    /**
     * The function `testAPIKey` sends a test message to an API endpoint using
     * the provided API key and checks if a valid response is received.
     *
     * @param apiKey The `testAPIKey` method you provided is used to test the
     * validity of an API key by making a request to the OpenAI API endpoint.
     * The `apiKey` parameter is the API key that is passed to the method for
     * testing.
     * @return The `testAPIKey` method returns a boolean value. If the API key
     * is valid and the response from the API contains expected content, it
     * returns `true`. If there is an IOException (e.g., internet connection
     * issue) or the response does not contain the expected content, it returns
     * `false`.
     */
    public static boolean testAPIKey(String apiKey) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> {
            String url = "https://api.openai.com/v1/engines";
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Authorization", "Bearer " + apiKey);
                int responseCode = con.getResponseCode();
                if (responseCode != 200) {
                    System.out.println("Response Code: " + responseCode);
                    return false;
                }
                return true;
            } catch (IOException e) {
                return false;
            }
        });
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return false;
        } catch (InterruptedException | ExecutionException e) {
            return false;
        } finally {
            executor.shutdown();
        }
    }

    /**
     * The function `setAPIKey` sets the user's API key for the OpenAI API.
     *
     * @param apiKey The `
     * @throws TimeoutException
     *
     */
    public void setAPIKey(String apiKey) {
        this.USER_API_KEY = apiKey;
    }

    /**
     * The function `getAPIKey` returns the user's API key for the OpenAI API.
     *
     * @return The `getAPIKey` method returns the user's API key for the OpenAI
     * API as a String.
     */
    public String getAPIKey() {
        return USER_API_KEY;
    }

    /**
     * The function `getLastPromptUsed` returns the last prompt used.
     *
     * @return The `getLastPromptUsed` method returns the last prompt used as a
     * String.
     */
    public String getLastPromptUsed() {
        return lastPromptUsed;
    }

    /**
     * The function `getLastResponseReceived` returns the last response
     * received.
     *
     * @return The `getLastResponseReceived` method returns the last response
     * received as a String.
     */
    public String getLastResponseReceived() {
        return lastResponseReceived;
    }

    public List<String> getChatCache() {
        return chatCache;
    }
}
