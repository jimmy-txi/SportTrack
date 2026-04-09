package fr.utc.miage.sporttrack.service.User;
import java.util.List;

import org.springframework.stereotype.Service;

import fr.utc.miage.sporttrack.entity.User.Athlete;
import fr.utc.miage.sporttrack.repository.User.AthleteRepository;

@Service
public class AthleteService {

    private AthleteRepository athleteRepository;

    public List<Athlete> getAllAthletes() {
        return athleteRepository.findAll();
    }
}
