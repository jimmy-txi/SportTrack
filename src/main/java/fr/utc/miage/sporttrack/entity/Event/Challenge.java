package fr.utc.miage.sporttrack.entity;

import fr.utc.miage.sporttrack.entity.User.Athlete;

import java.util.List;

public class Challenge {

    private int idC;
    private String nom;
    private String description;
    private List<Athlete> participants;

    public Challenge() {}

    public Athlete getOrganizer() {
        return null;
    }

    public void setOrganizer(Athlete organizer) {}

    public void setSport(Sport sport) {}
}
