package fr.utc.miage.sporttrack.service.Event;

import fr.utc.miage.sporttrack.entity.Event.Challenge;
import fr.utc.miage.sporttrack.repository.Event.ChallengeRepository;
import org.springframework.stereotype.Service;

@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    public ChallengeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }
}
