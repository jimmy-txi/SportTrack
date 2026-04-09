package fr.utc.miage.sporttrack.service.User;
import java.util.List;

import fr.utc.miage.sporttrack.entity.User.Athlete;
import fr.utc.miage.sporttrack.repository.User.AthleteRepository;

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
    
    public List<Athlete> getAllAthletes() {
        return athleteRepository.findAll();
    }

    public List<Athlete> searchAthletesByName(String query) {
        return athleteRepository.findByUsernameContainingIgnoreCase(query);
    }
 


}
