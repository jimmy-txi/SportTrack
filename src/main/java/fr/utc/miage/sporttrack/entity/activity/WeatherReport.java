package fr.utc.miage.sporttrack.entity.activity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Column;

/**
 * JPA entity representing the weather conditions associated with a given
 * {@link Activity} within the SportTrack application.
 *
 * <p>The report captures the city, country, WMO weather code, and total
 * precipitation. Convenience methods are provided to classify the weather
 * as sunny, rainy, or cloudy and to obtain a display label and icon class
 * for the user interface.</p>
 *
 * @author SportTrack Team
 */
@Entity
public class WeatherReport {

    /** WMO weather code for clear sky. */
    private static final int WEATHER_CODE_CLEAR = 0;
    
    /** WMO weather code for mainly clear. */
    private static final int WEATHER_CODE_MAINLY_CLEAR = 1;
    
    /** WMO weather code for partly cloudy. */
    private static final int WEATHER_CODE_PARTLY_CLOUDY = 2;
    
    /** WMO weather code for overcast. */
    private static final int WEATHER_CODE_OVERCAST = 3;
    
    /** WMO weather code for foggy. */
    private static final int WEATHER_CODE_FOG = 45;
    
    /** WMO weather code for depositing rime fog. */
    private static final int WEATHER_CODE_RIME_FOG = 48;
    
    /** Minimum WMO weather code for rain/snow (drizzle). */
    private static final int WEATHER_CODE_RAIN_MIN = 51;
    
    /** Maximum WMO weather code for rain/snow (drizzle). */
    private static final int WEATHER_CODE_RAIN_DRIZZLE_MAX = 67;
    
    /** Minimum WMO weather code for rain/snow (rain/snow). */
    private static final int WEATHER_CODE_RAIN_SNOW_MIN = 71;
    
    /** Maximum WMO weather code for rain/snow (rain/snow). */
    private static final int WEATHER_CODE_RAIN_SNOW_MAX = 77;
    
    /** Minimum WMO weather code for rain/snow (showers). */
    private static final int WEATHER_CODE_SHOWERS_MIN = 80;
    
    /** Maximum WMO weather code for rain/snow (showers). */
    private static final int WEATHER_CODE_SHOWERS_MAX = 99;
    
    /** Threshold for no precipitation. */
    private static final double NO_PRECIPITATION_THRESHOLD = 0d;

    /** The unique database-generated identifier for this weather report. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** The name of the city where the weather data was recorded. */
    private String city;

    /** The country code or name where the weather data was recorded. */
    private String country;

    /** The WMO weather code describing the general weather conditions. */
    @Column(name = "weather_code")
    private Integer weatherCode;

    /** The total precipitation sum (in millimetres) for the day of the activity. */
    @Column(name = "precipitation_sum")
    private double precipitationSum;

    /** The activity to which this weather report is linked. */
    @OneToOne
    @JoinColumn(name = "activity_id", nullable = false, unique = true)
    private Activity activity;

    /**
     * No-argument constructor required by JPA.
     */
    public WeatherReport() {
        // Default constructor for JPA
    }

    /**
     * Returns the unique identifier of this weather report.
     *
     * @return the database identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this weather report.
     *
     * @param id the database identifier to assign
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the city name associated with this weather report.
     *
     * @return the city name
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city name associated with this weather report.
     *
     * @param city the city name to assign
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Returns the country associated with this weather report.
     *
     * @return the country code or name
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the country associated with this weather report.
     *
     * @param country the country code or name to assign
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Returns the WMO weather code for this report.
     *
     * @return the weather code, or {@code null} if unavailable
     */
    public Integer getWeatherCode() {
        return weatherCode;
    }

    /**
     * Sets the WMO weather code for this report.
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
    public double getPrecipitationSum() {
        return precipitationSum;
    }

    /**
     * Sets the total precipitation sum.
     *
     * @param precipitationSum the precipitation in millimetres to assign
     */
    public void setPrecipitationSum(double precipitationSum) {
        this.precipitationSum = precipitationSum;
    }

    /**
     * Returns the activity to which this weather report is linked.
     *
     * @return the associated {@link Activity}
     */
    public Activity getActivity() {
        return activity;
    }

    /**
     * Sets the activity to which this weather report is linked.
     *
     * @param activity the {@link Activity} to associate
     */
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * Determines whether the weather conditions are sunny.
     * Falls back to a precipitation-based check when the weather code is unavailable.
     *
     * @return {@code true} if conditions are sunny, {@code false} otherwise
     */
    public boolean isSunny() {
        if (weatherCode == null) {
            return precipitationSum <= NO_PRECIPITATION_THRESHOLD;
        }

        return weatherCode == WEATHER_CODE_CLEAR || weatherCode == WEATHER_CODE_MAINLY_CLEAR || weatherCode == WEATHER_CODE_PARTLY_CLOUDY;
    }

    /**
     * Determines whether the weather conditions are rainy or snowy.
     * Falls back to a precipitation-based check when the weather code is unavailable.
     *
     * @return {@code true} if conditions indicate rain or snow, {@code false} otherwise
     */
    public boolean isRainy() {
        if (weatherCode == null) {
            return precipitationSum > NO_PRECIPITATION_THRESHOLD;
        }

        return (weatherCode >= WEATHER_CODE_RAIN_MIN && weatherCode <= WEATHER_CODE_RAIN_DRIZZLE_MAX)
                || (weatherCode >= WEATHER_CODE_RAIN_SNOW_MIN && weatherCode <= WEATHER_CODE_RAIN_SNOW_MAX)
                || (weatherCode >= WEATHER_CODE_SHOWERS_MIN && weatherCode <= WEATHER_CODE_SHOWERS_MAX);
    }

    /**
     * Determines whether the weather conditions are cloudy or foggy.
     *
     * @return {@code true} if conditions are cloudy, {@code false} otherwise
     */
    public boolean isCloudyWeather() {
        return weatherCode != null && (weatherCode == WEATHER_CODE_OVERCAST || weatherCode == WEATHER_CODE_FOG || weatherCode == WEATHER_CODE_RIME_FOG);
    }

    /**
     * Returns a French-language label describing the current weather conditions.
     *
     * @return "Pluie" for rain, "Nuageux" for cloudy, or "Ensoleillé" for sunny
     */
    public String getWeatherLabel() {
        if (isRainy()) {
            return "Pluie";
        }

        if (isCloudyWeather()) {
            return "Nuageux";
        }

        return "Ensoleillé";
    }

    /**
     * Returns a Bootstrap Icons CSS class name suitable for rendering a weather icon
     * in the user interface.
     *
     * @return the icon CSS class: "bi-cloud-rain-fill", "bi-cloud-fill", or "bi-sun-fill"
     */
    public String getWeatherIconClass() {
        if (isRainy()) {
            return "bi-cloud-rain-fill";
        }

        if (isCloudyWeather()) {
            return "bi-cloud-fill";
        }

        return "bi-sun-fill";
    }
}