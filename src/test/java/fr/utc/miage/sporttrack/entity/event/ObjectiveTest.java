package fr.utc.miage.sporttrack.entity.event;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.user.Athlete;

import static org.junit.jupiter.api.Assertions.*;

class ObjectiveTest {
   

    private static final String NAME = "Courir 100km au total";
    private static final String DESCRIPTION = "Je veux courir 20km pour améliorer mon endurance.";
    private static final Athlete ATHLETE = new Athlete();
    private static final Sport SPORT = new Sport();

    @Test
    void shouldCreateObjectiveSuccessfullyWithValidData() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        objective.setAthlete(ATHLETE);
        objective.setSport(SPORT);

        assertEquals(NAME, objective.getName());
        assertEquals(DESCRIPTION, objective.getDescription());
        assertEquals(ATHLETE, objective.getUser());
        assertEquals(SPORT, objective.getSport());
    }

    @Test
    void shouldAllowDescriptionToBeNull() {
        Objective objective = new Objective(NAME, null);
        objective.setAthlete(ATHLETE);
        objective.setSport(SPORT);

        assertEquals(NAME, objective.getName());
        assertNull(objective.getDescription());
        assertEquals(ATHLETE, objective.getUser());
        assertEquals(SPORT, objective.getSport());
    }

    @Test
    void shouldNotAllowNameToBeNull() {
        assertThrows(NullPointerException.class, () -> new Objective(null, DESCRIPTION));
    }

    @Test
    void shouldAllowUpdatingDescription() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        String newDescription = "Je veux courir 30km pour améliorer mon endurance.";
        objective.setDescription(newDescription);

        assertEquals(newDescription, objective.getDescription());
    }

    @Test
    void shouldAllowUpdatingName() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        String newName = "Courir 150km au total";
        objective.setName(newName);

        assertEquals(newName, objective.getName());
    }

    @Test
    void shouldStoreCompletionStatusAndDate() {
        Objective objective = new Objective(NAME, DESCRIPTION);
        LocalDateTime completedAt = LocalDateTime.of(2026, 4, 16, 12, 0);

        objective.setCompleted(true);
        objective.setCompletedAt(completedAt);

        assertTrue(objective.isCompleted());
        assertEquals(completedAt, objective.getCompletedAt());
    }

    @Test
    void shouldKeepDefaultCompletionValuesOnCreation() {
        Objective objective = new Objective(NAME, DESCRIPTION);

        assertFalse(objective.isCompleted());
        assertNull(objective.getCompletedAt());
    }
}
