package fr.utc.miage.sporttrack.service.event;

import org.springframework.stereotype.Service;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.event.Challenge;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.event.ChallengeRepository;

/**
 * Service layer component responsible for managing {@link Challenge} entities
 * within the SportTrack application.
 *
 * <p>Provides business logic for saving and retrieving challenges.</p>
 *
 * @author SportTrack Team
 */
@Service
public class ChallengeService {

    /** The repository used for persisting and retrieving challenge entities. */
    private final ChallengeRepository challengeRepository;

    /**
     * Constructs a new {@code ChallengeService} with the given repository.
     *
     * @param challengeRepository the repository for challenge data access
     */
    public ChallengeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    /**
     * Saves a challenge entity, associating it with the given organiser athlete and sport.
     * Does nothing if any of the parameters is {@code null}.
     *
     * @param challenge the challenge entity to save
     * @param athlete   the athlete who organises the challenge
     * @param sport     the sport to which the challenge applies
     */
    public void saveChallenge(Challenge challenge, Athlete athlete, Sport sport) {
        if (challenge != null && athlete != null && sport != null) {
            challenge.setOrganizer(athlete);
            challenge.setSport(sport);
            challengeRepository.save(challenge);
        }
    }

    /**
     * Retrieves a challenge by its unique identifier.
     *
     * @param id the challenge identifier
     * @return the matching {@link Challenge}, or {@code null} if not found
     */
    public Challenge getChallengeById(int id) {
        return challengeRepository.findById(id).orElse(null);
    }
}