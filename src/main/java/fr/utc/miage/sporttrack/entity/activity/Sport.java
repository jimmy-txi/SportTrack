package fr.utc.miage.sporttrack.entity.activity;

import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import fr.utc.miage.sporttrack.util.TextNormalizer;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String description;
    private double caloriesPerHour;
    private SportType type;
    private boolean active = true;

    public Sport() {
        // Required by JPA
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public SportType getType() {
        return type;
    }

    public double getCaloriesPerHour() {
        return caloriesPerHour;
    }

    public boolean isActive() {
        return active;
    }
}
