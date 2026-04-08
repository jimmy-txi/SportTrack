package fr.utc.miage.sporttrack.repository.Event;

import fr.utc.miage.sporttrack.entity.Event.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Integer> {
}
