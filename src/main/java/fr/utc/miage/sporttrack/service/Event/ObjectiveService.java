package fr.utc.miage.sporttrack.service.event;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;

import org.springframework.stereotype.Service;

import fr.utc.miage.sporttrack.repository.event.ObjectiveRepository;

@Service
public class ObjectiveService {

    private final ObjectiveRepository objectiveRepository;

    public ObjectiveService(ObjectiveRepository objectiveRepository) {
        this.objectiveRepository = objectiveRepository;
    }

    public void saveObjective(Objective objective, Athlete athlete, Sport sport) {
        if (objective != null && athlete != null && sport != null) {
            objective.setAthlete(athlete);
            objective.setSport(sport);
            objectiveRepository.save(objective);
        }
    }

    public Iterable<Objective> getObjectivesByUser(Athlete athlete) {
        return objectiveRepository.findByAthlete(athlete);
    }

    public void deleteById(int id) {
        objectiveRepository.deleteById(id);
    }
}
