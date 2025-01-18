
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * The PromptEngine class generates prompts using an OpenAI API key.
 *
 * @author Caden Finley
 * @version 2.0
 */
public class OpenAIPromptEngine {

    private String USER_API_KEY = "";
    private String lastPromptUsed = "";
    private String lastResponseReceived = "";
    private final List<String> chatCache;
    private Map<String, Object> responseDataMap;

    public OpenAIPromptEngine(String apiKey) {
        this.USER_API_KEY = apiKey;
        chatCache = new ArrayList<>();
    }

    public OpenAIPromptEngine() {
        chatCache = new ArrayList<>();
    }

    public String buildPromptAndReturnResponce(String message, boolean usingChatCache) {
        if (USER_API_KEY == null) {
            return "API key not set.";
        }
        if (message == null || message.isEmpty()) {
            return "User's message is empty.";
        }
        String response = chatGPT(message, usingChatCache);
        if (usingChatCache) {
            chatCache.add("User: " + message);
            if (response != null && !response.isEmpty()) {
                chatCache.add("ChatGPT: " + response);
            }
        }
        return response;
    }

    public void buildPromptAndReturnNoResponce(String message, boolean usingChatCache) {
        if (USER_API_KEY == null) {
            System.out.println("OpenAI: " + System.currentTimeMillis() + "API key not set.");
            return;
        }
        String response = chatGPT(message, usingChatCache);
        if (usingChatCache) {
            chatCache.add("User: " + message);
            if (response != null && !response.isEmpty()) {
                chatCache.add("ChatGPT: " + response);
            }
        }
    }

    private String chatGPT(String message, boolean usingChatCache) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            String url = "https://api.openai.com/v1/chat/completions";
            String apiKey = USER_API_KEY; // API key goes here
            String model = "gpt-3.5-turbo";
            String sentMessage;
            if (usingChatCache && !lastPromptUsed.equals("")) {
                sentMessage = "These are the previous messages from this conversation: '" + chatCache.toString().trim() + "' This is the users response based on the previous conversation: '" + message + "'";
            } else {
                sentMessage = message;
            }
            lastPromptUsed = sentMessage;
            //System.out.println("OpenAI: " + System.currentTimeMillis() + " Sending message to OpenAI API: " + sentMessage);
            StringBuilder response = new StringBuilder();
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
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
                // returns the extracted contents of the response.
                responseDataMap = parseJSONResponse(response.toString());
                lastResponseReceived = extractContentFromJSON(response.toString());
                return lastResponseReceived;
            } catch (IOException e) {
                System.out.println("OpenAI API connection failed. Please check your internet connection and try again later. " + System.currentTimeMillis() + " " + e.getMessage());
                lastResponseReceived = response.toString();
                return null;
            }
        });
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("OpenAI API connection timed out." + System.currentTimeMillis());
            return null;
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("OpenAI: " + System.currentTimeMillis() + " An error occurred while processing the request. " + e.getMessage());
            return null;
        } finally {
            executor.shutdown();
        }
    }

    private Map<String, Object> parseJSONResponse(String jsonResponse) {
        Map<String, Object> responseData = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            jsonObject.keySet().forEach(key -> {
                responseData.put(key, jsonObject.get(key));
            });
        } catch (JSONException e) {
            System.out.println("Failed to parse JSON response: " + e.getMessage());
        }
        return responseData;
    }

    private String extractContentFromJSON(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            return jsonObject.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        } catch (JSONException e) {
            System.out.println("Failed to extract content from JSON response: " + e.getMessage());
            return null;
        }
    }

    public boolean testAPIKey(String apiKey) {
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

    public void setAPIKey(String apiKey) {
        this.USER_API_KEY = apiKey;
    }

    public String getAPIKey() {
        return USER_API_KEY;
    }

    public String getLastPromptUsed() {
        return lastPromptUsed;
    }

    public String getLastResponseReceived() {
        return lastResponseReceived;
    }

    public List<String> getChatCache() {
        return chatCache;
    }

    public void clearChatCache() {
        chatCache.clear();
    }

    public void setChatCache(List<String> chatCache) {
        this.chatCache.clear();
        this.chatCache.addAll(chatCache);
    }

    public String getResponseData(String key) {
        if ("all".equals(key)) {
            if (responseDataMap == null || responseDataMap.isEmpty()) {
                return "No data available.";
            }
            return responseDataMap.toString();
        }
        if (responseDataMap.get(key) == null || responseDataMap.isEmpty()) {
            return "No data available.";
        }
        return responseDataMap.get(key).toString();
    }
}
