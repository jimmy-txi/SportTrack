package fr.utc.miage.sporttrack.service.activity;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import fr.utc.miage.sporttrack.repository.activity.SportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SportService {

    private final SportRepository repository;

    public SportService(SportRepository repository) {
        this.repository = repository;
    }

    public List<Sport> findAll() {
        return repository.findAll();
    }

    public Optional<Sport> findById(int id) {
        return repository.findById(id);
    }

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

    public void deleteSport(int id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Sport not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private void checkSportName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Sport name cannot be null or empty");
        }
    }

    private void checkCaloriesPerHour(double caloriesPerHour) {
        if (caloriesPerHour <= 0) {
            throw new IllegalArgumentException("Calories per hour must be greater than zero");
        }
    }
}
