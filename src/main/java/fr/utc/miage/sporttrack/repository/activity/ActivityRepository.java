package fr.utc.miage.sporttrack.repository.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.utc.miage.sporttrack.entity.activity.Activity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Integer> {

	List<Activity> findByCreatedBy_IdOrderByDateADescStartTimeDesc(Integer athleteId);

	List<Activity> findByCreatedBy_IdInOrderByDateADescStartTimeDesc(List<Integer> athleteIds);

	Optional<Activity> findByIdAndCreatedBy_Id(Integer id, Integer athleteId);

	boolean existsByIdAndCreatedBy_Id(Integer id, Integer athleteId);
}
