package dk.mathiasS.weatherApp.app;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

// retreive weather data from API - this backend logic will fetch the latest weather
// data from the external API and return it. The GUI will
// display this data to the user
public class WeatherApp {
    // fetch weather data for given location
    public static JSONObject getWeatherData(String locationName){
        // get location coordinates using the geolocation API
        if (locationName.contains("findes ikke") || locationName.contains(" Søg her")) return null;
        JSONArray locationData = getLocationData(locationName);

        if(locationData==null || locationData.isEmpty()){
            return null;
        }

        // extract latitude and longitude data
        JSONObject location = (JSONObject) locationData.get(0);

        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        // build API request URL with location coordinates
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=is_day,precipitation_probability,rain,temperature_2m&minutely_15=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m&wind_speed_unit=ms&timezone=Europe%2FCopenhagen&daily=uv_index_max,precipitation_probability_mean,sunset,sunrise&forecast_days=1";

        try{
            // call api and get response
            HttpURLConnection conn = fetchApiResponse(urlString);

            // check for response status
            // 200 - means that the connection was a success
            if(conn.getResponseCode() != 200){
                System.out.println("Error: Could not connect to API");
                return null;
            }

            // store resulting json data
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while(scanner.hasNext()){
                // read and store into the string builder
                resultJson.append(scanner.nextLine());
            }

            // close scanner
            scanner.close();

            // close url connection
            conn.disconnect();

            // parse through our data
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            // retrieve minutely data
            JSONObject minutely = (JSONObject) resultJsonObj.get("minutely_15");

            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");
            JSONArray timeOfDay = (JSONArray) hourly.get("is_day");

            JSONArray temperatures = ((JSONArray) hourly.get("temperature_2m"));

            // Convert the JSONArray to a List<Integer>
            List<Double> numbers = new ArrayList<>();
            for (int i = 0; i < temperatures.size(); i++) {
                numbers.add((double) temperatures.get(i));
            }
            Double highestTemperature = Collections.max(numbers);
            Double lowestTemperature = Collections.min(numbers);

            boolean is_day = ((Long) timeOfDay.get(getHour()) == 1);

            JSONObject daily = (JSONObject) resultJsonObj.get("daily");
            JSONArray uvData = (JSONArray) daily.get("uv_index_max");

            double uv_index=((double)uvData.get(0));

            String city = location.get("name") + ", " + location.get("country_code");

            // we want to get the current hour's data,
            // so we need to get the index of our current hour
            JSONArray time = (JSONArray) minutely.get("time");
            int index = findIndexOfCurrentTime(time);

            double rain = (double) ((JSONArray) hourly.get("rain")).get(new Date().getHours()-1);
            long possibility = (Long) ((JSONArray)hourly.get("precipitation_probability")).get(new Date().getHours());

            // get temperature
            JSONArray temperatureData = (JSONArray) minutely.get("temperature_2m");
            double temperature = (double) temperatureData.get(index);

            JSONArray humidityData = (JSONArray) minutely.get("relativehumidity_2m");
            long humidity = (long) humidityData.get(index);

            // get weather code
            JSONArray weathercode = (JSONArray) minutely.get("weathercode");
            String weatherCondition = convertWeatherCode((long) weathercode.get(index));

            // get windspeed
            JSONArray windspeedData = (JSONArray) minutely.get("windspeed_10m");
            double windspeed = (double) windspeedData.get(index);

            String sunrise_date = (daily.get("sunrise") + "").replace("[\"", "").replace("\"]", "");
            String sunset_date = (daily.get("sunset") + "").replace("[\"", "").replace("\"]", "");

            LocalDateTime sunrise_dateTime = LocalDateTime.parse(sunrise_date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime sunset_dateTime = LocalDateTime.parse(sunset_date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            String sunrise = sunrise_dateTime.format(timeFormatter);
            String sunset = sunset_dateTime.format(timeFormatter);

            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("windspeed", windspeed);
            weatherData.put("humidity", humidity);
            weatherData.put("uv_index", uv_index);
            weatherData.put("is_day", is_day);
            weatherData.put("city_name", city);
            weatherData.put("highestDegree", highestTemperature);
            weatherData.put("lowestDegree", lowestTemperature);
            weatherData.put("rain", rain);
            weatherData.put("rain_possibility", possibility);
            weatherData.put("sunrise", sunrise);
            weatherData.put("sunset", sunset);

            return weatherData;
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    // retrieves geographic coordinates for given location name
    public static JSONArray getLocationData(String locationName){
        // replace any whitespace in location name to + to adhere to API's request format
        locationName = locationName.replaceAll(" ", "+");

        // build API url with location parameter
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try{
            // call api and get a response
            HttpURLConnection conn = fetchApiResponse(urlString);

            // check response status
            // 200 means successful connection
            if(conn.getResponseCode() != 200){
                System.out.println("Error: Could not connect to API");
                return null;
            }else{
                // store the API results
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                // read and store the resulting json data into our string builder
                while(scanner.hasNext()){
                    resultJson.append(scanner.nextLine());
                }

                // close scanner
                scanner.close();

                // close url connection
                conn.disconnect();

                // parse the JSON string into a JSON obj
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                // get the list of location data the API gtenerated from the lcoation name
                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        // couldn't find location
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString){
        try{
            // attempt to create connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // set request method to get
            conn.setRequestMethod("GET");

            // connect to our API
            conn.connect();
            return conn;
        }catch(IOException e){
            e.printStackTrace();
        }

        // could not make connection
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList){
        ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneId.of("Europe/Copenhagen"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        // Get the current time in the required format
        String currentTime = currentDateTime.format(formatter);

        // iterate through the time list and see which one matches our current time
        for (int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if (time.equals(currentTime)) {
                // return the index
                return i; // bare tilføj x til index for at finde specifik hour i stedet for at manipulerer currentTime
            }
        }

        // if the exact current time is not found, find the closest time
        ZonedDateTime closestTime = null;
        int closestIndex = 0;
        for (int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            ZonedDateTime parsedTime = ZonedDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of("Europe/Copenhagen")));
            if (closestTime == null || Math.abs(currentDateTime.toEpochSecond() - parsedTime.toEpochSecond()) < Math.abs(currentDateTime.toEpochSecond() - closestTime.toEpochSecond())) {
                closestTime = parsedTime;
                closestIndex = i;
            }
        }

        return closestIndex;
    }

    static String getCurrentTime() {
        // get current date and time in Europe/Copenhagen timezone
        ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneId.of("Europe/Copenhagen"));

        // format date to be 2023-09-02T00:00 (this is how it is read in the API)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        // format and print the current date and time
        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;
    }

    // convert the weather code to something more readable
    private static String convertWeatherCode(long weathercode){
        String weatherCondition = "";
        if(weathercode == 0L) {
            // clear
            weatherCondition = "clear";
        }else if(weathercode == 1) {
            weatherCondition = "mainly_clear";
        } else if(weathercode==2) {
            weatherCondition = "partly_clear";
        }else if(weathercode == 3){
            // cloudy
            weatherCondition = "cloudy";
        }else if((weathercode >= 51L && weathercode <= 67L)
                || (weathercode >= 80L && weathercode <= 99L)){
            // rain
            weatherCondition = "rain";
        }else if(weathercode >= 71L && weathercode <= 77L){
            // snow
            weatherCondition = "Snow";
        }

        return weatherCondition;
    }

    private static int getHour(){
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);

        return calendar.get(Calendar.HOUR_OF_DAY);

    }
}







