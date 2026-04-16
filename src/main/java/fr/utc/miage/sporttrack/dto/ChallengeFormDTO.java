package fr.utc.miage.sporttrack.dto;

import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) used for challenge creation and update form binding
 * within the SportTrack application.
 *
 * <p>This DTO replaces direct use of the Challenge JPA entity as a
 * {@code @ModelAttribute} to prevent mass assignment vulnerabilities
 * (SonarQube java:S4684). It carries only the fields that are safe for
 * user input during challenge form submission.</p>
 *
 * @author SportTrack Team
 */
public class ChallengeFormDTO {

    /** The display name of the challenge. */
    private String name;

    /** A textual description outlining the rules or objectives of the challenge. */
    private String description;

    /** The date on which the challenge begins. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** The date on which the challenge ends and final results are computed. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    /** The performance metric used to rank participants in this challenge. */
    private Metric metric;

    // --- Getters ---

    /**
     * Returns the display name of the challenge.
     *
     * @return the challenge name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the textual description of the challenge.
     *
     * @return the challenge description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the start date of the challenge.
     *
     * @return the start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Returns the end date of the challenge.
     *
     * @return the end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Returns the performance metric for ranking in this challenge.
     *
     * @return the {@link Metric}
     */
    public Metric getMetric() {
        return metric;
    }

    // --- Setters ---

    /**
     * Sets the display name of the challenge.
     *
     * @param name the challenge name to assign
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the textual description of the challenge.
     *
     * @param description the description to assign
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the start date of the challenge.
     *
     * @param startDate the start date to assign
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Sets the end date of the challenge.
     *
     * @param endDate the end date to assign
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Sets the performance metric for ranking in this challenge.
     *
     * @param metric the {@link Metric} to assign
     */
    public void setMetric(Metric metric) {
        this.metric = metric;
    }
}