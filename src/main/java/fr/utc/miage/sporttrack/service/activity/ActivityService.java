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

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final SportRepository sportRepository;
    private final WeatherReportRepository weatherReportRepository;
    private final ChallengeRankingService challengeRankingService;
    private final FriendshipService friendshipService;
    private final NotificationService notificationService;

    public ActivityService(ActivityRepository activityRepository,
                           SportRepository sportRepository,
                           WeatherReportRepository weatherReportRepository,
                           ChallengeRankingService challengeRankingService) {
        this(activityRepository, sportRepository, weatherReportRepository, challengeRankingService, null, null);
    }

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

    public List<Activity> findAll() {
        return activityRepository.findAll();
    }

    public List<Activity> findAllByAthlete(Athlete athlete) {
        if (athlete == null || athlete.getId() == null) {
            throw new IllegalArgumentException("Athlete is required");
        }
        return activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(athlete.getId());
    }

    public List<Activity> findAllByAthleteIds(List<Integer> athleteIds) {
        if (athleteIds == null || athleteIds.isEmpty()) {
            return List.of();
        }
        return activityRepository.findByCreatedBy_IdInOrderByDateADescStartTimeDesc(athleteIds);
    }

    public Optional<Activity> findById(int id) {
        return activityRepository.findById(id);
    }

    public Optional<Activity> findByIdForAthlete(int id, Athlete athlete) {
        if (athlete == null || athlete.getId() == null) {
            throw new IllegalArgumentException("Athlete is required");
        }
        return activityRepository.findByIdAndCreatedBy_Id(id, athlete.getId());
    }

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

    private void recalculateImpactedChallengeRankings(Athlete athlete, Sport sport, LocalDate activityDate) {
        Integer athleteId = athlete != null ? athlete.getId() : null;
        Integer sportId = sport != null ? sport.getId() : null;
        challengeRankingService.recomputeRankingsForActivity(athleteId, sportId, activityDate);
    }

    Sport checkSport(int sportId) {
        if (sportId <= 0) {
            throw new IllegalArgumentException("Sport is required");
        }
        return sportRepository.findById(sportId)
                .orElseThrow(() -> new IllegalArgumentException("Sport not found with id: " + sportId));
    }

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

    void checkDateA(LocalDate dateA) {
        if (dateA == null) {
            throw new IllegalArgumentException("Activity date is required");
        }
        if (dateA.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Activity date cannot be in the future");
        }
    }

    void checkLocationCity(String locationCity) {
        if (locationCity == null || locationCity.isBlank()) {
            throw new IllegalArgumentException("Location city cannot be null or empty");
        }
    }

    void checkTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Activity title cannot be null or empty");
        }
    }

    void checkStartTime(java.time.LocalTime startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Activity start time is required");
        }
    }

    public boolean filterBySport(Activity activity, Sport selectedSport) {
        if (selectedSport == null) {
            return true;
        }
        return activity != null && activity.getSportAndType() != null && activity.getSportAndType().getId() == selectedSport.getId();
    }

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
