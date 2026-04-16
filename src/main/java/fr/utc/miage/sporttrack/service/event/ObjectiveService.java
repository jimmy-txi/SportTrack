package fr.utc.miage.sporttrack.service.event;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.service.user.communication.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.utc.miage.sporttrack.repository.event.ObjectiveRepository;

import java.time.LocalDateTime;

/**
 * Service layer component responsible for managing {@link Objective} entities
 * within the SportTrack application.
 *
 * <p>Provides business logic for creating, completing, querying, and deleting
 * objectives. When an objective is completed, a notification is sent to the athlete.</p>
 *
 * @author SportTrack Team
 */
@Service
public class ObjectiveService {

    /** The repository for objective data access. */
    private final ObjectiveRepository objectiveRepository;

    /** The notification service for objective-completed events (optional). */
    private final NotificationService notificationService;

    /**
     * Constructs an {@code ObjectiveService} without notification support.
     *
     * @param objectiveRepository the objective repository
     */
    public ObjectiveService(ObjectiveRepository objectiveRepository) {
        this(objectiveRepository, null);
    }

    /**
     * Constructs an {@code ObjectiveService} with full notification support.
     *
     * @param objectiveRepository  the objective repository
     * @param notificationService  the notification service for completion events
     */
    @Autowired
    public ObjectiveService(ObjectiveRepository objectiveRepository, NotificationService notificationService) {
        this.objectiveRepository = objectiveRepository;
        this.notificationService = notificationService;
    }

    /**
     * Saves a new objective associated with the given athlete and sport.
     * The objective is initialised as not completed.
     *
     * @param objective the objective entity to save
     * @param athlete   the athlete who owns the objective
     * @param sport     the sport to which the objective applies
     */
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

    /**
     * Marks the specified objective as completed for the given athlete.
     * A notification is sent if the notification service is available.
     *
     * @param id      the unique identifier of the objective
     * @param athlete the athlete who owns the objective
     * @return the updated {@link Objective}
     * @throws IllegalArgumentException if the athlete is null or the objective is not found
     */
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

    /**
     * Returns all objectives belonging to the specified athlete.
     *
     * @param athlete the athlete whose objectives should be retrieved
     * @return an iterable of objectives owned by the athlete
     */
    public Iterable<Objective> getObjectivesByUser(Athlete athlete) {
        return objectiveRepository.findByAthlete(athlete);
    }

    /**
     * Deletes the objective with the given identifier.
     *
     * @param id the unique identifier of the objective to delete
     */
    public void deleteById(int id) {
        objectiveRepository.deleteById(id);
    }

    /**
     * Determines whether an objective is considered completed based on the
     * provided list of activities. Completion is evaluated by matching the sport
     * and checking whether the relevant metric (distance or repetition) is positive.
     *
     * @param objective  the objective to evaluate
     * @param activities the list of activities to check against the objective
     * @return {@code true} if at least one activity satisfies the objective criteria
     */
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
