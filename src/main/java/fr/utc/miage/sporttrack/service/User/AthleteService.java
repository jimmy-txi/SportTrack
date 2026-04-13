package fr.utc.miage.sporttrack.service.User;
import java.util.List;

import fr.utc.miage.sporttrack.entity.User.Athlete;
import fr.utc.miage.sporttrack.repository.User.AthleteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AthleteService {

    private final AthleteRepository athleteRepository;
    private final PasswordEncoder passwordEncoder;

    public AthleteService(AthleteRepository athleteRepository, PasswordEncoder passwordEncoder) {
        this.athleteRepository = athleteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void createProfile(Athlete athlete) {
        if (athleteRepository.existsByEmail(athlete.getEmail())) {
            throw new IllegalArgumentException("Email is already used");
        }
        String encodedPassword = passwordEncoder.encode(athlete.getPassword());
        athlete.setPassword(encodedPassword);
        athleteRepository.save(athlete);
    }
    
    public List<Athlete> getAllAthletes() {
        return athleteRepository.findAll();
    }

    public List<Athlete> searchAthletesByName(String query) {
        return athleteRepository.findByUsernameContainingIgnoreCase(query);
    }
 


}
