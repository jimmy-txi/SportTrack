package fr.utc.miage.sporttrack.entity.activity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import fr.utc.miage.sporttrack.entity.user.Athlete;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private double duration;
    private String title;
    private String description;
    private Integer repetition;
    private Double distance;
    private LocalDate dateA;
    private LocalTime startTime;
    private String locationCity;

    @ManyToOne
    @JoinColumn(name = "sport_id")
    private Sport sportAndType;

    @ManyToOne
    @JoinColumn(name = "created_by_athlete_id")
    private Athlete createdBy;

    @OneToOne(mappedBy = "activity")
    private WeatherReport weatherReport;

    @Transient
    private Integer sportId;

    @Transient
    private java.util.List<fr.utc.miage.sporttrack.entity.user.communication.Comment> comments = new java.util.ArrayList<>();

    public Activity() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        this.locationCity = locationCity;
    }

    public Athlete getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Athlete createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByDisplayName() {
        if (createdBy == null) {
            return "Inconnu";
        }

        String firstName = createdBy.getFirstName() != null ? createdBy.getFirstName().trim() : "";
        String lastName = createdBy.getLastName() != null ? createdBy.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();

        if (!fullName.isEmpty()) {
            return fullName;
        }

        if (createdBy.getUsername() != null && !createdBy.getUsername().isBlank()) {
            return createdBy.getUsername();
        }

        return createdBy.getEmail() != null ? createdBy.getEmail() : "Inconnu";
    }

    public Sport getSportAndType() {
        return sportAndType;
    }

    public void setSportAndType(Sport sport) {
        this.sportAndType = sport;
        this.sportId = (sport != null ? sport.getId() : null);
    }

    public Integer getSportId() {
        if (sportId != null) {
            return sportId;
        }
        return sportAndType != null ? sportAndType.getId() : null;
    }

    public void setSportId(Integer sportId) {
        this.sportId = sportId;
        if (sportId == null || sportId <= 0) {
            this.sportAndType = null;
            return;
        }
        Sport sport = new Sport();
        sport.setId(sportId);
        this.sportAndType = sport;
    }

    public boolean hasRepetitions() {
        return sportAndType != null && sportAndType.getType() != null
                && sportAndType.getType().name().equals("REPETITION");
    }

    public boolean hasDistance() {
        return sportAndType != null && sportAndType.getType() != null
                && sportAndType.getType().name().equals("DISTANCE");
    }

    public WeatherReport getWeatherReport() {
        return weatherReport;
    }

    public void setWeatherReport(WeatherReport weatherReport) {
        this.weatherReport = weatherReport;
    }

    public double getCaloriesBurned() {
        if (duration <= 0 || sportAndType == null) {
            return 0d;
        }
        return duration * sportAndType.getCaloriesPerHour();
    }

    public java.util.List<fr.utc.miage.sporttrack.entity.user.communication.Comment> getComments() {
        return comments;
    }

    public void setComments(java.util.List<fr.utc.miage.sporttrack.entity.user.communication.Comment> comments) {
        this.comments = comments;
    }
}
