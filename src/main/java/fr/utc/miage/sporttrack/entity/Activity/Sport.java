package fr.utc.miage.sporttrack.entity.Activity;

import fr.utc.miage.sporttrack.entity.Enumeration.SportType;

public class Sport {

    private int idS;
    private String name;
    private String description;
    private double caloriesPerHour;
    private SportType type;

    public Sport() {}

    public SportType getType() {
        return type;
    }

    public double getHourlyCalories() {
        return caloriesPerHour;
    }
}
