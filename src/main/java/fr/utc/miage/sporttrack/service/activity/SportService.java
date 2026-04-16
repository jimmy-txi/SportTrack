package fr.utc.miage.sporttrack.service.activity;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import fr.utc.miage.sporttrack.repository.activity.SportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer component responsible for managing {@link Sport} entities
 * within the SportTrack application.
 *
 * <p>Provides business logic for creating, updating, enabling, disabling,
 * and querying sports. Input validation is performed before persistence.</p>
 *
 * @author SportTrack Team
 */
@Service
public class SportService {

    /** The repository used for persisting and retrieving sport entities. */
    private final SportRepository repository;

    /**
     * Constructs a new {@code SportService} with the given repository.
     *
     * @param repository the sport repository for data access
     */
    public SportService(SportRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns all sports, both active and inactive.
     *
     * @return a list of all sports
     */
    public List<Sport> findAll() {
        return repository.findAll();
    }

    /**
     * Returns only active sports.
     *
     * @return a list of sports where the active flag is {@code true}
     */
    public List<Sport> findAllActive() {
        return repository.findAllByActive(true);
    }

    /**
     * Finds a sport by its unique identifier.
     *
     * @param id the sport identifier
     * @return an {@link Optional} containing the sport if found, empty otherwise
     */
    public Optional<Sport> findById(int id) {
        return repository.findById(id);
    }

    /**
     * Creates and persists a new sport with the provided attributes.
     *
     * @param name           the display name of the sport
     * @param description    a textual description of the sport
     * @param caloriesPerHour the average calories burned per hour
     * @param type           the {@link SportType} defining the measurement method
     * @return the newly created and persisted {@link Sport}
     * @throws IllegalArgumentException if the name is empty or calories per hour is not positive
     */
    public Sport createSport(String name, String description, double caloriesPerHour, SportType type) {
        checkSportName(name);
        checkCaloriesPerHour(caloriesPerHour);

        Sport sport = new Sport();

        sport.setName(name);
        sport.setDescription(description);
        sport.setCaloriesPerHour(caloriesPerHour);
        sport.setType(type);

        return repository.save(sport);
    }

    /**
     * Updates an existing sport with the provided attributes.
     *
     * @param id             the identifier of the sport to update
     * @param name           the new display name
     * @param description    the new textual description
     * @param caloriesPerHour the new average calories burned per hour
     * @param type           the new {@link SportType}
     * @return the updated and persisted {@link Sport}
     * @throws IllegalArgumentException if the sport is not found, the name is empty, or calories per hour is not positive
     */
    public Sport updateSport(int id, String name, String description, double caloriesPerHour, SportType type) {
        checkSportName(name);
        checkCaloriesPerHour(caloriesPerHour);

        Sport sport = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sport not found with id: " + id));

        sport.setName(name);
        sport.setDescription(description);
        sport.setCaloriesPerHour(caloriesPerHour);
        sport.setType(type);

        return repository.save(sport);
    }

    /**
     * Disables a sport by setting its active flag to {@code false}.
     *
     * @param id the identifier of the sport to disable
     * @throws IllegalArgumentException if no sport is found with the given identifier
     */
    public void disableSport(int id) {
        Sport sport = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Sport not found with id: " + id));

        sport.setActive(false);
        repository.save(sport);
    }

    /**
     * Enables a sport by setting its active flag to {@code true}.
     *
     * @param id the identifier of the sport to enable
     * @throws IllegalArgumentException if no sport is found with the given identifier
     */
    public void enableSport(int id) {
        Sport sport = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Sport not found with id: " + id));

        sport.setActive(true);
        repository.save(sport);
    }

    /**
     * Validates that the sport name is not null or empty.
     *
     * @param name the sport name to validate
     * @throws IllegalArgumentException if the name is null or empty
     */
    private void checkSportName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Sport name cannot be null or empty");
        }
    }

    /**
     * Validates that the calories per hour value is positive.
     *
     * @param caloriesPerHour the calorie value to validate
     * @throws IllegalArgumentException if the value is zero or negative
     */
    private void checkCaloriesPerHour(double caloriesPerHour) {
        if (caloriesPerHour <= 0) {
            throw new IllegalArgumentException("Calories per hour must be greater than zero");
        }
    }

    /**
     * Returns a safe display name for the given sport, defaulting to "Autre"
     * if the sport or its name is null or blank.
     *
     * @param sport the sport whose name should be resolved
     * @return the sport name, or "Autre" as a fallback
     */
    public String safeSportName(Sport sport) {
        if (sport == null) {
            return "Autre";
        }
        String name = sport.getName();
        return name != null && !name.isBlank() ? name : "Autre";
    }
}