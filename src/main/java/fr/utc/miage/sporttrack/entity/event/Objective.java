package fr.utc.miage.sporttrack.entity.event;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

/**
 * JPA entity representing a personal objective set by an athlete within the
 * SportTrack application.
 *
 * <p>An objective defines a measurable goal associated with a specific sport
 * (e.g., "Run 50 km this month"). The athlete can mark the objective as
 * completed once the target has been reached, at which point a completion
 * timestamp is recorded.</p>
 *
 * @author SportTrack Team
 */
@Entity
public class Objective {

    /** The unique database-generated identifier for this objective. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** The display name of the objective. */
    private String name;

    /** A textual description providing details about the objective's target. */
    private String description;

    /** The athlete who owns this objective. */
    @ManyToOne
    @JoinColumn(name = "athlete_id")
    private Athlete athlete;

    /** The sport to which this objective is related. */
    @ManyToOne
    @JoinColumn(name = "sport_id")
    private Sport sport;

    /** Indicates whether this objective has been completed. */
    @Column(nullable = false)
    private boolean completed;

    /** The timestamp at which the objective was marked as completed. */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * No-argument constructor required by JPA.
     */
    public Objective() {}

    /**
     * Constructs a new objective with the specified name and description.
     *
     * @param name        the display name of the objective; must not be {@code null}
     * @param description the textual description of the objective
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public Objective(String name, String description) {
        if (name == null) {
            throw new NullPointerException("Objective name cannot be null");
        }
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the display name of this objective.
     *
     * @return the objective name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the textual description of this objective.
     *
     * @return the objective description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the textual description of this objective.
     *
     * @param description the description to assign
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the display name of this objective.
     *
     * @param name the objective name to assign
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the unique identifier of this objective.
     *
     * @return the objective's database identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the athlete who owns this objective.
     *
     * @return the owning {@link Athlete}
     */
    public Athlete getUser() {
        return athlete;
    }

    /**
     * Sets the athlete who owns this objective.
     *
     * @param athlete the owning {@link Athlete} to assign
     */
    public void setAthlete(Athlete athlete) {
        this.athlete = athlete;
    }

    /**
     * Sets the sport associated with this objective.
     *
     * @param sport the {@link Sport} to associate
     */
    public void setSport(Sport sport) {
        this.sport = sport;
    }

    /**
     * Returns the sport associated with this objective.
     *
     * @return the associated {@link Sport}
     */
    public Sport getSport() {
        return sport;
    }

    /**
     * Returns whether this objective has been completed.
     *
     * @return {@code true} if completed, {@code false} otherwise
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Returns the timestamp at which this objective was completed.
     *
     * @return the completion timestamp, or {@code null} if not yet completed
     */
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    /**
     * Sets whether this objective has been completed.
     *
     * @param completed {@code true} to mark as completed, {@code false} otherwise
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * Sets the timestamp at which this objective was completed.
     *
     * @param completedAt the completion timestamp to assign
     */
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}