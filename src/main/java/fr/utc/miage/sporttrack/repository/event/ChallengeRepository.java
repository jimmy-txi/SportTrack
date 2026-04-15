package fr.utc.miage.sporttrack.repository.event;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.utc.miage.sporttrack.entity.event.Challenge;

public interface ChallengeRepository extends JpaRepository<Challenge, Integer> {
    List<Challenge> findDistinctByOrganizer_IdOrParticipants_Id(int organizerId, int participantId);

    @Query("""
        SELECT DISTINCT c
        FROM Challenge c
        JOIN c.participants p
        WHERE p.id = :athleteId
          AND c.sport.id = :sportId
          AND :activityDate BETWEEN c.startDate AND c.endDate
        """)
    List<Challenge> findChallengesImpactedByActivity(
            @Param("athleteId") Integer athleteId,
            @Param("sportId") Integer sportId,
            @Param("activityDate") LocalDate activityDate
    );

    List<Challenge> findAll();
    Optional<Challenge> findById(int id);
}