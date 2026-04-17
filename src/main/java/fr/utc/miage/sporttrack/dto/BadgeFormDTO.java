package fr.utc.miage.sporttrack.dto;

import fr.utc.miage.sporttrack.entity.enumeration.Metric;

/**
 * Data Transfer Object (DTO) used for badge creation and update form binding
 * within the SportTrack application.
 *
 * <p>This DTO replaces direct use of the Badge JPA entity as a
 * {@code @ModelAttribute} to prevent mass assignment vulnerabilities
 * (SonarQube java:S4684). It carries only the fields that are safe for
 * admin input during badge form submission.</p>
 *
 * @author SportTrack Team
 */
public class BadgeFormDTO {

    /** The unique identifier of the badge; {@code null} for creation, positive for update. */
    private Integer id;

    /** The display label or title of the badge (e.g., "Marathon Runner"). */
    private String label;

    /** A detailed description of the criteria for earning this badge. */
    private String description;

    /** The identifier of the sport to which this badge applies, or {@code null} for universal badges. */
    private Integer sportId;

    /** The performance metric used to evaluate whether the badge threshold has been met. */
    private Metric metric;

    /** The numeric threshold that must be reached or exceeded to earn this badge. */
    private double threshold;

    /** The CSS icon class name used to render the badge visually in the user interface. */
    private String icon;

    /**
     * Returns the unique identifier of the badge.
     *
     * @return the badge identifier, or {@code null} for a new badge
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the badge.
     *
     * @param id the badge identifier to assign
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Returns the display label of the badge.
     *
     * @return the badge label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the display label of the badge.
     *
     * @param label the label to assign
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Returns the description of the badge's earning criteria.
     *
     * @return the badge description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the badge's earning criteria.
     *
     * @param description the description to assign
     */
    public void setDescription(String description) {
        this.description = description;
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

    /**
     * Returns the performance metric for this badge.
     *
     * @return the {@link Metric}
     */
    public Metric getMetric() {
        return metric;
    }

    /**
     * Sets the performance metric for this badge.
     *
     * @param metric the {@link Metric} to assign
     */
    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    /**
     * Returns the threshold value required to earn this badge.
     *
     * @return the threshold value
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * Sets the threshold value required to earn this badge.
     *
     * @param threshold the threshold to assign
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    /**
     * Returns the CSS icon class name for this badge.
     *
     * @return the icon class string
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Sets the CSS icon class name for this badge.
     *
     * @param icon the icon class string to assign
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }
}