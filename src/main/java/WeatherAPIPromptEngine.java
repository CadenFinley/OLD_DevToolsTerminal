
import java.io.BufferedReader;
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

    private String latitude = "32.4487";
    private String longitude = "99.7331";
    private Map<String, Object> weatherData;
    private Object timeDataLastGathered;

    public WeatherAPIPromptEngine() {
        refreshWeather();
    }

    public void refreshWeather() {
        weatherAPI();
    }

    public Map<String, Object> getWeatherData() {
        return weatherData;
    }

    public String getWeatherDataPart(String key) {
        if ("all".equals(key)) {
            return getWeatherData().toString();
        }
        if (weatherData.get(key) == null) {
            return "No weather data available.";
        }
        return weatherData.get(key).toString();
    }

    private String weatherAPI() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            try {
                URL url = new URL("https://api.tomorrow.io/v4/weather/forecast?location=" + latitude + "," + longitude + "&apikey=lJJSlWZaiv9LeL1iS35I7QnuMQkPMOya");
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
                timeDataLastGathered = System.currentTimeMillis();
                return "Weather API connection successful. " + System.currentTimeMillis();
            } catch (IOException e) {
                System.out.println("Weather API: " + System.currentTimeMillis() + "An error occurred while processing the request.");
                return null;
            }
        });
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("Weather API connection timed out. " + System.currentTimeMillis());
            return null;
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Weather API: " + System.currentTimeMillis() + "An error occurred while processing the request.");
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
            System.out.println("Weather API: " + System.currentTimeMillis() + "An error occurred while processing the request.");
            return null;
        }
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public Object getTimeDataLastGathered() {
        return timeDataLastGathered;
    }
}
