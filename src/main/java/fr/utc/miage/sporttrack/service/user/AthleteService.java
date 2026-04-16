package fr.utc.miage.sporttrack.service.user;

import fr.utc.miage.sporttrack.dto.AthleteProfileUpdateDTO;
import fr.utc.miage.sporttrack.dto.AthleteRegisterFormDTO;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer component responsible for managing {@link Athlete} entities
 * within the SportTrack application.
 *
 * <p>Provides business logic for athlete registration, profile retrieval,
 * profile updates, and athlete search functionality. Password hashing is
 * performed during registration using the injected {@link PasswordEncoder}.</p>
 *
 * @author SportTrack Team
 */
@Service
public class AthleteService {

    /** The repository used for persisting and retrieving athlete entities. */
    private final AthleteRepository athleteRepository;

    /** The encoder used to hash passwords before persistence. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a new {@code AthleteService} with the required dependencies.
     *
     * @param athleteRepository the repository for athlete data access
     * @param passwordEncoder   the encoder for hashing passwords
     */
    public AthleteService(AthleteRepository athleteRepository, PasswordEncoder passwordEncoder) {
        this.athleteRepository = athleteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new athlete profile from a registration form DTO.
     * Maps the DTO fields to a new {@link Athlete} entity before persisting.
     *
     * @param dto the registration form data containing email, username, and password
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

    /**
     * Retrieves the current athlete's profile by their email address.
     *
     * @param email the email address used as login identifier
     * @return the matching {@link Athlete}
     * @throws IllegalArgumentException if no athlete is found with the given email
     */
    public Athlete getCurrentAthlete(String email) {
        return athleteRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found"));
    }

    /**
     * Updates an athlete's profile with the data provided in the DTO.
     * Only the fields present in {@link AthleteProfileUpdateDTO} are modified.
     *
     * @param email       the email address identifying the athlete to update
     * @param updatedData the DTO containing the new profile field values
     */
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

    /**
     * Returns all registered athletes.
     *
     * @return a list of all athletes in the database
     */
    public List<Athlete> getAllAthletes() {
        return athleteRepository.findAll();
    }

    /**
     * Searches for athletes whose username contains the given query string, ignoring case.
     *
     * @param query the search keyword to match against usernames
     * @return a list of athletes whose usernames contain the query string
     */
    public List<Athlete> searchAthletesByName(String query) {
        return athleteRepository.findByUsernameContainingIgnoreCase(query);
    }



}