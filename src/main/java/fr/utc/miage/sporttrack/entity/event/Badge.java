package fr.utc.miage.sporttrack.entity.event;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;

@Entity
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String label;
    private String description;
    private boolean makeItBoolean;

    public Badge() {}

    public Badge(String label, String description, Sport sport) {}
}
