package fr.utc.miage.sporttrack.service.activity;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.activity.ActivityRepository;
import fr.utc.miage.sporttrack.repository.activity.SportRepository;
import fr.utc.miage.sporttrack.repository.activity.WeatherReportRepository;
import fr.utc.miage.sporttrack.service.event.ChallengeRankingService;
import fr.utc.miage.sporttrack.service.user.communication.FriendshipService;
import fr.utc.miage.sporttrack.service.user.communication.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service layer component responsible for managing {@link Activity} entities
 * within the SportTrack application.
 *
 * <p>Provides business logic for creating, updating, deleting, and querying
 * activities. Each mutation triggers recalculation of impacted challenge
 * rankings and, where applicable, sends notifications to friends.</p>
 *
 * @author SportTrack Team
 */
@Service
public class ActivityService {

    /** The repository for activity data access. */
    private final ActivityRepository activityRepository;

    /** The repository for sport data access. */
    private final SportRepository sportRepository;

    /** The repository for weather report data access. */
    private final WeatherReportRepository weatherReportRepository;

    /** The service for recomputing challenge rankings. */
    private final ChallengeRankingService challengeRankingService;

    /** The service for resolving friendship relationships (optional, may be {@code null}). */
    private final FriendshipService friendshipService;

    /** The service for sending in-app notifications (optional, may be {@code null}). */
    private final NotificationService notificationService;

    /**
     * Constructs an {@code ActivityService} without friendship/notification support.
     *
     * @param activityRepository      the activity repository
     * @param sportRepository         the sport repository
     * @param weatherReportRepository the weather report repository
     * @param challengeRankingService the challenge ranking service
     */
    public ActivityService(ActivityRepository activityRepository,
                           SportRepository sportRepository,
                           WeatherReportRepository weatherReportRepository,
                           ChallengeRankingService challengeRankingService) {
        this(activityRepository, sportRepository, weatherReportRepository, challengeRankingService, null, null);
    }

    /**
     * Constructs an {@code ActivityService} with full friendship and notification support.
     *
     * @param activityRepository      the activity repository
     * @param sportRepository         the sport repository
     * @param weatherReportRepository the weather report repository
     * @param challengeRankingService the challenge ranking service
     * @param friendshipService       the friendship service for resolving friends
     * @param notificationService     the notification service for pushing activity events
     */
    @Autowired
    public ActivityService(ActivityRepository activityRepository,
                           SportRepository sportRepository,
                           WeatherReportRepository weatherReportRepository,
                           ChallengeRankingService challengeRankingService,
                           FriendshipService friendshipService,
                           NotificationService notificationService) {
        this.activityRepository = activityRepository;
        this.sportRepository = sportRepository;
        this.weatherReportRepository = weatherReportRepository;
        this.challengeRankingService = challengeRankingService;
        this.friendshipService = friendshipService;
        this.notificationService = notificationService;
    }

    /**
     * Returns all activities in the database.
     *
     * @return a list of all activities
     */
    public List<Activity> findAll() {
        return activityRepository.findAll();
    }

    /**
     * Returns all activities created by the specified athlete, ordered newest first.
     *
     * @param athlete the athlete whose activities should be retrieved
     * @return a list of activities ordered by date and start time descending
     * @throws IllegalArgumentException if the athlete or their identifier is null
     */
    public List<Activity> findAllByAthlete(Athlete athlete) {
        if (athlete == null || athlete.getId() == null) {
            throw new IllegalArgumentException("Athlete is required");
        }
        return activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(athlete.getId());
    }

    /**
     * Returns all activities created by any of the specified athlete identifiers.
     *
     * @param athleteIds the list of athlete identifiers; if null or empty, an empty list is returned
     * @return a list of matching activities ordered newest first
     */
    public List<Activity> findAllByAthleteIds(List<Integer> athleteIds) {
        if (athleteIds == null || athleteIds.isEmpty()) {
            return List.of();
        }
        return activityRepository.findByCreatedBy_IdInOrderByDateADescStartTimeDesc(athleteIds);
    }

    /**
     * Finds an activity by its unique identifier.
     *
     * @param id the activity identifier
     * @return an {@link Optional} containing the activity if found
     */
    public Optional<Activity> findById(int id) {
        return activityRepository.findById(id);
    }

