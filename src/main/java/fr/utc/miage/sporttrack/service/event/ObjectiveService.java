package fr.utc.miage.sporttrack.service.event;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.service.user.communication.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.utc.miage.sporttrack.repository.event.ObjectiveRepository;

import java.time.LocalDateTime;

@Service
public class ObjectiveService {

    private final ObjectiveRepository objectiveRepository;
    private final NotificationService notificationService;

    public ObjectiveService(ObjectiveRepository objectiveRepository) {
        this(objectiveRepository, null);
    }

    @Autowired
    public ObjectiveService(ObjectiveRepository objectiveRepository, NotificationService notificationService) {
        this.objectiveRepository = objectiveRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void saveObjective(Objective objective, Athlete athlete, Sport sport) {
        if (objective != null && athlete != null && sport != null) {
            objective.setAthlete(athlete);
            objective.setSport(sport);
            objective.setCompleted(false);
            objective.setCompletedAt(null);
            objectiveRepository.save(objective);
        }
    }

    @Transactional
    public Objective markAsCompleted(int id, Athlete athlete) {
        if (athlete == null || athlete.getId() == null) {
            throw new IllegalArgumentException("Athlete is required");
        }

        Objective objective = objectiveRepository.findByIdAndAthlete_Id(id, athlete.getId())
                .orElseThrow(() -> new IllegalArgumentException("Objective not found for current athlete"));

        if (!objective.isCompleted()) {
            objective.setCompleted(true);
            objective.setCompletedAt(LocalDateTime.now());
            objectiveRepository.save(objective);
            if (notificationService != null) {
                notificationService.notifyObjectiveCompleted(athlete, objective);
            }
        }

        return objective;
    }

    public Iterable<Objective> getObjectivesByUser(Athlete athlete) {
        return objectiveRepository.findByAthlete(athlete);
    }

    public void deleteById(int id) {
        objectiveRepository.deleteById(id);
    }
}
