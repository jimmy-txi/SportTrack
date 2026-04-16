package fr.utc.miage.sporttrack.service.user;

import fr.utc.miage.sporttrack.dto.AthleteProfileUpdateDTO;
import fr.utc.miage.sporttrack.dto.AthleteRegisterFormDTO;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AthleteService {

    private final AthleteRepository athleteRepository;
    private final PasswordEncoder passwordEncoder;

    public AthleteService(AthleteRepository athleteRepository, PasswordEncoder passwordEncoder) {
        this.athleteRepository = athleteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new athlete profile from a registration form DTO.
     * Maps the DTO fields to a new Athlete entity before persisting.
     *
     * @param dto the registration form data
     * @throws IllegalArgumentException if the email is already in use
     */
    public void createProfile(AthleteRegisterFormDTO dto) {
        if (athleteRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email is already used");
        }
        Athlete athlete = new Athlete();
        athlete.setEmail(dto.getEmail());
        athlete.setUsername(dto.getUsername());
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        athlete.setPassword(encodedPassword);
        athleteRepository.save(athlete);
    }
    // Get current athlete profile
    public Athlete getCurrentAthlete(String email) {
        return athleteRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found"));
    }

    // Update athlete profile
    public void updateProfile(String email, AthleteProfileUpdateDTO updatedData) {
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

    public List<Athlete> getAllAthletes() {
        return athleteRepository.findAll();
    }

    public List<Athlete> searchAthletesByName(String query) {
        return athleteRepository.findByUsernameContainingIgnoreCase(query);
    }



}
