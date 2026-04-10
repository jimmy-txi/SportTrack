package fr.utc.miage.sporttrack.service.User;

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
    // Get current athlete profile
    public Athlete getCurrentAthlete(String email) {
        return athleteRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found"));
    }

    // Update athlete profile
    public void updateProfile(String email, Athlete updatedData) {
        Athlete existingAthlete = getCurrentAthlete(email);

        // Update allowed fields
        existingAthlete.setUsername(updatedData.getUsername());
        existingAthlete.setFirstName(updatedData.getFirstName());
        existingAthlete.setLastName(updatedData.getLastName());
        existingAthlete.setGender(updatedData.getGender());
        existingAthlete.setAge(updatedData.getAge());
        existingAthlete.setHeight(updatedData.getHeight());
        existingAthlete.setWeight(updatedData.getWeight());
        existingAthlete.setPracticeLevel(updatedData.getPracticeLevel());
        existingAthlete.setBio(updatedData.getBio());

        athleteRepository.save(existingAthlete);
    }

}
