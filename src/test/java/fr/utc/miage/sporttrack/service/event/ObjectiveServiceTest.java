package fr.utc.miage.sporttrack.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.event.ObjectiveRepository;
import fr.utc.miage.sporttrack.service.user.communication.NotificationService;

@ExtendWith(MockitoExtension.class)
class ObjectiveServiceTest {
    
    private static final String NAME = "Courir 100km au total";
    private static final String DESCRIPTION = "Endurance";
    private static final Athlete ATHLETE = new Athlete();
    private static final Sport SPORT = new Sport();
    
    @Mock
    private ObjectiveRepository objectiveRepository;

    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private ObjectiveService objectiveService;
    
    @Test
    void shouldGetObjectivesByUser() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        List<Objective> expected = List.of(objective);
        
        when(objectiveRepository.findByAthlete(ATHLETE)).thenReturn(expected);
        
        Iterable<Objective> result = objectiveService.getObjectivesByUser(ATHLETE);
        
        assertNotNull(result);
        assertEquals(expected, result);
        verify(objectiveRepository, times(1)).findByAthlete(ATHLETE);
    }
    
    @Test
    void shouldSaveObjectiveWhenAllValid() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        
        objectiveService.saveObjective(objective, ATHLETE, SPORT);
        
        assertEquals(ATHLETE, objective.getUser());
        assertEquals(SPORT, objective.getSport());
        assertFalse(objective.isCompleted());
        assertEquals(null, objective.getCompletedAt());
        verify(objectiveRepository, times(1)).save(objective);
    }

    @Test
    void markAsCompletedShouldThrowWhenAthleteIsNull() {
        assertThrows(IllegalArgumentException.class, () -> objectiveService.markAsCompleted(1, null));
    }

    @Test
    void markAsCompletedShouldThrowWhenAthleteIdIsNull() {
        Athlete athleteWithoutId = new Athlete();
        athleteWithoutId.setUsername("u");
        athleteWithoutId.setPassword("p");
        athleteWithoutId.setEmail("u@mail.com");

        assertThrows(IllegalArgumentException.class, () -> objectiveService.markAsCompleted(1, athleteWithoutId));
    }

    @Test
    void markAsCompletedShouldThrowWhenObjectiveNotFound() {
        Athlete athleteWithId = createAthleteWithId(101);
        when(objectiveRepository.findByIdAndAthlete_Id(1, athleteWithId.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> objectiveService.markAsCompleted(1, athleteWithId));
    }

    @Test
    void markAsCompletedShouldUpdateObjectiveAndNotify() {
        Athlete athleteWithId = createAthleteWithId(102);
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setAthlete(athleteWithId);
        objective.setSport(SPORT);
        objective.setCompleted(false);

        when(objectiveRepository.findByIdAndAthlete_Id(2, athleteWithId.getId())).thenReturn(Optional.of(objective));

        Objective result = objectiveService.markAsCompleted(2, athleteWithId);

        assertTrue(result.isCompleted());
        assertNotNull(result.getCompletedAt());
        verify(objectiveRepository).save(objective);
        verify(notificationService).notifyObjectiveCompleted(athleteWithId, objective);
    }

    @Test
    void markAsCompletedShouldNotSaveWhenAlreadyCompleted() {
        Athlete athleteWithId = createAthleteWithId(103);
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setCompleted(true);

        when(objectiveRepository.findByIdAndAthlete_Id(3, athleteWithId.getId())).thenReturn(Optional.of(objective));

        Objective result = objectiveService.markAsCompleted(3, athleteWithId);

        assertTrue(result.isCompleted());
        verify(objectiveRepository, never()).save(any());
        verify(notificationService, never()).notifyObjectiveCompleted(any(), any());
    }
    
    @Test
    void shouldNotSaveWhenObjectiveIsNull() {
        objectiveService.saveObjective(null, ATHLETE, SPORT);
        verify(objectiveRepository, never()).save(any());
    }
    
    @Test
    void shouldNotSaveWhenAthleteIsNull() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objectiveService.saveObjective(objective, null, SPORT);
        verify(objectiveRepository, never()).save(any());
    }
    
    @Test
    void shouldNotSaveWhenSportIsNull() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objectiveService.saveObjective(objective, ATHLETE, null);
        verify(objectiveRepository, never()).save(any());
    }
    
    @Test   
    void deleteByIdShouldCallRepository() {
        int id = 1;
        objectiveService.deleteById(id);
        verify(objectiveRepository, times(1)).deleteById(id);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnFalseWhenObjectiveIsNull() {
        boolean result = objectiveService.isObjectiveCompleted(null, List.of());
        assertEquals(false, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnFalseWhenSportIsNull() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of());
        assertEquals(false, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnFalseWhenActivitiesIsNull() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(createSport(1, null));
        boolean result = objectiveService.isObjectiveCompleted(objective, null);
        assertEquals(false, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnFalseWhenActivitiesIsEmpty() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(createSport(1, null));
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of());
        assertEquals(false, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnFalseWhenNoMatchingActivity() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(createSport(1, null));
        
        Activity activity = new Activity();
        Sport differentSport = createSport(999, null);
        activity.setSportAndType(differentSport);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(false, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnTrueWhenSportTypeIsNull() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(createSport(1, null));
        
        Activity activity = new Activity();
        activity.setSportAndType(createSport(1, null));
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(true, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnTrueWhenDistanceTypeAndValidDistance() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        Sport sport = createSport(1, SportType.DISTANCE);
        objective.setSport(sport);
        
        Activity activity = new Activity();
        activity.setSportAndType(sport);
        activity.setDistance(5.0);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(true, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnFalseWhenDistanceTypeAndInvalidDistance() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        Sport sport = createSport(1, SportType.DISTANCE);
        objective.setSport(sport);
        
        Activity activity = new Activity();
        activity.setSportAndType(sport);
        activity.setDistance(0.0);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(false, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnTrueWhenRepetitionTypeAndValidRepetition() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        Sport sport = createSport(1, SportType.REPETITION);
        objective.setSport(sport);
        
        Activity activity = new Activity();
        activity.setSportAndType(sport);
        activity.setRepetition(10);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(true, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnFalseWhenRepetitionTypeAndInvalidRepetition() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        Sport sport = createSport(1, SportType.REPETITION);
        objective.setSport(sport);
        
        Activity activity = new Activity();
        activity.setSportAndType(sport);
        activity.setRepetition(0);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(false, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnTrueWhenMatchingActivityExists() {
        Sport sport = createSport(1, SportType.DISTANCE);
        
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(sport);
        
        Activity activity = new Activity();
        activity.setSportAndType(sport);
        activity.setDistance(10.0); 
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertTrue(result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnFalseWhenActivityInListIsNull() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(SPORT);
        
        List<Activity> activities = Arrays.asList((Activity) null);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, activities);
        assertFalse(result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnFalseWhenActivitySportIsNull() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(createSport(1, null));
        
        Activity activity = new Activity();
        activity.setSportAndType(null);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(false, result);
    }

    private Athlete createAthleteWithId(int id) {
        Athlete athlete = new Athlete();
        athlete.setUsername("athlete" + id);
        athlete.setPassword("pwd");
        athlete.setEmail("athlete" + id + "@mail.com");
        try {
            Field field = athlete.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(athlete, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return athlete;
    }

    private Sport createSport(int id, SportType type) {
        Sport sport = new Sport();
        sport.setId(id);
        sport.setType(type);
        return sport;
    }
}