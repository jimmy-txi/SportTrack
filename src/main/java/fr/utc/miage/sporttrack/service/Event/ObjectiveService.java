package fr.utc.miage.sporttrack.service.event;

import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.utc.miage.sporttrack.repository.event.ObjectiveRepository;

@Service
public class ObjectiveService {

    @Autowired
    private ObjectiveRepository objectiveRepository;

    public void saveObjective(Objective objective) {
        if (objective != null) {
            objectiveRepository.save(objective);
        }
    }

    public Iterable<Objective> getObjectivesByUser(Athlete athlete) {
        return objectiveRepository.findByUser(athlete);
    }
}
