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

@Service
public class WeatherReportService {

    private static final String GEOCODING_API = "https://geocoding-api.open-meteo.com/v1/search?name=%s&count=1&language=en&format=json";
    private static final String HISTORICAL_API = "https://archive-api.open-meteo.com/v1/archive?latitude=%s&longitude=%s&start_date=%s&end_date=%s&hourly=weather_code,precipitation&timezone=auto&format=json";
    private static final Pattern GEOCODING_LATITUDE_PATTERN = Pattern.compile("\\\"latitude\\\"\\s*:\\s*([-0-9.]+)");
    private static final Pattern GEOCODING_LONGITUDE_PATTERN = Pattern.compile("\\\"longitude\\\"\\s*:\\s*([-0-9.]+)");
    private static final Pattern GEOCODING_CITY_PATTERN = Pattern.compile("\\\"name\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern GEOCODING_COUNTRY_PATTERN = Pattern.compile("\\\"country\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");

    private final WeatherReportRepository weatherReportRepository;
    private final RestTemplate restTemplate;

    public WeatherReportService(WeatherReportRepository weatherReportRepository) {
        this.weatherReportRepository = weatherReportRepository;
        this.restTemplate = new RestTemplate();
    }

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

    public Optional<WeatherReport> findByActivityId(int activityId) {
        return weatherReportRepository.findByActivity_Id(activityId);
    }

    private String readResponse(String url) throws IOException {
        String response = restTemplate.getForObject(url, String.class);
        if (response == null) {
            throw new IOException("Empty response");
        }
        return response;
    }

    private String encode(String value) {
        return value == null ? "" : URLEncoder.encode(value.trim(), StandardCharsets.UTF_8);
    }

    private Double extractDouble(String source, Pattern pattern) {
        Matcher matcher = pattern.matcher(source);
        return matcher.find() ? Double.valueOf(matcher.group(1)) : null;
    }

    private String extractString(String source, Pattern pattern) {
        Matcher matcher = pattern.matcher(source);
        return matcher.find() ? matcher.group(1) : null;
    }

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

    private Integer firstHourlyInt(String source, String fieldName) {
        Pattern pattern = Pattern.compile("\\\"" + fieldName + "\\\"\\s*:\\s*\\[(?:\\s*null\\s*|\\s*)(-?[0-9]+)");
        Matcher matcher = pattern.matcher(source);
        return matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
    }

}