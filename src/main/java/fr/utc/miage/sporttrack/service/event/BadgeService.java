package fr.utc.miage.sporttrack.service.event;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.entity.event.Badge;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.activity.ActivityRepository;
import fr.utc.miage.sporttrack.repository.event.BadgeRepository;
import fr.utc.miage.sporttrack.service.user.communication.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service layer component responsible for managing {@link Badge} entities
 * within the SportTrack application.
 *
 * <p>Provides business logic for badge CRUD operations, querying earned and
 * unearned badges per athlete, and automatically awarding badges based on
 * cumulative activity metrics.</p>
 *
 * @author SportTrack Team
 */
@Service
public class BadgeService {

    /** The repository for badge data access. */
    private final BadgeRepository badgeRepository;

    /** The repository for activity data access, used in badge award computation. */
    private final ActivityRepository activityRepository;

    /** The notification service for sending badge-earned notifications (optional). */
    private final NotificationService notificationService;

    /**
     * Constructs a {@code BadgeService} without notification support.
     *
     * @param badgeRepository      the badge repository
     * @param activityRepository   the activity repository
     */
    public BadgeService(BadgeRepository badgeRepository,
                        ActivityRepository activityRepository) {
        this(badgeRepository, activityRepository, null);
    }

    /**
     * Constructs a {@code BadgeService} with full notification support.
     *
     * @param badgeRepository      the badge repository
     * @param activityRepository   the activity repository
     * @param notificationService  the notification service for badge-earned events
     */
    @Autowired
    public BadgeService(BadgeRepository badgeRepository,
                        ActivityRepository activityRepository,
                        NotificationService notificationService) {
        this.badgeRepository = badgeRepository;
        this.activityRepository = activityRepository;
        this.notificationService = notificationService;
    }

    // ========== Admin CRUD ==========

    /**
     * Saves a badge entity after associating it with the given sport.
     * Initialises the earned-by list if it is null.
     *
     * @param badge the badge entity to save
     * @param sport the sport to associate with the badge, or {@code null} for universal badges
     * @throws IllegalArgumentException if the badge is null
     */
    public void saveBadge(Badge badge, Sport sport) {
        if (badge == null) {
            throw new IllegalArgumentException("Badge is required");
        }
        badge.setSport(sport); // Sport can be null for universal badges
        if (badge.getEarnedBy() == null) {
            badge.setEarnedBy(new ArrayList<>());
        }
        badgeRepository.save(badge);
    }

    /**
     * Returns all badges in the database.
     *
     * @return a list of all badges
     */
    public List<Badge> findAll() {
        return badgeRepository.findAll();
    }

    /**
     * Finds a badge by its unique identifier.
     *
     * @param id the badge identifier
     * @return the matching {@link Badge}
     * @throws IllegalArgumentException if no badge is found
     */
    public Badge findById(int id) {
        return badgeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found with id: " + id));
    }

    /**
     * Deletes the badge with the given identifier.
     *
     * @param id the badge identifier to delete
     * @throws IllegalArgumentException if no badge is found with the given identifier
     */
    public void deleteById(int id) {
        if (!badgeRepository.existsById(id)) {
            throw new IllegalArgumentException("Badge not found with id: " + id);
        }
        badgeRepository.deleteById(id);
    }

    // ========== Athlete Badge Queries ==========

    /**
     * Returns all badges earned by the specified athlete.
     *
     * @param athleteId the unique identifier of the athlete
     * @return a list of badges earned by the athlete
     */
    public List<Badge> getEarnedBadges(Integer athleteId) {
        return badgeRepository.findByEarnedBy_Id(athleteId);
    }

    /**
     * Returns all badges not yet earned by the specified athlete.
     *
     * @param athleteId the unique identifier of the athlete
     * @return a list of badges the athlete has not yet earned
     */
    public List<Badge> getUnearnedBadges(Integer athleteId) {
        List<Badge> earned = getEarnedBadges(athleteId);
        List<Integer> earnedIds = earned.stream().map(Badge::getId).toList();
        if (earnedIds.isEmpty()) {
            return badgeRepository.findAll();
        }
        return badgeRepository.findByIdNotIn(earnedIds);
    }

    /**
     * Returns the N most recently earned badges for the specified athlete.
     * Since earned-at timestamps are not tracked, the last N from the list are returned.
     *
     * @param athleteId the unique identifier of the athlete
     * @param limit     the maximum number of badges to return
     * @return a list of up to {@code limit} recent badges
     */
    public List<Badge> getRecentBadges(Integer athleteId, int limit) {
        List<Badge> earned = getEarnedBadges(athleteId);
        if (earned.size() <= limit) {
            return earned;
        }
        return earned.subList(earned.size() - limit, earned.size());
    }

