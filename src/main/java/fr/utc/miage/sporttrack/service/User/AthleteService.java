package fr.utc.miage.sporttrack.service.user;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import org.springframework.stereotype.Service;

@Service
public class AthleteService {

    private final AthleteRepository athleteRepository;

    public AthleteService(AthleteRepository athleteRepository) {
        this.athleteRepository = athleteRepository;
    }

    public void createProfile(Athlete athlete) {
        if (athleteRepository.existsByEmail(athlete.getEmail())) {
            throw new IllegalArgumentException("Email is already used");
        }
        athleteRepository.save(athlete);
    }

}
