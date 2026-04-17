package fr.utc.miage.sporttrack.entity.event;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.List;

/**
 * JPA entity representing an achievement badge within the SportTrack application.
 *
 * <p>A badge defines a performance milestone tied to an optional sport and
 * measurement metric (e.g., "Run 100 km in total"). When an athlete meets or
 * exceeds the defined threshold value, the badge is awarded and linked to that
 * athlete via a many-to-many relationship. If no sport is specified, the badge
 * is universal and applies to activities of any sport.</p>
 *
 * @author SportTrack Team
 */
@Entity
@Table(name = "badges")
public class Badge {

    /** The unique database-generated identifier for this badge. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** The display label or title of the badge (e.g., "Marathon Runner"). */
    @Column(nullable = false)
    private String label;

    /** A detailed description of the criteria for earning this badge. */
    @Column(name = "description_col", columnDefinition = "TEXT")
    private String description;

    /** The sport to which this badge is applicable, or null if the badge applies to all sports. */
    @ManyToOne
    @JoinColumn(name = "sport_id")
    private Sport sport;

    /** The performance metric used to evaluate whether the badge threshold has been met. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Metric metric;

    /** The numeric threshold that must be reached or exceeded to earn this badge. */
    @Column(nullable = false)
    private double threshold;

    /** The CSS icon class name used to render the badge visually in the user interface. */
    @Column(nullable = false)
    private String icon;

    /** The list of athletes who have earned this badge. */
    @ManyToMany
    @JoinTable(
        name = "badge_athletes",
        joinColumns = @JoinColumn(name = "badge_id"),
        inverseJoinColumns = @JoinColumn(name = "athlete_id")
    )
    private List<Athlete> earnedBy;

    /**
     * No-argument constructor required by JPA.
     */
    public Badge() {
        // normal if empty
    }

    // --- Getters ---

    /**
     * Returns the unique identifier of this badge.
     *
     * @return the badge's database identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the display label of this badge.
     *
     * @return the badge label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the description of this badge's earning criteria.
     *
     * @return the badge description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the sport associated with this badge.
     *
     * @return the associated {@link Sport}
     */
    public Sport getSport() {
        return sport;
    }

    /**
     * Returns the performance metric used for this badge.
     *
     * @return the {@link Metric}
     */
    public Metric getMetric() {
        return metric;
    }

    /**
     * Returns the numeric threshold required to earn this badge.
     *
     * @return the threshold value
     */
    public double getThreshold() {
        return threshold;
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
     * Returns the list of athletes who have earned this badge.
     *
     * @return the list of {@link Athlete}s
     */
    public List<Athlete> getEarnedBy() {
        return earnedBy;
    }

    // --- Setters ---

    /**
     * Sets the unique identifier of this badge.
     *
     * @param id the database identifier to assign
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the display label of this badge.
     *
     * @param label the label to assign
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Sets the description of this badge.
     *
     * @param description the description to assign
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the sport associated with this badge.
     *
     * @param sport the {@link Sport} to associate
     */
    public void setSport(Sport sport) {
        this.sport = sport;
    }

    /**
     * Sets the performance metric used for this badge.
     *
     * @param metric the {@link Metric} to assign
     */
    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    /**
     * Sets the numeric threshold required to earn this badge.
     *
     * @param threshold the threshold value to assign
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    /**
     * Sets the CSS icon class name for this badge.
     *
     * @param icon the icon class string to assign
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * Sets the list of athletes who have earned this badge.
     *
     * @param earnedBy the list of {@link Athlete}s to assign
     */
    public void setEarnedBy(List<Athlete> earnedBy) {
        this.earnedBy = earnedBy;
    }
}