    /**
     * Finds an activity by identifier, restricted to those owned by the specified athlete.
     *
     * @param id      the activity identifier
     * @param athlete the athlete who must own the activity
     * @return an {@link Optional} containing the activity if found and owned by the athlete
     * @throws IllegalArgumentException if the athlete or their identifier is null
     */
    public Optional<Activity> findByIdForAthlete(int id, Athlete athlete) {
        if (athlete == null || athlete.getId() == null) {
            throw new IllegalArgumentException("Athlete is required");
        }
        return activityRepository.findByIdAndCreatedBy_Id(id, athlete.getId());
    }

    /**
     * Creates and persists a new activity for the specified athlete with full validation.
     * After saving, notifications are sent to friends and impacted challenge rankings are recomputed.
     *
     * @param athlete      the athlete who is creating the activity
     * @param duration     the duration of the activity in hours
     * @param title        the user-defined title of the activity
     * @param description  an optional description
     * @param repetition   the repetition count (for repetition-based sports)
     * @param distance     the distance in kilometres (for distance-based sports)
     * @param dateA        the date the activity took place
     * @param startTime    the local start time of the activity
     * @param locationCity the city or location name
     * @param sportId      the identifier of the associated sport
     * @return the newly created and persisted {@link Activity}
     * @throws IllegalArgumentException if any validation fails
     */
    public Activity createActivityForAthlete(Athlete athlete, double duration, String title, String description, int repetition, double distance, LocalDate dateA, java.time.LocalTime startTime, String locationCity, int sportId) {
        if (athlete == null || athlete.getId() == null) {
            throw new IllegalArgumentException("Athlete is required");
        }

        checkLocationCity(locationCity);
        checkDateA(dateA);
        checkTitle(title);
        checkStartTime(startTime);
        Sport sport = checkSport(sportId);
        checkMetricBySportType(sport.getType(), duration, repetition, distance);

        Activity activity = new Activity();
        activity.setDuration(duration);
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setRepetition(repetition);
        activity.setDistance(distance);
        activity.setDateA(dateA);
        activity.setStartTime(startTime);
        activity.setLocationCity(locationCity);
        activity.setSportAndType(sport);
        activity.setCreatedBy(athlete);

        Activity savedActivity = activityRepository.save(activity);
        if (notificationService != null && friendshipService != null) {
            notificationService.notifyActivityPublished(
                    savedActivity.getCreatedBy(),
                    savedActivity,
                    friendshipService.getFriendsOfAthlete(savedActivity.getCreatedBy().getId())
            );
        }
        recalculateImpactedChallengeRankings(savedActivity.getCreatedBy(), savedActivity.getSportAndType(), savedActivity.getDateA());
        return savedActivity;
    }

    /**
     * Updates an existing activity by its identifier with full validation.
     * Both old and new challenge rankings are recomputed after the update.
     *
     * @param id           the activity identifier to update
     * @param duration     the new duration in hours
     * @param title        the new title
     * @param description  the new description
     * @param repetition   the new repetition count
     * @param distance     the new distance in kilometres
     * @param dateA        the new activity date
     * @param startTime    the new start time
     * @param locationCity the new location city
     * @param sportId      the new sport identifier
     * @return the updated and persisted {@link Activity}
     * @throws IllegalArgumentException if the activity is not found or validation fails
     */
    public Activity updateActivity(int id, double duration, String title, String description, int repetition, double distance, LocalDate dateA, java.time.LocalTime startTime, String locationCity, int sportId) {
        checkLocationCity(locationCity);
        checkDateA(dateA);
        checkTitle(title);
        checkStartTime(startTime);
        Sport sport = checkSport(sportId);
        checkMetricBySportType(sport.getType(), duration, repetition, distance);

        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found with id: " + id));

        Integer oldAthleteId = activity.getCreatedBy() != null ? activity.getCreatedBy().getId() : null;
        Integer oldSportId = activity.getSportAndType() != null ? activity.getSportAndType().getId() : null;
        LocalDate oldDate = activity.getDateA();

        activity.setDuration(duration);
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setRepetition(repetition);
        activity.setDistance(distance);
        activity.setDateA(dateA);
        activity.setStartTime(startTime);
        activity.setLocationCity(locationCity);
        activity.setSportAndType(sport);

        Activity updatedActivity = activityRepository.save(activity);
        challengeRankingService.recomputeRankingsForActivity(oldAthleteId, oldSportId, oldDate);
        recalculateImpactedChallengeRankings(updatedActivity.getCreatedBy(), updatedActivity.getSportAndType(), updatedActivity.getDateA());
        return updatedActivity;
    }

