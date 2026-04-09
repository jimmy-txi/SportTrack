package fr.utc.miage.sporttrack.service.user;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
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

}
