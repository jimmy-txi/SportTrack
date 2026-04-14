package fr.utc.miage.sporttrack.repository.event;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.utc.miage.sporttrack.entity.event.Challenge;

public interface ChallengeRepository extends JpaRepository<Challenge, Integer> {
    List<Challenge> findDistinctByOrganizer_IdOrParticipants_Id(int organizerId, int participantId);
    List<Challenge> findAll();
    Optional<Challenge> findById(int id);
}
