package fr.utc.miage.sporttrack.repository.User;

import fr.utc.miage.sporttrack.entity.User.Athlete;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
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

    public Optional<Athlete> findByEmail(String email) {
        return athletes.stream()
                .filter(a -> a.getEmail() != null && a.getEmail().equals(email))
                .findFirst();
    }

}
