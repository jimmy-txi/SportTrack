package fr.utc.miage.sporttrack.dto;

import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import fr.utc.miage.sporttrack.util.TextNormalizer;

/**
 * Data Transfer Object (DTO) used for sport creation and update form binding
 * within the SportTrack application.
 *
 * <p>This DTO replaces direct use of the Sport JPA entity as a
 * {@code @ModelAttribute} to prevent mass assignment vulnerabilities
 * (SonarQube java:S4684). An {@code id} of {@code 0} indicates a new sport
 * creation, while a positive {@code id} indicates an update to an existing sport.</p>
 *
 * @author SportTrack Team
 */
public class SportFormDTO extends AbstractIdFormDTO {

    /** The display name of the sport (e.g., "Running", "Swimming"). */
    private String name;

    /** A textual description of the sport. */
    private String description;

    /** The average number of calories burned per hour of practice for this sport. */
    private double caloriesPerHour;

    /** The measurement type that defines how performance is tracked for this sport. */
    private SportType type;

    // --- Getters ---

    /**
     * Returns the display name of the sport.
     *
     * @return the sport name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the textual description of the sport.
     *
     * @return the sport description
     */
    public String getDescription() {
        return description;
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
     * Returns the measurement type of this sport.
     *
     * @return the {@link SportType}
     */
    public SportType getType() {
        return type;
    }

    // --- Setters ---

    /**
     * Sets the display name of the sport.
     *
     * @param name the sport name to assign
     */
    public void setName(String name) {
        this.name = TextNormalizer.trimNullable(name);
    }

    /**
     * Sets the textual description of the sport.
     *
     * @param description the description to assign
     */
    public void setDescription(String description) {
        this.description = TextNormalizer.trimNullable(description);
    }

    /**
     * Sets the average calorie burn rate per hour for this sport.
     *
     * @param caloriesPerHour the calories burned per hour to assign
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
}
