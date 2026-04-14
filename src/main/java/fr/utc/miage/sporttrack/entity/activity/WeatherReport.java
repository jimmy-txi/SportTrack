package fr.utc.miage.sporttrack.entity.activity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Column;

@Entity
public class WeatherReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String city;
    private String country;

    @Column(name = "weather_code")
    private Integer weatherCode;

    @Column(name = "precipitation_sum")
    private double precipitationSum;

    @OneToOne
    @JoinColumn(name = "activity_id", nullable = false, unique = true)
    private Activity activity;

    public WeatherReport() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(Integer weatherCode) {
        this.weatherCode = weatherCode;
    }

    public double getPrecipitationSum() {
        return precipitationSum;
    }

    public void setPrecipitationSum(double precipitationSum) {
        this.precipitationSum = precipitationSum;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public boolean isSunny() {
        if (weatherCode == null) {
            return precipitationSum <= 0d;
        }

        return weatherCode == 0 || weatherCode == 1 || weatherCode == 2;
    }

    public boolean isRainy() {
        if (weatherCode == null) {
            return precipitationSum > 0d;
        }

        return (weatherCode >= 51 && weatherCode <= 67)
                || (weatherCode >= 71 && weatherCode <= 77)
                || (weatherCode >= 80 && weatherCode <= 99);
    }

    public boolean isCloudyWeather() {
        return weatherCode != null && (weatherCode == 3 || weatherCode == 45 || weatherCode == 48);
    }

    public String getWeatherLabel() {
        if (isRainy()) {
            return "Pluie";
        }

        if (isCloudyWeather()) {
            return "Nuageux";
        }

        return "Ensoleillé";
    }

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