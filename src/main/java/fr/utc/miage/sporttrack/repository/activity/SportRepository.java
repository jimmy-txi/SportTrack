package fr.utc.miage.sporttrack.repository.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.utc.miage.sporttrack.entity.activity.Sport;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Sport} entities.
 *
 * <p>Provides standard CRUD operations as well as custom query methods
 * for retrieving sports by their active status.</p>
 *
 * @author SportTrack Team
 */
@Repository
public interface SportRepository extends JpaRepository<Sport, Integer> {

    /**
     * Finds all sports matching the given active status.
     *
     * @param active {@code true} to retrieve only active sports, {@code false} for inactive
     * @return a list of sports matching the specified active status
     */
    List<Sport> findAllByActive(boolean active);
}