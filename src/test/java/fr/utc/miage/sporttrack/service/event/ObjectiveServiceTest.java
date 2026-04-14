package fr.utc.miage.sporttrack.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.event.Objective;
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
}