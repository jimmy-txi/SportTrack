package fr.utc.miage.sporttrack.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import fr.utc.miage.sporttrack.util.TextNormalizer;

public class ActivityFormDTO extends AbstractIdFormDTO {

    private double duration;
    private String title;
    private String description;
    private Integer repetition;
    private Double distance;
    private LocalDate dateA;
    private LocalTime startTime;
    private String locationCity;
    private Integer sportId;

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = TextNormalizer.trimNullable(title);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = TextNormalizer.trimNullable(description);
    }

    public Integer getRepetition() {
        return repetition;
    }

    public void setRepetition(Integer repetition) {
        this.repetition = repetition;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public LocalDate getDateA() {
        return dateA;
    }

    public void setDateA(LocalDate dateA) {
        this.dateA = dateA;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public String getLocationCity() {
        return locationCity;
    }

    public void setLocationCity(String locationCity) {
        this.locationCity = TextNormalizer.trimNullable(locationCity);
    }

    public Integer getSportId() {
        return sportId;
    }

    public void setSportId(Integer sportId) {
        this.sportId = sportId;
    }
}