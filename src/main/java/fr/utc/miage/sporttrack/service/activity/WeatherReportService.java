package fr.utc.miage.sporttrack.service.activity;

import fr.utc.miage.sporttrack.dto.MeteoDTO;
import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.activity.WeatherReport;
import fr.utc.miage.sporttrack.repository.activity.WeatherReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service layer component responsible for managing {@link WeatherReport} entities
 * within the SportTrack application.
 *
 * <p>Integrates with the Open-Meteo Geocoding and Historical Weather APIs
 * to fetch and persist historical weather data for a given activity location and date.</p>
 *
 * @author SportTrack Team
 */
@Service
public class WeatherReportService {

    /** URL template for the Open-Meteo Geocoding API. */
    private static final String GEOCODING_API = "https://geocoding-api.open-meteo.com/v1/search?name=%s&count=1&language=en&format=json";

    /** URL template for the Open-Meteo Historical Archive API. */
    private static final String HISTORICAL_API = "https://archive-api.open-meteo.com/v1/archive?latitude=%s&longitude=%s&start_date=%s&end_date=%s&hourly=weather_code,precipitation&timezone=auto&format=json";

    /** Regex pattern for extracting latitude from the geocoding JSON response. */
    private static final Pattern GEOCODING_LATITUDE_PATTERN = Pattern.compile("\\\"latitude\\\"\\s*:\\s*([-0-9.]+)");

    /** Regex pattern for extracting longitude from the geocoding JSON response. */
    private static final Pattern GEOCODING_LONGITUDE_PATTERN = Pattern.compile("\\\"longitude\\\"\\s*:\\s*([-0-9.]+)");

    /** Regex pattern for extracting the resolved city name from the geocoding JSON response. */
    private static final Pattern GEOCODING_CITY_PATTERN = Pattern.compile("\\\"name\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");

    /** Regex pattern for extracting the country name from the geocoding JSON response. */
    private static final Pattern GEOCODING_COUNTRY_PATTERN = Pattern.compile("\\\"country\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");

    /** The repository for weather report data access. */
    private final WeatherReportRepository weatherReportRepository;

    /** The REST client used to call external weather APIs. */
    private final RestTemplate restTemplate;

