package fr.utc.miage.sporttrack.dto;

import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import fr.utc.miage.sporttrack.util.TextNormalizer;

/**
 * DTO used for sport creation/update form binding.
 * Replaces direct use of the Sport JPA entity as a @ModelAttribute
 * to prevent mass assignment vulnerabilities (SonarQube java:S4684).
 * id == 0 means creation; id > 0 means update.
 */
public class SportFormDTO extends AbstractIdFormDTO {

    private String name;
    private String description;
    private double caloriesPerHour;
    private SportType type;

    // --- Getters ---

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getCaloriesPerHour() {
        return caloriesPerHour;
    }

    public SportType getType() {
        return type;
    }

    // --- Setters ---

    public void setName(String name) {
        this.name = TextNormalizer.trimNullable(name);
    }

    public void setDescription(String description) {
        this.description = TextNormalizer.trimNullable(description);
    }

    public void setCaloriesPerHour(double caloriesPerHour) {
        this.caloriesPerHour = caloriesPerHour;
    }

    public void setType(SportType type) {
        this.type = type;
    }
}