    /**
     * Updates an activity owned by the specified athlete with full validation.
     * Both old and new challenge rankings are recomputed after the update.
     *
     * @param athlete      the athlete who must own the activity
     * @param id           the activity identifier
     * @param duration     the new duration in hours
     * @param title        the new title
     * @param description  the new description
     * @param repetition   the new repetition count
     * @param distance     the new distance in kilometres
     * @param dateA        the new activity date
     * @param startTime    the new start time
     * @param locationCity the new location city
     * @param sportId      the new sport identifier
     * @return the updated and persisted {@link Activity}
     * @throws IllegalArgumentException if the athlete is invalid, the activity is not found, or validation fails
     */
    public Activity updateActivityForAthlete(Athlete athlete, int id, double duration, String title, String description,
                                             int repetition, double distance, LocalDate dateA,
                                             java.time.LocalTime startTime, String locationCity, int sportId) {
        if (athlete == null || athlete.getId() == null) {
            throw new IllegalArgumentException("Athlete is required");
        }

        Activity activity = activityRepository.findByIdAndCreatedBy_Id(id, athlete.getId())
                .orElseThrow(() -> new IllegalArgumentException("Activity not found for current athlete"));

        Integer oldAthleteId = activity.getCreatedBy() != null ? activity.getCreatedBy().getId() : null;
        Integer oldSportId = activity.getSportAndType() != null ? activity.getSportAndType().getId() : null;
        LocalDate oldDate = activity.getDateA();

        checkLocationCity(locationCity);
        checkDateA(dateA);
        checkTitle(title);
        checkStartTime(startTime);
        Sport sport = checkSport(sportId);
        checkMetricBySportType(sport.getType(), duration, repetition, distance);

        activity.setDuration(duration);
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setRepetition(repetition);
        activity.setDistance(distance);
        activity.setDateA(dateA);
        activity.setStartTime(startTime);
        activity.setLocationCity(locationCity);
        activity.setSportAndType(sport);

        Activity updatedActivity = activityRepository.save(activity);
        challengeRankingService.recomputeRankingsForActivity(oldAthleteId, oldSportId, oldDate);
        recalculateImpactedChallengeRankings(updatedActivity.getCreatedBy(), updatedActivity.getSportAndType(), updatedActivity.getDateA());
        return updatedActivity;
    }

    /**
     * Deletes an activity by its identifier, including its associated weather report.
     * Impacted challenge rankings are recomputed after deletion.
     *
     * @param id the activity identifier to delete
     * @throws IllegalArgumentException if no activity is found with the given identifier
     */
    @Transactional
    public void deleteById(int id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found with id: " + id));

        Integer athleteId = activity.getCreatedBy() != null ? activity.getCreatedBy().getId() : null;
        Integer sportId = activity.getSportAndType() != null ? activity.getSportAndType().getId() : null;
        LocalDate activityDate = activity.getDateA();

        weatherReportRepository.deleteByActivity_Id(id);
        activityRepository.deleteById(id);
        challengeRankingService.recomputeRankingsForActivity(athleteId, sportId, activityDate);
    }

    /**
     * Deletes an activity owned by the specified athlete, including its associated weather report.
     * Impacted challenge rankings are recomputed after deletion.
     *
     * @param athlete the athlete who must own the activity
     * @param id      the activity identifier to delete
     * @throws IllegalArgumentException if the athlete is invalid or the activity is not found
     */
    @Transactional
    public void deleteByIdForAthlete(Athlete athlete, int id) {
        if (athlete == null || athlete.getId() == null) {
            throw new IllegalArgumentException("Athlete is required");
        }

        Activity activity = activityRepository.findByIdAndCreatedBy_Id(id, athlete.getId())
                .orElseThrow(() -> new IllegalArgumentException("Activity not found for current athlete"));

        Integer sportId = activity.getSportAndType() != null ? activity.getSportAndType().getId() : null;
        LocalDate activityDate = activity.getDateA();

        weatherReportRepository.deleteByActivity_Id(id);
        activityRepository.deleteById(id);
        challengeRankingService.recomputeRankingsForActivity(athlete.getId(), sportId, activityDate);
    }

