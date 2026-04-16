package fr.utc.miage.sporttrack.repository.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.utc.miage.sporttrack.entity.activity.Activity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Activity} entities.
 *
 * <p>Provides standard CRUD operations as well as custom query methods
 * for retrieving activities by creator, sport, and date range.</p>
 *
 * @author SportTrack Team
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, Integer> {

    /**
     * Finds all activities created by a specific athlete, ordered by date and start time descending.
     *
     * @param athleteId the unique identifier of the athlete who created the activities
     * @return a list of matching activities, newest first
     */
	List<Activity> findByCreatedBy_IdOrderByDateADescStartTimeDesc(Integer athleteId);

    /**
     * Finds all activities created by any of the specified athletes, ordered by date and start time descending.
     *
     * @param athleteIds the list of athlete identifiers whose activities should be retrieved
     * @return a list of matching activities, newest first
     */
	List<Activity> findByCreatedBy_IdInOrderByDateADescStartTimeDesc(List<Integer> athleteIds);

    /**
     * Finds activities matching the criteria for challenge ranking computation.
     * Returns activities performed by the given athletes, for the specified sport,
     * within the given date range.
     *
     * @param athleteIds the identifiers of the athletes whose activities should be considered
     * @param sportId    the identifier of the sport to filter by
     * @param startDate  the inclusive start date of the range
     * @param endDate    the inclusive end date of the range
     * @return a list of matching activities
     */
	@Query("""
		SELECT a
		FROM Activity a
		WHERE a.createdBy.id IN :athleteIds
		  AND a.sportAndType.id = :sportId
		  AND a.dateA BETWEEN :startDate AND :endDate
		""")
	List<Activity> findActivitiesForChallengeRanking(
			@Param("athleteIds") List<Integer> athleteIds,
			@Param("sportId") Integer sportId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate
	);

    /**
     * Finds a single activity by its identifier and the identifier of the athlete who created it.
     *
     * @param id        the activity identifier
     * @param athleteId the creator's identifier
     * @return an {@link Optional} containing the activity if found, empty otherwise
     */
	Optional<Activity> findByIdAndCreatedBy_Id(Integer id, Integer athleteId);

    /**
     * Checks whether an activity with the given identifier was created by the specified athlete.
     *
     * @param id        the activity identifier
     * @param athleteId the creator's identifier
     * @return {@code true} if such an activity exists, {@code false} otherwise
     */
	boolean existsByIdAndCreatedBy_Id(Integer id, Integer athleteId);
}