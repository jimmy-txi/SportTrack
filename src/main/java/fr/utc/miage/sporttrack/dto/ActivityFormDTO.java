package fr.utc.miage.sporttrack.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import fr.utc.miage.sporttrack.util.TextNormalizer;

/**
 * Data Transfer Object (DTO) used for activity creation and update form binding
 * within the SportTrack application.
 *
 * <p>This DTO replaces direct use of the Activity JPA entity as a
 * {@code @ModelAttribute} to prevent mass assignment vulnerabilities
 * (SonarQube java:S4684). It carries only the fields that are safe for
 * user input during activity form submission.</p>
 *
 * @author SportTrack Team
 */
public class ActivityFormDTO extends AbstractIdFormDTO {

    /** The unique identifier of the activity; {@code null} for a new activity, positive for an update. */
    private Integer id;

    /** The total duration of the activity, expressed in hours. */
    private double duration;

    /** The user-defined title summarising this activity. */
    private String title;

    /** An optional textual description providing additional details about the activity. */
    private String description;

    /** The number of repetitions performed (applicable for repetition-based sports). */
    private Integer repetition;

    /** The total distance covered in kilometres (applicable for distance-based sports). */
    private Double distance;

    /** The calendar date on which the activity took place. */
    private LocalDate dateA;

    /** The local time at which the activity started. */
    private LocalTime startTime;

    /** The name of the city or location where the activity was performed. */
    private String locationCity;

    /** The identifier of the sport associated with this activity. */
    private Integer sportId;

    /**
     * Returns the unique identifier of the activity.
     *
     * @return the activity identifier, or {@code null} for a new activity
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the activity.
     *
     * @param id the activity identifier to assign
     */
    public void setId(Integer id) {
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
     * @param duration the duration in hours to assign
     */
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * Returns the title of the activity.
     *
     * @return the activity title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the activity.
     *
     * @param title the title to assign
     */
    public void setTitle(String title) {
        this.title = TextNormalizer.trimNullable(title);
    }

    /**
     * Returns the description of the activity.
     *
     * @return the activity description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the activity.
     *
     * @param description the description to assign
     */
    public void setDescription(String description) {
        this.description = TextNormalizer.trimNullable(description);
    }

    /**
     * Returns the repetition count for the activity.
     *
     * @return the repetition count, or {@code null} if not applicable
     */
    public Integer getRepetition() {
        return repetition;
    }

    /**
     * Sets the repetition count for the activity.
     *
     * @param repetition the repetition count to assign
     */
    public void setRepetition(Integer repetition) {
        this.repetition = repetition;
    }

    /**
     * Returns the distance covered during the activity.
     *
     * @return the distance in kilometres, or {@code null} if not applicable
     */
    public Double getDistance() {
        return distance;
    }

    /**
     * Sets the distance covered during the activity.
     *
     * @param distance the distance in kilometres to assign
     */
    public void setDistance(Double distance) {
        this.distance = distance;
    }

    /**
     * Returns the date of the activity.
     *
     * @return the activity date
     */
    public LocalDate getDateA() {
        return dateA;
    }

    /**
     * Sets the date of the activity.
     *
     * @param dateA the activity date to assign
     */
    public void setDateA(LocalDate dateA) {
        this.dateA = dateA;
    }

    /**
     * Returns the start time of the activity.
     *
     * @return the start time
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time of the activity.
     *
     * @param startTime the start time to assign
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the city or location name where the activity was performed.
     *
     * @return the location city name
     */
    public String getLocationCity() {
        return locationCity;
    }

    /**
     * Sets the city or location name where the activity was performed.
     *
     * @param locationCity the location city name to assign
     */
    public void setLocationCity(String locationCity) {
        this.locationCity = TextNormalizer.trimNullable(locationCity);
    }

    /**
     * Returns the identifier of the associated sport.
     *
     * @return the sport identifier
     */
    public Integer getSportId() {
        return sportId;
    }

    /**
     * Sets the identifier of the associated sport.
     *
     * @param sportId the sport identifier to assign
     */
    public void setSportId(Integer sportId) {
        this.sportId = sportId;
    }
}