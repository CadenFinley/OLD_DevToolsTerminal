
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
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
 * The WeatherAPIPromptEngine class generates prompts using an TOMORROW.IO API
 * key.
 *
 * @author Caden Finley
 * @version 1.0
 */
public final class WeatherAPIPromptEngine {

    private static final Console console = System.console();
    private String latitude = "40.712776";
    private String longitude = "-74.005974";
    private String URL_TO_TOMORROW_IO = "https://api.tomorrow.io/v4/weather/forecast?location=" + latitude + "," + longitude + "&apikey=lJJSlWZaiv9LeL1iS35I7QnuMQkPMOya";
    private Map<String, Object> weatherData;

    public WeatherAPIPromptEngine() {
        recallWeather();
    }

    public void recallWeather() {
        weatherAPI();
    }

    public void setLocation() throws InterruptedException {
        String bufferLatitude;
        String bufferLongitude;
        while (true) {
            TextEngine.printWithDelays("Please enter your latitude.", true);
            bufferLatitude = console.readLine();
            TextEngine.printWithDelays("Please enter your longitude.", true);
            bufferLongitude = console.readLine();
            if (latitude == null || longitude == null) {
                TextEngine.printWithDelays("Invalid input. Please try again.", true);
            }
            if (latitude.isEmpty() || longitude.isEmpty()) {
                TextEngine.printWithDelays("Invalid input. Please try again.", true);
            }
            try {
                Float.valueOf(bufferLatitude);
                Float.valueOf(bufferLongitude);
                break;
            } catch (NumberFormatException e) {
                TextEngine.printWithDelays("Invalid input. Please try again.", true);
            }
        }
        this.latitude = bufferLatitude;
        this.longitude = bufferLongitude;
        this.URL_TO_TOMORROW_IO = "https://api.tomorrow.io/v4/weather/forecast?location=" + latitude + "," + longitude + "&apikey=lJJSlWZaiv9LeL1iS35I7QnuMQkPMOya";
    }

    public Map<String, Object> getWeatherData() {
        return weatherData;
    }

    public Object getWeatherDataPart(String key) {
        return weatherData.get(key);
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
                weatherData = extractWeatherData(response.toString());
                return "Weather API connection successful.";
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

    public Map<String, Object> extractWeatherData(String jsonResponse) {
        try {
            Map<String, Object> dataMap = new HashMap<>();
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject values = jsonObject.getJSONObject("timelines").getJSONArray("minutely").getJSONObject(0).getJSONObject("values");

            dataMap.put("cloudBase", values.opt("cloudBase"));
            dataMap.put("cloudCeiling", values.opt("cloudCeiling"));
            dataMap.put("cloudCover", values.optDouble("cloudCover"));
            dataMap.put("dewPoint", values.optDouble("dewPoint"));
            dataMap.put("freezingRainIntensity", values.optDouble("freezingRainIntensity"));
            dataMap.put("hailProbability", values.optDouble("hailProbability"));
            dataMap.put("hailSize", values.optDouble("hailSize"));
            dataMap.put("humidity", values.optInt("humidity"));
            dataMap.put("precipitationProbability", values.optDouble("precipitationProbability"));
            dataMap.put("pressureSurfaceLevel", values.optDouble("pressureSurfaceLevel"));
            dataMap.put("rainIntensity", values.optDouble("rainIntensity"));
            dataMap.put("sleetIntensity", values.optDouble("sleetIntensity"));
            dataMap.put("snowIntensity", values.optDouble("snowIntensity"));
            dataMap.put("temperature", values.optDouble("temperature"));
            dataMap.put("temperatureApparent", values.optDouble("temperatureApparent"));
            dataMap.put("uvHealthConcern", values.optDouble("uvHealthConcern"));
            dataMap.put("uvIndex", values.optDouble("uvIndex"));
            dataMap.put("visibility", values.optDouble("visibility"));
            dataMap.put("weatherCode", values.optInt("weatherCode"));
            dataMap.put("windDirection", values.optDouble("windDirection"));
            dataMap.put("windGust", values.optDouble("windGust"));
            dataMap.put("windSpeed", values.optDouble("windSpeed"));

            return dataMap;
        } catch (JSONException e) {
            System.out.println("An error occurred while processing the request.");
            return null;
        }
    }
}