    /**
     * Constructs a new {@code WeatherReportService} with the given repository.
     *
     * @param weatherReportRepository the repository for weather report data access
     */
    public WeatherReportService(WeatherReportRepository weatherReportRepository) {
        this.weatherReportRepository = weatherReportRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Retrieves historical weather data for a given city and activity date
     * by calling the Open-Meteo Geocoding and Archive APIs.
     *
     * @param cityName     the name of the city to look up
     * @param activityDate the date for which weather data is requested
     * @return a {@link MeteoDTO} populated with weather data, or a DTO with {@code valid=false} on failure
     */
    public MeteoDTO getWeatherForActivity(String cityName, LocalDate activityDate) {
        try {
            String geocodingResponse = readResponse(String.format(GEOCODING_API, encode(cityName)));
            if (geocodingResponse == null || !geocodingResponse.contains("results")) {
                MeteoDTO dto = new MeteoDTO();
                dto.setValid(false);
                dto.setMessage("City not found");
                return dto;
            }

            Double latitude = extractDouble(geocodingResponse, GEOCODING_LATITUDE_PATTERN);
            Double longitude = extractDouble(geocodingResponse, GEOCODING_LONGITUDE_PATTERN);
            String country = extractString(geocodingResponse, GEOCODING_COUNTRY_PATTERN);
            String resolvedCity = extractString(geocodingResponse, GEOCODING_CITY_PATTERN);

            if (latitude == null || longitude == null) {
                MeteoDTO dto = new MeteoDTO();
                dto.setValid(false);
                dto.setMessage("City not found");
                return dto;
            }

            String archiveResponse = readResponse(String.format(HISTORICAL_API, latitude, longitude, activityDate, activityDate));

            MeteoDTO dto = new MeteoDTO();
            dto.setCity(resolvedCity != null ? resolvedCity : cityName);
            dto.setCountry(country);
            dto.setPrecipitationSum(sumHourlyValues(archiveResponse, "precipitation"));
            dto.setWeatherCode(firstHourlyInt(archiveResponse, "weather_code"));
            dto.setValid(true);
            return dto;
        } catch (Exception exception) {
            MeteoDTO dto = new MeteoDTO();
            dto.setValid(false);
            dto.setMessage("Unable to parse weather response");
            return dto;
        }
    }

    /**
     * Refreshes (or creates) the weather report for the given activity by fetching
     * live data from the external API and persisting the result.
     *
     * @param activity the activity whose weather report should be refreshed
     * @return the updated or newly created {@link WeatherReport}
     * @throws IllegalArgumentException if the weather data cannot be retrieved
     */
    public WeatherReport refreshWeatherReport(Activity activity) {
        MeteoDTO meteoDTO = getWeatherForActivity(activity.getLocationCity(), activity.getDateA());
        if (!meteoDTO.isValid()) {
            throw new IllegalArgumentException(meteoDTO.getMessage());
        }

        WeatherReport weatherReport = weatherReportRepository.findByActivity_Id(activity.getId())
                .orElseGet(WeatherReport::new);

        weatherReport.setActivity(activity);
        weatherReport.setCity(meteoDTO.getCity());
        weatherReport.setCountry(meteoDTO.getCountry());
        weatherReport.setPrecipitationSum(meteoDTO.getPrecipitationSum() != null ? meteoDTO.getPrecipitationSum() : 0d);
        weatherReport.setWeatherCode(meteoDTO.getWeatherCode());

        return weatherReportRepository.save(weatherReport);
    }

    /**
     * Finds the weather report associated with the specified activity.
     *
     * @param activityId the unique identifier of the activity
     * @return an {@link Optional} containing the weather report if found, empty otherwise
     */
    public Optional<WeatherReport> findByActivityId(int activityId) {
        return weatherReportRepository.findByActivity_Id(activityId);
    }

    /**
     * Performs an HTTP GET request and returns the response body as a string.
     *
     * @param url the URL to request
     * @return the response body
     * @throws IOException if the response is null or a connection error occurs
     */
    private String readResponse(String url) throws IOException {
        String response = restTemplate.getForObject(url, String.class);
        if (response == null) {
            throw new IOException("Empty response");
        }
        return response;
    }

    /**
     * URL-encodes the given value using UTF-8.
     *
     * @param value the string to encode; if {@code null}, an empty string is returned
     * @return the URL-encoded string
     */
    private String encode(String value) {
        return value == null ? "" : URLEncoder.encode(value.trim(), StandardCharsets.UTF_8);
    }

    /**
     * Extracts the first double value matching the given pattern from the source string.
     *
     * @param source  the JSON response string to search
     * @param pattern the compiled regex pattern with one capture group
     * @return the extracted double value, or {@code null} if no match is found
     */
    private Double extractDouble(String source, Pattern pattern) {
        Matcher matcher = pattern.matcher(source);
        return matcher.find() ? Double.valueOf(matcher.group(1)) : null;
    }

    /**
     * Extracts the first string value matching the given pattern from the source string.
     *
     * @param source  the JSON response string to search
     * @param pattern the compiled regex pattern with one capture group
     * @return the extracted string, or {@code null} if no match is found
     */
    private String extractString(String source, Pattern pattern) {
        Matcher matcher = pattern.matcher(source);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Sums all hourly numeric values for the specified field in the JSON response.
     *
     * @param source    the JSON response string
     * @param fieldName the name of the hourly field to sum
     * @return the total sum of all non-null hourly values, or {@code null} if no values are found
     */
    private Double sumHourlyValues(String source, String fieldName) {
        Pattern pattern = Pattern.compile("\\\"" + fieldName + "\\\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);
        if (!matcher.find()) {
            return null;
        }

        String[] parts = matcher.group(1).split(",");
        double sum = 0d;
        boolean found = false;
        for (String part : parts) {
            String cleaned = part.trim();
            if (cleaned.isEmpty() || "null".equals(cleaned)) {
                continue;
            }
            sum += Double.parseDouble(cleaned);
            found = true;
        }
        return found ? sum : null;
    }

    /**
     * Extracts the first non-null integer value for the specified hourly field in the JSON response.
     *
     * @param source    the JSON response string
     * @param fieldName the name of the hourly field
     * @return the first non-null integer value, or {@code null} if none is found
     */
    private Integer firstHourlyInt(String source, String fieldName) {
        Pattern pattern = Pattern.compile("\\\"" + fieldName + "\\\"\\s*:\\s*\\[(?:\\s*null\\s*|\\s*)(-?[0-9]+)");
        Matcher matcher = pattern.matcher(source);
        return matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
    }

}