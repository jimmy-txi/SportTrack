package fr.utc.miage.sporttrack.entity.activity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import fr.utc.miage.sporttrack.entity.user.communication.Comment;
import fr.utc.miage.sporttrack.util.TextNormalizer;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import fr.utc.miage.sporttrack.entity.user.Athlete;

@Entity
public class Activity {

    private static final String UNKNOWN_LABEL = "Inconnu";

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
    private List<Comment> comments = new ArrayList<>();

    public Activity() {
        // Required by JPA
    }

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

    public Athlete getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Athlete createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByDisplayName() {
        if (createdBy == null) {
            return UNKNOWN_LABEL;
        }

        String firstName = normalizeAthletePart(createdBy.getFirstName());
        String lastName = normalizeAthletePart(createdBy.getLastName());
        String fullName = (firstName + " " + lastName).trim();

        if (!fullName.isEmpty()) {
            return fullName;
        }

        if (createdBy.getUsername() != null && !createdBy.getUsername().isBlank()) {
            return createdBy.getUsername();
        }

        return createdBy.getEmail() != null ? createdBy.getEmail() : UNKNOWN_LABEL;
    }

    public Sport getSportAndType() {
        return sportAndType;
    }

    public void setSportAndType(Sport sport) {
        this.sportAndType = sport;
        this.sportId = sport != null ? sport.getId() : null;
    }

    public Integer getSportId() {
        return sportId != null ? sportId : (sportAndType != null ? sportAndType.getId() : null);
    }

    public void setSportId(Integer sportId) {
        this.sportId = sportId;
        this.sportAndType = toSportReference(sportId);
    }

    public boolean hasRepetitions() {
        return hasSportType(SportType.REPETITION);
    }

    public boolean hasDistance() {
        return hasSportType(SportType.DISTANCE);
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

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    private boolean hasSportType(SportType expectedType) {
        return sportAndType != null && sportAndType.getType() == expectedType;
    }

    private Sport toSportReference(Integer sportId) {
        if (sportId == null || sportId <= 0) {
            return null;
        }
        Sport sport = new Sport();
        sport.setId(sportId);
        return sport;
    }

    private String normalizeAthletePart(String value) {
        return value != null ? value.trim() : "";
    }
}
