package fr.utc.miage.sporttrack.service.User;

import fr.utc.miage.sporttrack.entity.User.Athlete;
import fr.utc.miage.sporttrack.repository.User.AthleteRepository;
import org.springframework.stereotype.Service;

@Service
public class AthleteService {

    private final AthleteRepository athleteRepository;

    public AthleteService(AthleteRepository athleteRepository) {
        this.athleteRepository = athleteRepository;
    }
}
