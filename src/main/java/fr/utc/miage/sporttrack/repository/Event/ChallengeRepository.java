package fr.utc.miage.sporttrack.repository.event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.utc.miage.sporttrack.entity.event.Challenge;

public interface ChallengeRepository extends JpaRepository<Challenge, Integer> {
    List<Challenge> findDistinctByOrganizer_IdUOrParticipants_IdU(int organizerId, int participantId);

}
