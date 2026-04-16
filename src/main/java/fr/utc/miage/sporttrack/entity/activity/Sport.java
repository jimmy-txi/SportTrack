package fr.utc.miage.sporttrack.entity.activity;

import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * JPA entity representing a sport discipline within the SportTrack application.
 *
 * <p>Each sport is characterised by a name, a description, a calorie burn rate
 * per hour, a measurement type (duration, repetition, or distance), and an
 * active flag that determines whether the sport is currently available for
 * selection when creating new activities.</p>
 *
 * @author SportTrack Team
 */
@Entity
public class Sport {

    /** The unique database-generated identifier for this sport. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** The display name of the sport (e.g., "Running", "Swimming"). */
    private String name;

    /** A textual description of the sport. */
    private String description;

    /** The average number of calories burned per hour of practice for this sport. */
    private double caloriesPerHour;

    /** The measurement type that defines how performance is tracked for this sport. */
    private SportType type;

    /** Indicates whether this sport is currently active and available for selection. */
    private boolean active = true;

    /**
     * No-argument constructor required by JPA.
     */
    public Sport() {}

    /**
     * Sets the unique identifier of this sport.
     *
     * @param id the database identifier to assign
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the display name of this sport.
     *
     * @param name the sport name to assign
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the textual description of this sport.
     *
     * @param description the description to assign
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the average calorie burn rate per hour for this sport.
     *
     * @param caloriesPerHour the calories burned per hour
     */
    public void setCaloriesPerHour(double caloriesPerHour) {
        this.caloriesPerHour = caloriesPerHour;
    }

    /**
     * Sets the measurement type for this sport.
     *
     * @param type the {@link SportType} to assign
     */
    public void setType(SportType type) {
        this.type = type;
    }

    /**
     * Sets whether this sport is active and available for selection.
     *
     * @param active {@code true} to activate, {@code false} to deactivate
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the unique identifier of this sport.
     *
     * @return the sport's database identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the display name of this sport.
     *
     * @return the sport name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the textual description of this sport.
     *
     * @return the sport description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the measurement type of this sport.
     *
     * @return the {@link SportType}
     */
    public SportType getType() {
        return type;
    }

    /**
     * Returns the average calorie burn rate per hour for this sport.
     *
     * @return the calories burned per hour
     */
    public double getCaloriesPerHour() {
        return caloriesPerHour;
    }

    /**
     * Returns whether this sport is currently active and available for selection.
     *
     * @return {@code true} if the sport is active, {@code false} otherwise
     */
    public boolean isActive() {
        return active;
    }
}