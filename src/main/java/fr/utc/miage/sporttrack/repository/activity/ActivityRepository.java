package fr.utc.miage.sporttrack.repository.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.utc.miage.sporttrack.entity.activity.Activity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Integer> {

	List<Activity> findByCreatedBy_IdOrderByDateADescStartTimeDesc(Integer athleteId);

	List<Activity> findByCreatedBy_IdInOrderByDateADescStartTimeDesc(List<Integer> athleteIds);

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

	Optional<Activity> findByIdAndCreatedBy_Id(Integer id, Integer athleteId);

	boolean existsByIdAndCreatedBy_Id(Integer id, Integer athleteId);
}
