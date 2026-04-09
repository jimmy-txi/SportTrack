package fr.utc.miage.sporttrack.entity.Event;

import fr.utc.miage.sporttrack.entity.Activity.Sport;

public class Badge {

    private int id;
    private String label;
    private String description;
    private boolean makeItBoolean;

    public Badge() {}

    public Badge(String label, String description, Sport sport) {}
}
