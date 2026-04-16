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

/**
 * JPA entity representing a sport activity recorded by an athlete within the
 * SportTrack application.
 *
 * <p>An activity captures key performance data such as duration, distance,
 * repetitions, and the associated sport type. It may also reference a
 * {@link WeatherReport} and a list of {@link fr.utc.miage.sporttrack.entity.user.communication.Comment comments}
 * from other athletes.</p>
 *
 * @author SportTrack Team
 */
@Entity
public class Activity {

    /** The unique database-generated identifier for this activity. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** The total duration of the activity, expressed in hours. */
    private double duration;

    /** The user-defined title summarising this activity. */
    private String title;

    /** An optional textual description providing additional details about the activity. */
    private String description;

    /** The number of repetitions performed during the activity (applicable for repetition-based sports). */
    private Integer repetition;

    /** The total distance covered during the activity, in kilometres (applicable for distance-based sports). */
    private Double distance;

    /** The calendar date on which the activity took place. */
    private LocalDate dateA;

    /** The local time at which the activity started. */
    private LocalTime startTime;

    /** The name of the city or location where the activity was performed. */
    private String locationCity;

    /** The sport category and type associated with this activity. */
    @ManyToOne
    @JoinColumn(name = "sport_id")
    private Sport sportAndType;

    /** The athlete who created (recorded) this activity. */
    @ManyToOne
    @JoinColumn(name = "created_by_athlete_id")
    private Athlete createdBy;

    /** The weather report linked to this activity, based on its location and date. */
    @OneToOne(mappedBy = "activity")
    private WeatherReport weatherReport;

    /** Transient field used during form binding to carry the selected sport identifier. */
    @Transient
    private Integer sportId;

    /** Transient list of comments associated with this activity, populated at runtime. */
    @Transient
    private java.util.List<fr.utc.miage.sporttrack.entity.user.communication.Comment> comments = new java.util.ArrayList<>();

    /**
     * No-argument constructor required by JPA.
     */
    public Activity() {
        // normal is empty
    }

    /**
     * Returns the unique identifier of this activity.
     *
     * @return the activity's database identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this activity.
     *
     * @param id the database identifier to assign
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the total duration of the activity.
     *
     * @return the duration in hours
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Sets the total duration of the activity.
     *
     * @param duration the duration in hours
     */
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * Returns the title of this activity.
     *
     * @return the user-defined activity title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of this activity.
     *
     * @param title the title to assign
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the description of this activity.
     *
     * @return the textual description, or {@code null} if none was provided
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this activity.
     *
     * @param description the textual description to assign
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the number of repetitions performed during this activity.
     *
     * @return the repetition count, or {@code null} if not applicable
     */
    public Integer getRepetition() {
        return repetition;
    }

    /**
     * Sets the number of repetitions performed during this activity.
     *
     * @param repetition the repetition count to assign
     */
    public void setRepetition(Integer repetition) {
        this.repetition = repetition;
    }

    /**
     * Returns the distance covered during this activity.
     *
     * @return the distance in kilometres, or {@code null} if not applicable
     */
    public Double getDistance() {
        return distance;
    }

    /**
     * Sets the distance covered during this activity.
     *
     * @param distance the distance in kilometres to assign
     */
    public void setDistance(Double distance) {
        this.distance = distance;
    }

    /**
     * Returns the date on which this activity took place.
     *
     * @return the activity date
     */
    public LocalDate getDateA() {
        return dateA;
    }

    /**
     * Sets the date on which this activity took place.
     *
     * @param dateA the activity date to assign
     */
    public void setDateA(LocalDate dateA) {
        this.dateA = dateA;
    }

    /**
     * Returns the local start time of this activity.
     *
     * @return the start time
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Sets the local start time of this activity.
     *
     * @param startTime the start time to assign
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the city or location name where this activity was performed.
     *
     * @return the location city name
     */
    public String getLocationCity() {
        return locationCity;
    }

    /**
     * Sets the city or location name where this activity was performed.
     *
     * @param locationCity the location city name to assign
     */
    public void setLocationCity(String locationCity) {
        this.locationCity = locationCity;
    }

    /**
     * Returns the athlete who created this activity.
     *
     * @return the owning athlete
     */
    public Athlete getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the athlete who created this activity.
     *
     * @param createdBy the owning athlete to assign
     */
    public void setCreatedBy(Athlete createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Returns the display name of the athlete who created this activity.
     * Falls back to the username or email if the full name is unavailable.
     *
     * @return the creator's display name, or "Inconnu" if unknown
     */
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

    /**
     * Returns the sport (and its type) associated with this activity.
     *
     * @return the associated {@link Sport}
     */
    public Sport getSportAndType() {
        return sportAndType;
    }

    /**
     * Sets the sport associated with this activity and updates the transient sport identifier.
     *
     * @param sport the {@link Sport} to associate with this activity
     */
    public void setSportAndType(Sport sport) {
        this.sportAndType = sport;
        this.sportId = (sport != null ? sport.getId() : null);
    }

    /**
     * Returns the sport identifier, sourcing it from the transient field if available,
     * or from the associated {@link Sport} entity otherwise.
     *
     * @return the sport identifier, or {@code null} if no sport is associated
     */
    public Integer getSportId() {
        if (sportId != null) {
            return sportId;
        }
        return sportAndType != null ? sportAndType.getId() : null;
    }

    /**
     * Sets the sport identifier and resolves the associated {@link Sport} proxy.
     * If the provided identifier is {@code null} or non-positive, the sport association is cleared.
     *
     * @param sportId the sport identifier to assign
     */
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

    /**
     * Determines whether this activity's sport type is measured by repetitions.
     *
     * @return {@code true} if the sport type is repetition-based, {@code false} otherwise
     */
    public boolean hasRepetitions() {
        return sportAndType != null && sportAndType.getType() != null
                && sportAndType.getType().name().equals("REPETITION");
    }

    /**
     * Determines whether this activity's sport type is measured by distance.
     *
     * @return {@code true} if the sport type is distance-based, {@code false} otherwise
     */
    public boolean hasDistance() {
        return sportAndType != null && sportAndType.getType() != null
                && sportAndType.getType().name().equals("DISTANCE");
    }

    /**
     * Returns the weather report associated with this activity.
     *
     * @return the {@link WeatherReport}, or {@code null} if none is available
     */
    public WeatherReport getWeatherReport() {
        return weatherReport;
    }

    /**
     * Sets the weather report associated with this activity.
     *
     * @param weatherReport the {@link WeatherReport} to associate
     */
    public void setWeatherReport(WeatherReport weatherReport) {
        this.weatherReport = weatherReport;
    }

    /**
     * Calculates the estimated calories burned during this activity,
     * based on the duration and the sport's calories-per-hour rate.
     *
     * @return the estimated calories burned, or {@code 0.0} if the duration is
     *         invalid or no sport is associated
     */
    public double getCaloriesBurned() {
        if (duration <= 0 || sportAndType == null) {
            return 0d;
        }
        return duration * sportAndType.getCaloriesPerHour();
    }

    /**
     * Returns the transient list of comments attached to this activity.
     *
     * @return the list of {@link fr.utc.miage.sporttrack.entity.user.communication.Comment}
     */
    public java.util.List<fr.utc.miage.sporttrack.entity.user.communication.Comment> getComments() {
        return comments;
    }

    /**
     * Sets the transient list of comments attached to this activity.
     *
     * @param comments the list of comments to assign
     */
    public void setComments(java.util.List<fr.utc.miage.sporttrack.entity.user.communication.Comment> comments) {
        this.comments = comments;
    }
}