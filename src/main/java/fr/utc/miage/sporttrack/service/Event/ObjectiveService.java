package fr.utc.miage.sporttrack.service.event;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.utc.miage.sporttrack.repository.event.ObjectiveRepository;

@Service
public class ObjectiveService {

    @Autowired
    private ObjectiveRepository objectiveRepository;

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
