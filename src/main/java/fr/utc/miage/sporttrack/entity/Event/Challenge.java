package fr.utc.miage.sporttrack.entity.event;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;

import java.util.List;

@Entity
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idC;
    private String nom;
    private String description;

    @ElementCollection
    private List<Athlete> participants;

    public Challenge() {}

    public Athlete getOrganizer() {
        return null;
    }

    public void setOrganizer(Athlete organizer) {}

    public void setSport(Sport sport) {}
}
