
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * The PromptEngine class generates prompts for a text adventure game using an
 * OpenAI API key.
 *
 * @author Caden Finley
 * @version 1.0
 */
public class PromptEngine {

    public static final String USER_API_KEY = "sk-z3q9L-Lh39YYmooGmbPNAFlsaDywlFdRB-O1vFB4mYT3BlbkFJSSn6Um-zBw4r7fUB2H6dX3fhiOisNo8PFzy-fdKXwA";
    public static boolean aiGenerationEnabled = true;
    public static int promptLength = 30;
    private static String prompt = null;

    /**
     *
     */
    public static void buildPrompt() {
        if (aiGenerationEnabled || prompt == null || prompt.isEmpty()) {
            try {
                prompt = chatGPT(buildMessage()) + "\n";
            } catch (TimeoutException e) {
                TextEngine.printNoDelay("AI Generation is now disabled. Re-enable it in settings.", false);
                aiGenerationEnabled = false;
                TextEngine.enterToNext();
            }
            TextEngine.clearScreen();
        }
    }

    private static String buildMessage() {
        return " ";
    }

    /**
     * The function `returnPrompt` checks if AI generation is enabled,
     * highlights keywords in the prompt, and returns the modified prompt or a
     * message if AI generation is disabled.
     *
     * @return The method `returnPrompt()` returns the generated prompt with
     * highlighted keywords if AI generation is enabled. If AI generation is
     * disabled, it returns the message "AI generation is disabled. Please
     * enable it in settings."
     */
    public static String returnPrompt() {
        if (aiGenerationEnabled) {
            if (prompt == null || prompt.isEmpty()) {
                buildPrompt();
            }

            // Highlight keywords
            return prompt;
        }
        return "AI generation is disabled. Please enable it in settings.";
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
    private static String chatGPT(String message) throws TimeoutException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            String url = "https://api.openai.com/v1/chat/completions";
            String apiKey = USER_API_KEY; // API key goes here
            String model = "gpt-3.5-turbo";
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Authorization", "Bearer " + apiKey);
                con.setRequestProperty("Content-Type", "application/json");
                String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + message + "\"}]}";
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
                return extractContentFromResponse(response.toString());
            } catch (IOException e) {
                TextEngine.printNoDelay("OpenAI API connection failed. Please check your internet connection and try again later.", false);
                TextEngine.printNoDelay("AI generation has been disabled. You can renable it in settings.", false);
                TextEngine.enterToNext();
                aiGenerationEnabled = false;
                return null;
            }
        });
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            TextEngine.printNoDelay("OpenAI API connection timed out.", false);
            TextEngine.printNoDelay("AI generation has been disabled. You can renable it in settings.", false);
            TextEngine.enterToNext();
            aiGenerationEnabled = false;
            return null;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
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
    public static boolean testAPIKey(String apiKey) throws TimeoutException {
        Callable<Boolean> task = () -> {
            String testMessage = "This is a test message to check if the API key is valid.";
            try {
                String url = "https://api.openai.com/v1/chat/completions";
                String model = "gpt-3.5-turbo";
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Authorization", "Bearer " + apiKey);
                con.setRequestProperty("Content-Type", "application/json");
                String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + testMessage + "\"}]}";
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
                // Check if the response contains the expected content
                String responseContent = extractContentFromResponse(response.toString());
                aiGenerationEnabled = responseContent != null && !responseContent.isEmpty();
                return responseContent != null && !responseContent.isEmpty();
            } catch (IOException e) {
                TextEngine.printNoDelay("API Key failed. Please check your internet connection", false);
                aiGenerationEnabled = false;
                return false;
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Future<Boolean> future = executor.submit(task);
        try {
            return future.get(10, TimeUnit.SECONDS); // Set timeout to 10 seconds
        } catch (TimeoutException e) {
            future.cancel(true);
            aiGenerationEnabled = false;
            TextEngine.printNoDelay("API Key validation timed out. AI generation is disabled, re-enable it in settings.", false);
            TextEngine.enterToNext();
            return false;
        } catch (InterruptedException | ExecutionException e) {
            aiGenerationEnabled = false;
            TextEngine.printNoDelay("API Key failed. AI generation is disabled, re-enable it in settings.", false);
            TextEngine.enterToNext();
            return false;
        } finally {
            executor.shutdown();
        }
    }

    /**
     * The function `setPromptLength` sets the length of a prompt based on the
     * input string "short", "medium", or "long".
     *
     * @param length The `length` parameter in the `setPromptLength` method is a
     * String that specifies the desired length of the prompt. It can have three
     * possible values: "short", "medium", or "long". The method sets the
     * `promptLength` variable based on the value of the `length`
     */
    public static void setPromptLength(String length) {
        promptLength = switch (length) {
            case "short" ->
                30;
            case "medium" ->
                50;
            case "long" ->
                75;
            default ->
                30;
        };
    }
}
