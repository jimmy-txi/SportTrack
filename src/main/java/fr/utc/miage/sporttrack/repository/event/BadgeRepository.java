package fr.utc.miage.sporttrack.repository.event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.utc.miage.sporttrack.entity.event.Badge;

/**
 * Spring Data JPA repository for {@link Badge} entities.
 *
 * <p>Provides standard CRUD operations as well as custom query methods
 * for retrieving badges by sport, by earning athlete, or excluding specific identifiers.</p>
 *
 * @author SportTrack Team
 */
@Repository
public interface BadgeRepository extends JpaRepository<Badge, Integer> {

    /**
     * Finds all badges associated with the specified sport.
     *
     * @param sportId the unique identifier of the sport
     * @return a list of badges linked to the given sport
     */
    List<Badge> findBySportId(Integer sportId);

    /**
     * Finds all badges with no sport assigned (universal badges that apply to all sports).
     *
     * @return a list of badges with sport field null
     */
    List<Badge> findBySportIsNull();

    /**
     * Finds all badges earned by the specified athlete.
     *
     * @param athleteId the unique identifier of the athlete
     * @return a list of badges earned by the athlete
     */
    List<Badge> findByEarnedBy_Id(Integer athleteId);

    /**
     * Finds all badges whose identifiers are not in the provided list.
     *
     * @param ids the list of badge identifiers to exclude
     * @return a list of badges not matching any of the given identifiers
     */
    List<Badge> findByIdNotIn(List<Integer> ids);
}