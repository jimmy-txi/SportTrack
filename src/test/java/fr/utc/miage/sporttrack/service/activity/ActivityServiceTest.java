package fr.utc.miage.sporttrack.service.activity;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.activity.ActivityRepository;
import fr.utc.miage.sporttrack.repository.activity.SportRepository;
import fr.utc.miage.sporttrack.repository.activity.WeatherReportRepository;
import fr.utc.miage.sporttrack.service.event.ChallengeRankingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private SportRepository sportRepository;

    @Mock
    private WeatherReportRepository weatherReportRepository;

    @Mock
    private ChallengeRankingService challengeRankingService;

    @InjectMocks
    private ActivityService activityService;

    @Test
    void shouldCreateActivitySuccessfullyForAthlete_Test34() {
        Athlete athlete = buildAthlete(10);
        Sport sport = buildSport(1, SportType.DISTANCE);

        when(sportRepository.findById(1)).thenReturn(Optional.of(sport));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity persisted = invocation.getArgument(0);
            persisted.setId(100);
            return persisted;
        });

        Activity result = activityService.createActivityForAthlete(
                athlete,
                1.5,
                "Sortie footing",
                "Seance facile",
                0,
                10.0,
                LocalDate.now(),
                LocalTime.of(8, 0),
                "Compiegne",
                1
        );

        assertNotNull(result);
        assertEquals(100, result.getId());

        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        verify(activityRepository).save(captor.capture());
        Activity persisted = captor.getValue();
        assertEquals("Sortie footing", persisted.getTitle());
        assertEquals("Compiegne", persisted.getLocationCity());
        assertEquals(athlete, persisted.getCreatedBy());
        assertEquals(sport.getId(), persisted.getSportAndType().getId());
        verify(challengeRankingService).recomputeRankingsForActivity(10, 1, persisted.getDateA());
    }

    @Test
    void shouldRefuseActivityCreationWhenMandatoryFieldsMissing_Test35() {
        Athlete athlete = buildAthlete(10);
        LocalDate activityDate = LocalDate.now();
        LocalTime activityTime = LocalTime.of(9, 0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.createActivityForAthlete(
                        athlete,
                        1.0,
                        "",
                        "Sans titre valide",
                        0,
                        5.0,
                        activityDate,
                        activityTime,
                        "Compiegne",
                        1
                )
        );

        assertEquals("Activity title cannot be null or empty", exception.getMessage());
        verify(activityRepository, never()).save(any(Activity.class));
    }

    @Test
    void shouldReturnAllActivitiesForAthlete_Test36() {
        Athlete athlete = buildAthlete(42);
        List<Activity> expected = List.of(new Activity(), new Activity());

        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(42)).thenReturn(expected);

        List<Activity> result = activityService.findAllByAthlete(athlete);

        assertEquals(2, result.size());
        verify(activityRepository).findByCreatedBy_IdOrderByDateADescStartTimeDesc(42);
    }

    @Test
    void shouldReturnAllActivitiesForMultipleAthletes() {
        List<Activity> expected = List.of(new Activity(), new Activity(), new Activity());

        when(activityRepository.findByCreatedBy_IdInOrderByDateADescStartTimeDesc(List.of(2, 3))).thenReturn(expected);

        List<Activity> result = activityService.findAllByAthleteIds(List.of(2, 3));

        assertEquals(3, result.size());
        verify(activityRepository).findByCreatedBy_IdInOrderByDateADescStartTimeDesc(List.of(2, 3));
    }

    @Test
    void shouldReturnEmptyListWhenAthleteIdsMissing() {
        List<Activity> result = activityService.findAllByAthleteIds(List.of());

        assertTrue(result.isEmpty());
        verify(activityRepository, never()).findByCreatedBy_IdInOrderByDateADescStartTimeDesc(any());
    }

    @Test
    void shouldFindAllActivities() {
        when(activityRepository.findAll()).thenReturn(List.of(new Activity(), new Activity(), new Activity()));

        List<Activity> result = activityService.findAll();

        assertEquals(3, result.size());
        verify(activityRepository).findAll();
    }

    @Test
    void shouldFindById() {
        Activity activity = new Activity();
        activity.setId(7);
        when(activityRepository.findById(7)).thenReturn(Optional.of(activity));

        Optional<Activity> result = activityService.findById(7);

        assertTrue(result.isPresent());
        assertEquals(7, result.get().getId());
    }

    @Test
    void shouldReturnEmptyWhenFindByIdNotFound() {
        when(activityRepository.findById(404)).thenReturn(Optional.empty());

        Optional<Activity> result = activityService.findById(404);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindByIdForAthlete() {
        Athlete athlete = buildAthlete(9);
        Activity activity = new Activity();
        activity.setId(33);

        when(activityRepository.findByIdAndCreatedBy_Id(33, 9)).thenReturn(Optional.of(activity));

        Optional<Activity> result = activityService.findByIdForAthlete(33, athlete);

        assertTrue(result.isPresent());
        verify(activityRepository).findByIdAndCreatedBy_Id(33, 9);
    }

    @Test
    void shouldRejectFindByIdForAthleteWhenAthleteMissing() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.findByIdForAthlete(1, null)
        );
        assertEquals("Athlete is required", ex.getMessage());
    }

    @Test
    void shouldUpdateActivitySuccessfully() {
        Sport sport = buildSport(2, SportType.DISTANCE);
        Activity existing = new Activity();
        existing.setId(5);
        existing.setCreatedBy(buildAthlete(7));
        existing.setDateA(LocalDate.now().minusDays(4));
        existing.setSportAndType(sport);

        when(sportRepository.findById(2)).thenReturn(Optional.of(sport));
        when(activityRepository.findById(5)).thenReturn(Optional.of(existing));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Activity updated = activityService.updateActivity(
                5,
                2.5,
                "Sortie longue",
                "MAJ",
                0,
                18.2,
                LocalDate.now().minusDays(2),
                LocalTime.of(7, 30),
                "Lille",
                2
        );

        assertEquals("Sortie longue", updated.getTitle());
        assertEquals("Lille", updated.getLocationCity());
        assertEquals(18.2, updated.getDistance());
        verify(challengeRankingService, times(2)).recomputeRankingsForActivity(eq(7), eq(2), any(LocalDate.class));
    }

    @Test
    void shouldRejectUpdateWhenActivityUnknown() {
        Sport sport = buildSport(2, SportType.DISTANCE);
        when(sportRepository.findById(2)).thenReturn(Optional.of(sport));
        when(activityRepository.findById(77)).thenReturn(Optional.empty());
        LocalDate activityDate = LocalDate.now().minusDays(1);
        LocalTime activityTime = LocalTime.of(8, 0);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.updateActivity(
                77, 1.2, "x", "x", 0, 5,
                activityDate, activityTime, "Paris", 2
                )
        );
        assertEquals("Activity not found with id: 77", ex.getMessage());
    }

    @Test
    void shouldDeleteActivitySuccessfully() {
        Activity activity = new Activity();
        activity.setDateA(LocalDate.now().minusDays(1));
        activity.setCreatedBy(buildAthlete(55));
        Sport sport = new Sport();
        sport.setId(4);
        activity.setSportAndType(sport);
        when(activityRepository.findById(10)).thenReturn(Optional.of(activity));

        activityService.deleteById(10);

        verify(weatherReportRepository).deleteByActivity_Id(10);
        verify(activityRepository).deleteById(10);
        verify(challengeRankingService).recomputeRankingsForActivity(55, 4, activity.getDateA());
    }

    @Test
    void shouldRejectDeleteWhenActivityUnknown() {
        when(activityRepository.findById(88)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.deleteById(88)
        );
        assertEquals("Activity not found with id: 88", ex.getMessage());
    }

    @Test
    void shouldDeleteActivityForAthleteSuccessfully() {
        Athlete athlete = buildAthlete(15);
        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setDateA(LocalDate.now().minusDays(2));
        Sport sport = new Sport();
        sport.setId(6);
        activity.setSportAndType(sport);
        when(activityRepository.findByIdAndCreatedBy_Id(3, 15)).thenReturn(Optional.of(activity));

        activityService.deleteByIdForAthlete(athlete, 3);

        verify(weatherReportRepository).deleteByActivity_Id(3);
        verify(activityRepository).deleteById(3);
        verify(challengeRankingService).recomputeRankingsForActivity(15, 6, activity.getDateA());
    }

    @Test
    void shouldRejectDeleteForAthleteWhenNotOwner() {
        Athlete athlete = buildAthlete(15);
        when(activityRepository.findByIdAndCreatedBy_Id(4, 15)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.deleteByIdForAthlete(athlete, 4)
        );
        assertEquals("Activity not found for current athlete", ex.getMessage());
    }

    @Test
    void shouldRejectCreationForRepetitionSportWhenRepetitionMissing() {
        Athlete athlete = buildAthlete(10);
        Sport sport = buildSport(5, SportType.REPETITION);
        when(sportRepository.findById(5)).thenReturn(Optional.of(sport));
        LocalDate activityDate = LocalDate.now().minusDays(1);
        LocalTime activityTime = LocalTime.of(10, 0);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.createActivityForAthlete(
                        athlete, 1.0, "Pompes", "", 0, 0,
                activityDate, activityTime, "Compiegne", 5
                )
        );

        assertEquals("Repetitions must be greater than zero for this sport", ex.getMessage());
    }

    private Athlete buildAthlete(int id) {
        Athlete athlete = new Athlete();
        athlete.setUsername("athlete" + id);
        athlete.setPassword("pwd");
        athlete.setEmail("athlete" + id + "@mail.com");
        try {
            java.lang.reflect.Field idField = athlete.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(athlete, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return athlete;
    }

    private Sport buildSport(int id, SportType type) {
        Sport sport = new Sport();
        sport.setId(id);
        sport.setName("Course");
        sport.setDescription("Running");
        sport.setCaloriesPerHour(500);
        sport.setType(type);
        return sport;
    }
}
