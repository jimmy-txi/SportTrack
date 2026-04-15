package fr.utc.miage.sporttrack.service.event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.entity.event.Badge;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.activity.ActivityRepository;
import fr.utc.miage.sporttrack.repository.activity.SportRepository;
import fr.utc.miage.sporttrack.repository.event.BadgeRepository;

@Service
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final ActivityRepository activityRepository;
    private final SportRepository sportRepository;

    public BadgeService(BadgeRepository badgeRepository,
                        ActivityRepository activityRepository,
                        SportRepository sportRepository) {
        this.badgeRepository = badgeRepository;
        this.activityRepository = activityRepository;
        this.sportRepository = sportRepository;
    }

    // ========== Admin CRUD ==========

    public void saveBadge(Badge badge, Sport sport) {
        if (badge == null || sport == null) {
            throw new IllegalArgumentException("Badge and Sport are required");
        }
        badge.setSport(sport);
        if (badge.getEarnedBy() == null) {
            badge.setEarnedBy(new ArrayList<>());
        }
        badgeRepository.save(badge);
    }

    public List<Badge> findAll() {
        return badgeRepository.findAll();
    }

    public Badge findById(int id) {
        return badgeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found with id: " + id));
    }

    public void deleteById(int id) {
        if (!badgeRepository.existsById(id)) {
            throw new IllegalArgumentException("Badge not found with id: " + id);
        }
        badgeRepository.deleteById(id);
    }

    // ========== Athlete Badge Queries ==========

    /**
     * Get all badges earned by an athlete.
     */
    public List<Badge> getEarnedBadges(Integer athleteId) {
        return badgeRepository.findByEarnedBy_Id(athleteId);
    }

    /**
     * Get all badges NOT yet earned by an athlete (with description showing conditions).
     */
    public List<Badge> getUnearnedBadges(Integer athleteId) {
        List<Badge> earned = getEarnedBadges(athleteId);
        List<Integer> earnedIds = earned.stream().map(Badge::getId).collect(Collectors.toList());
        if (earnedIds.isEmpty()) {
            return badgeRepository.findAll();
        }
        return badgeRepository.findByIdNotIn(earnedIds);
    }

    /**
     * Get the N most recently earned badges for an athlete.
     * Since we don't track earnedAt, we return the last N from the earned list.
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
     * Check and award badges after an activity is created.
     * Only checks badges related to the activity's sport.
     */
    public void checkAndAwardBadges(Activity activity) {
        if (activity == null || activity.getCreatedBy() == null || activity.getSportAndType() == null) {
            return;
        }

        Athlete athlete = activity.getCreatedBy();
        Sport sport = activity.getSportAndType();

        // Find all badges for this sport
        List<Badge> sportBadges = badgeRepository.findBySportId(sport.getId());
        if (sportBadges.isEmpty()) {
            return;
        }

        // Get all activities for this athlete in this sport
        List<Activity> athleteSportActivities = activityRepository
                .findByCreatedBy_IdOrderByDateADescStartTimeDesc(athlete.getId())
                .stream()
                .filter(a -> a.getSportAndType() != null && a.getSportAndType().getId() == sport.getId())
                .collect(Collectors.toList());

        // Check each badge
        for (Badge badge : sportBadges) {
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
     * Compute the cumulative sum of a metric across all given activities.
     */
    private double computeCumulativeMetric(List<Activity> activities, Metric metric) {
        double total = 0;
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
                        total += a.getDistance() / (a.getDuration() / 60.0);
                    }
                    break;
                case REPS_PER_MINUTE:
                    if (a.getRepetition() != null && a.getDuration() > 0) {
                        total += a.getRepetition() / (a.getDuration() / 60.0);
                    }
                    break;
            }
        }
        return total;
    }

    /**
     * Check if an athlete has already earned a badge.
     */
    private boolean hasEarned(Badge badge, Athlete athlete) {
        if (badge.getEarnedBy() == null) {
            return false;
        }
        return badge.getEarnedBy().stream()
                .anyMatch(a -> a.getId().equals(athlete.getId()));
    }

    /**
     * Award a badge to an athlete.
     */
    private void awardBadge(Badge badge, Athlete athlete) {
        if (badge.getEarnedBy() == null) {
            badge.setEarnedBy(new ArrayList<>());
        }
        badge.getEarnedBy().add(athlete);
        badgeRepository.save(badge);
    }
}