package fr.utc.miage.sporttrack.repository.event;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.utc.miage.sporttrack.entity.event.Challenge;

/**
 * Spring Data JPA repository for {@link Challenge} entities.
 *
 * <p>Provides standard CRUD operations as well as custom query methods
 * for retrieving challenges by organiser, participant, date range, and sport.</p>
 *
 * @author SportTrack Team
 */
@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Integer> {

    /**
     * Finds all distinct challenges in which the given athlete is either the organiser
     * or a participant.
     *
     * @param organizerId   the identifier of the potential organiser
     * @param participantId the identifier of the potential participant
     * @return a list of matching challenges
     */
    List<Challenge> findDistinctByOrganizer_IdOrParticipants_Id(int organizerId, int participantId);

    /**
     * Finds all challenges whose end date is on or before the given date and for which
     * no end notification has yet been sent.
     *
     * @param currentDate the date to compare against challenge end dates
     * @return a list of ended but unnotified challenges
     */
    List<Challenge> findByEndDateLessThanEqualAndEndedNotifiedAtIsNull(LocalDate currentDate);

    /**
     * Finds all active challenges that are impacted by a newly created activity.
     * A challenge is impacted if the athlete is a participant, the sport matches,
     * and the activity date falls within the challenge date range.
     *
     * @param athleteId    the identifier of the athlete who created the activity
     * @param sportId      the identifier of the sport associated with the activity
     * @param activityDate the date of the activity
     * @return a list of challenges affected by the new activity
     */
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

    /**
     * Returns all challenges in the database.
     *
     * @return a list of all challenges
     */
    List<Challenge> findAll();

    /**
     * Finds a challenge by its identifier.
     *
     * @param id the unique identifier of the challenge
     * @return an {@link Optional} containing the challenge if found, empty otherwise
     */
    Optional<Challenge> findById(int id);
}