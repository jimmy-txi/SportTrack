package fr.utc.miage.sporttrack.service.activity;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.activity.ActivityRepository;
import fr.utc.miage.sporttrack.repository.activity.SportRepository;
import fr.utc.miage.sporttrack.repository.activity.WeatherReportRepository;
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

    @InjectMocks
    private ActivityService activityService;

    @Test
    void shouldCreateActivitySuccessfullyForAthlete_Test34() {
        Athlete athlete = buildAthlete(10);
        Sport sport = buildSport(1, SportType.DISTANCE);
        Activity saved = new Activity();
        saved.setId(100);

        when(sportRepository.findById(1)).thenReturn(Optional.of(sport));
        when(activityRepository.save(any(Activity.class))).thenReturn(saved);

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
        when(activityRepository.existsById(10)).thenReturn(true);

        activityService.deleteById(10);

        verify(weatherReportRepository).deleteByActivity_Id(10);
        verify(activityRepository).deleteById(10);
    }

    @Test
    void shouldRejectDeleteWhenActivityUnknown() {
        when(activityRepository.existsById(88)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.deleteById(88)
        );
        assertEquals("Activity not found with id: 88", ex.getMessage());
    }

    @Test
    void shouldDeleteActivityForAthleteSuccessfully() {
        Athlete athlete = buildAthlete(15);
        when(activityRepository.existsByIdAndCreatedBy_Id(3, 15)).thenReturn(true);

        activityService.deleteByIdForAthlete(athlete, 3);

        verify(weatherReportRepository).deleteByActivity_Id(3);
        verify(activityRepository).deleteById(3);
    }

    @Test
    void shouldRejectDeleteForAthleteWhenNotOwner() {
        Athlete athlete = buildAthlete(15);
        when(activityRepository.existsByIdAndCreatedBy_Id(4, 15)).thenReturn(false);

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

    @Test
    void shouldFilterByDate() {
        Activity activity = new Activity();
        activity.setDateA(LocalDate.of(2024, 1, 15));

        assertTrue(activityService.filterByDate(activity, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31)));
        assertFalse(activityService.filterByDate(activity, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 28)));
    }

    @Test
    void shouldFilterBySport() {
        Sport sport1 = new Sport();
        sport1.setId(1);
        Sport sport2 = new Sport();
        sport2.setId(2);

        Activity activity = new Activity();
        activity.setSportAndType(sport1);

        assertTrue(activityService.filterBySport(activity, sport1));
        assertFalse(activityService.filterBySport(activity, sport2));
    }

    @Test
    void shouldNotFilterWhenCriteriaMissing() {
        Activity activity = new Activity();
        activity.setDateA(LocalDate.of(2024, 1, 15));
        Sport sport = new Sport();
        sport.setId(1);
        activity.setSportAndType(sport);

        assertTrue(activityService.filterByDate(activity, null, null));
        assertTrue(activityService.filterBySport(activity, null));
    }

    @Test
    void checkDateAShouldThrowWhenMissing() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.checkDateA(null)
        );
        assertEquals("Activity date is required", ex.getMessage());
    }

    @Test
    void checkDateAShouldNotThrowWhenPresent() {
        assertDoesNotThrow(() -> activityService.checkDateA(LocalDate.now()));
    }

    @Test
    void shouldThrowWhenSportIdFewerThanOne() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.checkSport(0)
        );
        assertEquals("Sport is required", ex.getMessage());
    }

    @Test
    void shouldThrowWhenSportNotFound() {
        when(sportRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.checkSport(99)
        );
        assertEquals("Sport not found with id: 99", ex.getMessage());
    }

    @Test
    void shouldThrowWhenDurationInvalidForSportType() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.checkMetricBySportType(SportType.DISTANCE, -1, 0, 0)
        );
        assertEquals("Duration must be greater than zero", ex.getMessage());
    }

    @Test
    void shouldThrowWhenLocationCityMissing() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.checkLocationCity("  ")
        );
        assertEquals("Location city cannot be null or empty", ex.getMessage());
    }

    @Test
    void shouldThrowWhenStartTimeMissing() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.checkStartTime(null)
        );
        assertEquals("Activity start time is required", ex.getMessage());
    }

    @Test
    void shouldNotThrowWhenStartTimePresent() {
        assertDoesNotThrow(() -> activityService.checkStartTime(LocalTime.of(8, 0)));
    }

    @Test
    void shouldNotThrowWhenLocationCityPresent() {
        assertDoesNotThrow(() -> activityService.checkLocationCity("Paris"));
    }

    @Test
    void shouldNotThrowWhenDurationValidForSportType() {
        assertDoesNotThrow(() -> activityService.checkMetricBySportType(SportType.DISTANCE, 1.0, 0, 1));
    }

    @Test
    void shouldNotThrowWhenRepetitionValidForSportType() {
        assertDoesNotThrow(() -> activityService.checkMetricBySportType(SportType.REPETITION, 1.0, 10, 1));
    }

    @Test
    void shouldReturnFalseWhenActivityNullForFilterByDate() {
        assertFalse(activityService.filterByDate(null, LocalDate.now(), LocalDate.now()));
    }

    @Test
    void shouldReturnFalseWhenActivityDateNullForFilterByDate() {
        Activity activity = new Activity();
        assertFalse(activityService.filterByDate(activity, LocalDate.now(), LocalDate.now()));
    }

    @Test
    void shouldReturnFalseWhenActivityNullForFilterBySport() {
        assertFalse(activityService.filterBySport(null, new Sport()));
    }

    @Test
    void shouldReturnFalseWhenActivitySportNullForFilterBySport() {
        Activity activity = new Activity();
        assertFalse(activityService.filterBySport(activity, new Sport()));
    }

    @Test
    void shouldReturnFalseWhenDatesInvalidForFilterByDate() {
        Activity activity = new Activity();
        activity.setDateA(LocalDate.of(2024, 1, 15));
        assertFalse(activityService.filterByDate(activity, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 28)));
    }

    @Test
    void shouldReturnFalseWhenSportDoesNotMatchForFilterBySport() {
        Activity activity = new Activity();
        Sport sport1 = new Sport();
        sport1.setId(1);
        activity.setSportAndType(sport1);
        Sport sport2 = new Sport();
        sport2.setId(2);
        assertFalse(activityService.filterBySport(activity, sport2));
    }

    @Test
    void shouldReturnTrueWhenSportMatchesForFilterBySport() {
        Activity activity = new Activity();
        Sport sport = new Sport();
        sport.setId(1);
        activity.setSportAndType(sport);
        assertTrue(activityService.filterBySport(activity, sport));
    }

    @Test
    void shouldThrowWhenAthleteIsNullForCreateActivity() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.createActivityForAthlete(
                        null, 1.0, "Title", "Description", 0, 5.0,
                        LocalDate.now(), LocalTime.of(8, 0), "City", 1
                )
        );
        assertEquals("Athlete is required", ex.getMessage());
    }

    @Test
    void shouldThrowWhenAthleteIdIsNullForCreateActivity() {
        Athlete athlete = new Athlete();
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.createActivityForAthlete(
                        athlete, 1.0, "Title", "Description", 0, 5.0,
                        LocalDate.now(), LocalTime.of(8, 0), "City", 1
                )
        );
        assertEquals("Athlete is required", ex.getMessage());
    }
}
