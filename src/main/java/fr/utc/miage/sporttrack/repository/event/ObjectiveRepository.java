package fr.utc.miage.sporttrack.repository.event;

import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Objective} entities.
 *
 * <p>Provides standard CRUD operations as well as custom query methods
 * for retrieving objectives by athlete.</p>
 *
 * @author SportTrack Team
 */
@Repository
public interface ObjectiveRepository extends JpaRepository<Objective, Integer> {

    /**
     * Finds all objectives belonging to the specified athlete.
     *
     * @param athlete the athlete whose objectives should be retrieved
     * @return an iterable of objectives owned by the athlete
     */
    Iterable<Objective> findByAthlete(Athlete athlete);

    /**
     * Finds an objective by its identifier and the identifier of the owning athlete.
     *
     * @param id        the unique identifier of the objective
     * @param athleteId the identifier of the athlete who owns the objective
     * @return an {@link Optional} containing the objective if found, empty otherwise
     */
    Optional<Objective> findByIdAndAthlete_Id(int id, Integer athleteId);
}