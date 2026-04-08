package fr.utc.miage.sporttrack.repository.Event;

import fr.utc.miage.sporttrack.entity.Event.Objective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObjectiveRepository extends JpaRepository<Objective, Integer> {
}
