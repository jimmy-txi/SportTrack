package fr.utc.miage.sporttrack.dto;

public class MeteoDTO {

    private String city;
    private String country;
    private Integer weatherCode;
    private Double precipitationSum;
    private boolean valid;
    private String message;

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

    public Double getPrecipitationSum() {
        return precipitationSum;
    }

    public void setPrecipitationSum(Double precipitationSum) {
        this.precipitationSum = precipitationSum;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}