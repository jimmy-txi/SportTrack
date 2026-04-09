package fr.utc.miage.sporttrack.repository.event;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;

public interface ObjectiveRepository extends JpaRepository<Objective, Integer> {
    public Iterable<Objective> findByUser(Athlete athlete);
}
