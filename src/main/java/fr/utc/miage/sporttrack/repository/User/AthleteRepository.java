package fr.utc.miage.sporttrack.repository.User;

import fr.utc.miage.sporttrack.entity.User.Athlete;

import java.util.ArrayList;
import java.util.List;

public class AthleteRepository {

    private final List<Athlete> athletes = new ArrayList<>();

    public void save(Athlete athlete) {
        athletes.add(athlete);
    }

    public boolean existsByEmail(String email) {
        return athletes.stream()
                .anyMatch(a -> a.getEmail() != null && a.getEmail().equals(email));
    }

    public List<Athlete> findAll() {
        return athletes;
    }

}
