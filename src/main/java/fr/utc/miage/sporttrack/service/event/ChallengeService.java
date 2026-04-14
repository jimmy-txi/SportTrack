package fr.utc.miage.sporttrack.service.event;

import org.springframework.stereotype.Service;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.event.Challenge;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.event.ChallengeRepository;

@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    public ChallengeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    public void saveChallenge(Challenge challenge, Athlete athlete, Sport sport) {
        if (challenge != null && athlete != null && sport != null) {
            challenge.setOrganizer(athlete);
            challenge.setSport(sport);
            challengeRepository.save(challenge);
        }
    }
}
