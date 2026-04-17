package fr.utc.miage.sporttrack.dto;

import fr.utc.miage.sporttrack.util.TextNormalizer;

/**
 * Data Transfer Object (DTO) representing weather (meteorological) data
 * retrieved from an external weather API within the SportTrack application.
 *
 * <p>This DTO carries the weather information for a given location, including
 * a validity flag and an informational message indicating whether the data
 * was successfully fetched.</p>
 *
 * @author SportTrack Team
 */
public class MeteoDTO {

    /** The name of the city for which the weather data was retrieved. */
    private String city;

    /** The country code or name for the weather data location. */
    private String country;

    /** The WMO weather code describing the general weather conditions. */
    private Integer weatherCode;

    /** The total precipitation sum (in millimetres) for the queried day. */
    private Double precipitationSum;

    /** Indicates whether the weather data was successfully retrieved and is valid. */
    private boolean valid;

    /** An informational message describing the result of the weather data retrieval. */
    private String message;

    /**
     * Returns the city name for the weather data.
     *
     * @return the city name
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city name for the weather data.
     *
     * @param city the city name to assign
     */
    public void setCity(String city) {
        this.city = TextNormalizer.trimNullable(city);
    }

    /**
     * Returns the country for the weather data.
     *
     * @return the country code or name
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the country for the weather data.
     *
     * @param country the country code or name to assign
     */
    public void setCountry(String country) {
        this.country = TextNormalizer.trimNullable(country);
    }

    /**
     * Returns the WMO weather code.
     *
     * @return the weather code, or {@code null} if unavailable
     */
    public Integer getWeatherCode() {
        return weatherCode;
    }

    /**
     * Sets the WMO weather code.
     *
     * @param weatherCode the weather code to assign
     */
    public void setWeatherCode(Integer weatherCode) {
        this.weatherCode = weatherCode;
    }

    /**
     * Returns the total precipitation sum.
     *
     * @return the precipitation in millimetres
     */
    public Double getPrecipitationSum() {
        return precipitationSum;
    }

    /**
     * Sets the total precipitation sum.
     *
     * @param precipitationSum the precipitation in millimetres to assign
     */
    public void setPrecipitationSum(Double precipitationSum) {
        this.precipitationSum = precipitationSum;
    }

    /**
     * Returns whether the weather data is valid.
     *
     * @return {@code true} if the data was successfully retrieved, {@code false} otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets whether the weather data is valid.
     *
     * @param valid {@code true} if valid, {@code false} otherwise
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * Returns the informational message about the weather data retrieval.
     *
     * @return the result message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the informational message about the weather data retrieval.
     *
     * @param message the result message to assign
     */
    public void setMessage(String message) {
        this.message = TextNormalizer.trimNullable(message);
    }
}