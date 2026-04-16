package fr.utc.miage.sporttrack.service.event;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;

import java.util.List;

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

    public  boolean isObjectiveCompleted(Objective objective, List<Activity> activities) {
        if (objective == null || objective.getSport() == null || activities == null || activities.isEmpty()) {
            return false;
        }
        return activities.stream().anyMatch(activity -> {
            if (activity == null || activity.getSportAndType() == null) {
                return false;
            }
            Sport objectiveSport = objective.getSport();
            if (objectiveSport.getId() != activity.getSportAndType().getId()) {
                return false;
            }
            if (objectiveSport.getType() == null) {
                return true;
            }
            switch (objectiveSport.getType()) {
                case DISTANCE:
                    return activity.getDistance() != null && activity.getDistance() > 0;
                case REPETITION:
                    return activity.getRepetition() != null && activity.getRepetition() > 0;
                default:
                    return true;
            }
        });
    }
}
