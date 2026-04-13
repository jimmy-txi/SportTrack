package fr.utc.miage.sporttrack.entity.activity;

import java.time.LocalDate;

public class Activity {

    private int idA;
    private double duration;
    private int repetition;
    private double distance;
    private LocalDate dateA;
    private String locationCity;

    public Activity() {}

    public Sport getSportAndType() {
        return null;
    }

    public void setSportAndType(Sport sport) {}

    public boolean hasRepetitions() {
        return false;
    }

    public boolean hasDistance() {
        return false;
    }
}
