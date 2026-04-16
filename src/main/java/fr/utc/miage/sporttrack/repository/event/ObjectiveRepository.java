package fr.utc.miage.sporttrack.repository.event;

import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ObjectiveRepository extends JpaRepository<Objective, Integer> {
    Iterable<Objective> findByAthlete(Athlete athlete);

    Optional<Objective> findByIdAndAthlete_Id(int id, Integer athleteId);
}
