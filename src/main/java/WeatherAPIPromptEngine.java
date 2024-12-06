
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
public class WeatherAPIPromptEngine {

    private String URL_TO_TOMORROW_IO = "https://api.tomorrow.io/v4/weather/forecast?location=42.3478,-71.0466&apikey=lJJSlWZaiv9LeL1iS35I7QnuMQkPMOya";

    public WeatherAPIPromptEngine() {
        //get location of user
        //amend URL_TO_TOMORROW_IO to include user's location
    }

    public String buildPromptAndReturnResponce() {
        return weatherAPI();
    }

    public void buildPromptAndReturnNoResponce() {
        //nothing rn
    }

    private String weatherAPI() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            try {
                URL url = new URL(URL_TO_TOMORROW_IO);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Type", "application/json");
                StringBuilder response;
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
                return response.toString();
            } catch (IOException e) {
                System.out.println("An error occurred while processing the request. Please check your internet connection.");
                return null;
            }
        });
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("Weather API connection timed out.");
            return null;
        } catch (InterruptedException | ExecutionException e) {
            TextEngine.printNoDelay("An error occurred while processing the request.", false);
            return null;
        } finally {
            executor.shutdown();
        }
    }

    private static String extractContentFromResponse(String response) {
        return "Extracted content from response";
    }

    public static boolean testAPIKey(String apiKey) {
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
                return responseContent != null && !responseContent.isEmpty();
            } catch (IOException e) {
                TextEngine.printNoDelay("API Key failed. Please check your internet connection", false);
                return false;
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Future<Boolean> future = executor.submit(task);
        try {
            return future.get(10, TimeUnit.SECONDS); // Set timeout to 10 seconds
        } catch (TimeoutException e) {
            future.cancel(true);
            TextEngine.printNoDelay("API Key validation timed out. AI generation is disabled, re-enable it in settings.", false);
            TextEngine.enterToNext();
            return false;
        } catch (InterruptedException | ExecutionException e) {
            TextEngine.printNoDelay("API Key failed. AI generation is disabled, re-enable it in settings.", false);
            TextEngine.enterToNext();
            return false;
        } finally {
            executor.shutdown();
        }
    }
}