    /**
     * Delegates to the challenge ranking service to recompute rankings for any
     * challenges impacted by an activity belonging to the given athlete, sport,
     * and date.
     *
     * @param athlete      the athlete whose activity triggered the recalculation
     * @param sport        the sport associated with the activity
     * @param activityDate the date of the activity
     */
    private void recalculateImpactedChallengeRankings(Athlete athlete, Sport sport, LocalDate activityDate) {
        Integer athleteId = athlete != null ? athlete.getId() : null;
        Integer sportId = sport != null ? sport.getId() : null;
        challengeRankingService.recomputeRankingsForActivity(athleteId, sportId, activityDate);
    }

    /**
     * Validates that a sport with the given identifier exists.
     *
     * @param sportId the sport identifier
     * @return the resolved {@link Sport} entity
     * @throws IllegalArgumentException if the identifier is invalid or the sport is not found
     */
    Sport checkSport(int sportId) {
        if (sportId <= 0) {
            throw new IllegalArgumentException("Sport is required");
        }
        return sportRepository.findById(sportId)
                .orElseThrow(() -> new IllegalArgumentException("Sport not found with id: " + sportId));
    }

    /**
     * Validates that the metric values are consistent with the given sport type.
     *
     * @param sportType  the sport type determining which metrics are required
     * @param duration   the duration value to validate
     * @param repetition the repetition value to validate (required for REPETITION sports)
     * @param distance   the distance value to validate (required for DISTANCE sports)
     * @throws IllegalArgumentException if any value is invalid for the given sport type
     */
    void checkMetricBySportType(SportType sportType, double duration, int repetition, double distance) {
        if (sportType == null) {
            throw new IllegalArgumentException("Sport type is required");
        }

        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero");
        }

        if (sportType == SportType.REPETITION && repetition <= 0) {
            throw new IllegalArgumentException("Repetitions must be greater than zero for this sport");
        }
        if (sportType == SportType.DISTANCE && distance <= 0) {
            throw new IllegalArgumentException("Distance must be greater than zero for this sport");
        }
    }

    /**
     * Validates that the activity date is not null and not in the future.
     *
     * @param dateA the activity date to validate
     * @throws IllegalArgumentException if the date is null or in the future
     */
    void checkDateA(LocalDate dateA) {
        if (dateA == null) {
            throw new IllegalArgumentException("Activity date is required");
        }
        if (dateA.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Activity date cannot be in the future");
        }
    }

    /**
     * Validates that the location city is not null or blank.
     *
     * @param locationCity the location city name to validate
     * @throws IllegalArgumentException if the value is null or blank
     */
    void checkLocationCity(String locationCity) {
        if (locationCity == null || locationCity.isBlank()) {
            throw new IllegalArgumentException("Location city cannot be null or empty");
        }
    }

    /**
     * Validates that the activity title is not null or blank.
     *
     * @param title the title to validate
     * @throws IllegalArgumentException if the value is null or blank
     */
    void checkTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Activity title cannot be null or empty");
        }
    }

    /**
     * Validates that the start time is not null.
     *
     * @param startTime the start time to validate
     * @throws IllegalArgumentException if the value is null
     */
    void checkStartTime(java.time.LocalTime startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Activity start time is required");
        }
    }

    /**
     * Filters an activity by matching its associated sport against a selected sport.
     *
     * @param activity      the activity to test
     * @param selectedSport the sport to match against; if {@code null}, all activities pass
     * @return {@code true} if the activity matches the selected sport, {@code false} otherwise
     */
    public boolean filterBySport(Activity activity, Sport selectedSport) {
        if (selectedSport == null) {
            return true;
        }
        return activity != null && activity.getSportAndType() != null && activity.getSportAndType().getId() == selectedSport.getId();
    }

    /**
     * Filters an activity by checking whether its date falls within the specified range.
     *
     * @param activity  the activity to test
     * @param startDate the inclusive start date of the range; if {@code null}, no lower bound is applied
     * @param endDate   the inclusive end date of the range; if {@code null}, no upper bound is applied
     * @return {@code true} if the activity date is within the range, {@code false} otherwise
     */
    public boolean filterByDate(Activity activity, LocalDate startDate, LocalDate endDate) {
        if (activity == null || activity.getDateA() == null) {
            return false;
        }
        if (startDate != null && activity.getDateA().isBefore(startDate)) {
            return false;
        }
        if (endDate != null && activity.getDateA().isAfter(endDate)) {
            return false;
        }
        return true;
    }
}
