package fr.utc.miage.sporttrack.entity.activity;

import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idS;
    private String name;
    private String description;
    private double caloriesPerHour;
    private SportType type;

    public Sport() {}

    public int getIdS() {
        return idS;
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

    public double getHourlyCalories() {
        return caloriesPerHour;
    }
}