    // ========== Auto-award Logic ==========

    /**
     * Checks and awards badges after an activity is created.
     * Evaluates both sport-specific badges and universal badges (with no sport filter).
     *
     * @param activity the newly created activity that may trigger badge awards
     */
    public void checkAndAwardBadges(Activity activity) {
        if (activity == null || activity.getCreatedBy() == null || activity.getSportAndType() == null) {
            return;
        }

        Athlete athlete = activity.getCreatedBy();
        Sport sport = activity.getSportAndType();

        // Find all badges: both sport-specific and universal (no sport assigned)
        List<Badge> sportBadges = badgeRepository.findBySportId(sport.getId());
        List<Badge> universalBadges = badgeRepository.findBySportIsNull();

        // Combine the lists
        List<Badge> allRelevantBadges = new ArrayList<>(sportBadges);
        allRelevantBadges.addAll(universalBadges);

        if (allRelevantBadges.isEmpty()) {
            return;
        }

        // Get all activities for this athlete in this sport
        List<Activity> athleteSportActivities = activityRepository
                .findByCreatedBy_IdOrderByDateADescStartTimeDesc(athlete.getId())
                .stream()
                .filter(a -> a.getSportAndType() != null && a.getSportAndType().getId() == sport.getId())
                .toList();

        // Check each badge
        for (Badge badge : allRelevantBadges) {
            // Skip if already earned
            if (hasEarned(badge, athlete)) {
                continue;
            }

            double total = computeCumulativeMetric(athleteSportActivities, badge.getMetric());
            if (total >= badge.getThreshold()) {
                awardBadge(badge, athlete);
            }
        }
    }

    /**
     * Computes the cumulative or maximum value of a metric across all given activities.
     * For {@code MEAN_VELOCITY} and {@code REPS_PER_MINUTE}, the maximum is used;
     * for {@code COUNT}, the number of distinct days is returned;
     * for other metrics the sum is used.
     *
     * @param activities the list of activities to aggregate
     * @param metric     the metric to compute
     * @return the computed cumulative or maximum metric value
     */
    private double computeCumulativeMetric(List<Activity> activities, Metric metric) {
        double total = 0;

        if (metric == Metric.COUNT) {
            // For COUNT metric, return the number of distinct days with activities
            return activities.stream()
                    .map(Activity::getDateA) // Get the date (without time)
                    .distinct()
                    .count();
        }

        for (Activity a : activities) {
            switch (metric) {
                case DISTANCE:
                    total += (a.getDistance() != null) ? a.getDistance() : 0;
                    break;
                case DURATION:
                    total += a.getDuration();
                    break;
                case REPETITION:
                    total += (a.getRepetition() != null) ? a.getRepetition() : 0;
                    break;
                case MEAN_VELOCITY:
                    // duration in minutes, distance in km -> km/h
                    if (a.getDistance() != null && a.getDuration() > 0) {
                        double velocity = a.getDistance() / (a.getDuration() / 60.0);
                        total = Math.max(velocity, total);
                    }
                    break;
                case REPS_PER_MINUTE:
                    if (a.getRepetition() != null && a.getDuration() > 0) {
                        double rpm = a.getRepetition() / (a.getDuration() / 60.0);
                        total = Math.max(rpm, total);
                    }
                    break;
                default:
                    // should be unreachable
                    break;
            }
        }
        return total;
    }

    /**
     * Checks whether the specified athlete has already earned the given badge.
     *
     * @param badge   the badge to check
     * @param athlete the athlete to verify
     * @return {@code true} if the athlete has already earned the badge
     */
    private boolean hasEarned(Badge badge, Athlete athlete) {
        if (badge.getEarnedBy() == null) {
            return false;
        }
        return badge.getEarnedBy().stream()
                .anyMatch(a -> a.getId().equals(athlete.getId()));
    }

    /**
     * Awards the given badge to the specified athlete and sends a notification.
     *
     * @param badge   the badge to award
     * @param athlete the athlete receiving the badge
     */
    private void awardBadge(Badge badge, Athlete athlete) {
        if (badge.getEarnedBy() == null) {
            badge.setEarnedBy(new ArrayList<>());
        }
        badge.getEarnedBy().add(athlete);
        badgeRepository.save(badge);
        if (notificationService != null) {
            notificationService.notifyBadgeEarned(athlete, badge);
        }
    }
}