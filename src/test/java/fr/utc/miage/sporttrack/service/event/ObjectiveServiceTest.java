package fr.utc.miage.sporttrack.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

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

@ExtendWith(MockitoExtension.class)
class ObjectiveServiceTest {
    
    private static final String NAME = "Courir 100km au total";
    private static final String DESCRIPTION = "Endurance";
    private static final Athlete ATHLETE = new Athlete();
    private static final Sport SPORT = new Sport();
    
    @Mock
    private ObjectiveRepository objectiveRepository;
    
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
        verify(objectiveRepository, times(1)).save(objective);
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
        objective.setSport(SPORT);
        boolean result = objectiveService.isObjectiveCompleted(objective, null);
        assertEquals(false, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnFalseWhenActivitiesIsEmpty() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(SPORT);
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of());
        assertEquals(false, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnFalseWhenNoMatchingActivity() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(SPORT);
        
        Activity activity = new Activity();
        Sport differentSport = new Sport();
        differentSport.setId(999);
        activity.setSportAndType(differentSport);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(false, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnTrueWhenSportTypeIsNull() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(SPORT);
        
        Activity activity = new Activity();
        SPORT.setType(null);
        activity.setSportAndType(SPORT);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(true, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnTrueWhenDistanceTypeAndValidDistance() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(SPORT);
        SPORT.setType(SportType.DISTANCE);
        
        Activity activity = new Activity();
        activity.setSportAndType(SPORT);
        activity.setDistance(5.0);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(true, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnFalseWhenDistanceTypeAndInvalidDistance() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(SPORT);
        SPORT.setType(SportType.DISTANCE);
        
        Activity activity = new Activity();
        activity.setSportAndType(SPORT);
        activity.setDistance(0.0);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(false, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnTrueWhenRepetitionTypeAndValidRepetition() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(SPORT);
        SPORT.setType(SportType.REPETITION);
        
        Activity activity = new Activity();
        activity.setSportAndType(SPORT);
        activity.setRepetition(10);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(true, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnFalseWhenRepetitionTypeAndInvalidRepetition() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(SPORT);
        SPORT.setType(SportType.REPETITION);
        
        Activity activity = new Activity();
        activity.setSportAndType(SPORT);
        activity.setRepetition(0);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(false, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnTrueWhenMatchingActivityExists() {
        SPORT.setId(1);
        
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(SPORT);
        
        Activity activity = new Activity();
        activity.setSportAndType(SPORT);
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
        objective.setSport(SPORT);
        
        Activity activity = new Activity();
        activity.setSportAndType(null);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(false, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnFalseWhenActivitySportIdDoesNotMatch() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(SPORT);
        
        Activity activity = new Activity();
        Sport differentSport = new Sport();
        differentSport.setId(999);
        activity.setSportAndType(differentSport);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(false, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnTrueWhenActivitySportTypeIsNull() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(SPORT);
        
        Activity activity = new Activity();
        SPORT.setType(null);
        activity.setSportAndType(SPORT);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(true, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnTrueWhenActivitySportTypeIsDistanceAndValidDistance() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(SPORT);
        SPORT.setType(SportType.DISTANCE);
        
        Activity activity = new Activity();
        activity.setSportAndType(SPORT);
        activity.setDistance(5.0);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(true, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnFalseWhenActivitySportTypeIsDistanceAndInvalidDistance() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(SPORT);
        SPORT.setType(SportType.DISTANCE);
        
        Activity activity = new Activity();
        activity.setSportAndType(SPORT);
        activity.setDistance(0.0);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(false, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnTrueWhenActivitySportTypeIsRepetitionAndValidRepetition() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(SPORT);
        SPORT.setType(SportType.REPETITION);
        
        Activity activity = new Activity();
        activity.setSportAndType(SPORT);
        activity.setRepetition(10);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(true, result);
    }
    
    @Test
    void isObjectiveCompletedShouldReturnFalseWhenActivitySportTypeIsRepetitionAndInvalidRepetition() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setSport(SPORT);
        SPORT.setType(SportType.REPETITION);
        
        Activity activity = new Activity();
        activity.setSportAndType(SPORT);
        activity.setRepetition(0);
        
        boolean result = objectiveService.isObjectiveCompleted(objective, List.of(activity));
        assertEquals(false, result);
    }
}