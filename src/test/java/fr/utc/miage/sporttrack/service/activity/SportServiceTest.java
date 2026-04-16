package fr.utc.miage.sporttrack.service.activity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import fr.utc.miage.sporttrack.repository.activity.SportRepository;

@ExtendWith(MockitoExtension.class)
class SportServiceTest {

    private static final int SPORT_ID = 1;
    private static final String SPORT_NAME = "Course à pied";
    private static final String SPORT_DESCRIPTION = "Sport de course";
    private static final double CALORIES_PER_HOUR = 500.0;
    private static final SportType SPORT_TYPE = SportType.DURATION;

    @Mock
    private SportRepository sportRepository;

    @InjectMocks
    private SportService sportService;

    /**
     * Test : findAll() retourne la liste de tous les sports
     */
    @Test
    void shouldReturnAllSports() {
        Sport sport1 = createSport(1, "Course", "Running", 500, SportType.DURATION);
        Sport sport2 = createSport(2, "Natation", "Swimming", 400, SportType.DISTANCE);
        List<Sport> expected = List.of(sport1, sport2);

        when(sportRepository.findAll()).thenReturn(expected);

        List<Sport> result = sportService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Course", result.get(0).getName());
        assertEquals("Natation", result.get(1).getName());
        verify(sportRepository, times(1)).findAll();
    }

    /**
     * Test : findAll() retourne une liste vide
     */
    @Test
    void shouldReturnEmptyListWhenNoSports() {
        when(sportRepository.findAll()).thenReturn(List.of());

        List<Sport> result = sportService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sportRepository, times(1)).findAll();
    }

    /**
     * Test : findById() retourne un sport existant
     */
    @Test
    void shouldFindSportById() {
        Sport sport = createSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        when(sportRepository.findById(SPORT_ID)).thenReturn(Optional.of(sport));

        Optional<Sport> result = sportService.findById(SPORT_ID);

        assertTrue(result.isPresent());
        assertEquals(SPORT_NAME, result.get().getName());
        verify(sportRepository, times(1)).findById(SPORT_ID);
    }

    /**
     * Test : findById() retourne Optional vide pour un sport inexistant
     */
    @Test
    void shouldReturnEmptyOptionalWhenSportNotFound() {
        when(sportRepository.findById(SPORT_ID)).thenReturn(Optional.empty());

        Optional<Sport> result = sportService.findById(SPORT_ID);

        assertTrue(result.isEmpty());
        verify(sportRepository, times(1)).findById(SPORT_ID);
    }

    /**
     * Test : createSport() crée un nouveau sport avec succès
     */
    @Test
    void shouldCreateSportSuccessfully() {
        Sport sportToSave = new Sport();
        sportToSave.setName(SPORT_NAME);
        sportToSave.setDescription(SPORT_DESCRIPTION);
        sportToSave.setCaloriesPerHour(CALORIES_PER_HOUR);
        sportToSave.setType(SPORT_TYPE);

        Sport savedSport = createSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        when(sportRepository.save(any(Sport.class))).thenReturn(savedSport);

        Sport result = sportService.createSport(SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);

        assertNotNull(result);
        assertEquals(SPORT_NAME, result.getName());
        assertEquals(CALORIES_PER_HOUR, result.getCaloriesPerHour());
        verify(sportRepository, times(1)).save(any(Sport.class));
    }

    /**
     * Test : createSport() lève une exception avec un nom vide
     */
    @Test
    void shouldThrowExceptionWhenCreatingSportWithEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            sportService.createSport("", SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        });
        verify(sportRepository, never()).save(any(Sport.class));
    }

    /**
     * Test : createSport() lève une exception avec un nom null
     */
    @Test
    void shouldThrowExceptionWhenCreatingSportWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            sportService.createSport(null, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        });
        verify(sportRepository, never()).save(any(Sport.class));
    }

    /**
     * Test : createSport() lève une exception avec calories négatives
     */
    @Test
    void shouldThrowExceptionWhenCreatingSportWithNegativeCalories() {
        assertThrows(IllegalArgumentException.class, () -> {
            sportService.createSport(SPORT_NAME, SPORT_DESCRIPTION, -100, SPORT_TYPE);
        });
        verify(sportRepository, never()).save(any(Sport.class));
    }

    /**
     * Test : createSport() lève une exception avec calories zéro
     */
    @Test
    void shouldThrowExceptionWhenCreatingSportWithZeroCalories() {
        assertThrows(IllegalArgumentException.class, () -> {
            sportService.createSport(SPORT_NAME, SPORT_DESCRIPTION, 0, SPORT_TYPE);
        });
        verify(sportRepository, never()).save(any(Sport.class));
    }

    /**
     * Test : updateSport() met à jour un sport existant
     */
    @Test
    void shouldUpdateSportSuccessfully() {
        Sport existingSport = createSport(SPORT_ID, "Ancien nom", "Ancienne description", 300, SportType.DISTANCE);
        when(sportRepository.findById(SPORT_ID)).thenReturn(Optional.of(existingSport));

        Sport updatedSport = createSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        when(sportRepository.save(any(Sport.class))).thenReturn(updatedSport);

        Sport result = sportService.updateSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);

        assertNotNull(result);
        assertEquals(SPORT_NAME, result.getName());
        assertEquals(SPORT_DESCRIPTION, result.getDescription());
        verify(sportRepository, times(1)).findById(SPORT_ID);
        verify(sportRepository, times(1)).save(any(Sport.class));
    }

    /**
     * Test : updateSport() lève une exception si le sport n'existe pas
     */
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentSport() {
        when(sportRepository.findById(SPORT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            sportService.updateSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        });
        verify(sportRepository, times(1)).findById(SPORT_ID);
        verify(sportRepository, never()).save(any(Sport.class));
    }

    /**
     * Test : updateSport() lève une exception avec un nom vide
     */
    @Test
    void shouldThrowExceptionWhenUpdatingWithEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            sportService.updateSport(SPORT_ID, "", SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        });
        verify(sportRepository, never()).findById(anyInt());
        verify(sportRepository, never()).save(any(Sport.class));
    }

    /**
     * Test : enableSport() active un sport existant
     */
    @Test
    void shouldEnableSportSuccessfully() {
        Sport sport = createSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        sport.setActive(false);
        when(sportRepository.findById(SPORT_ID)).thenReturn(Optional.of(sport));

        Sport enabledSport = createSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        enabledSport.setActive(true);
        when(sportRepository.save(any(Sport.class))).thenReturn(enabledSport);

        sportService.enableSport(SPORT_ID);

        verify(sportRepository, times(1)).findById(SPORT_ID);
        verify(sportRepository, times(1)).save(any(Sport.class));
    }

    /**
     * Test : enableSport() lève une exception si le sport n'existe pas
     */
    @Test
    void shouldThrowExceptionWhenEnablingNonExistentSport() {
        when(sportRepository.findById(SPORT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            sportService.enableSport(SPORT_ID);
        });
        verify(sportRepository, times(1)).findById(SPORT_ID);
        verify(sportRepository, never()).save(any(Sport.class));
    }

    /**
     * Test : disableSport() désactive un sport existant
     */
    @Test
    void shouldDisableSportSuccessfully() {
        Sport sport = createSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        sport.setActive(true);
        when(sportRepository.findById(SPORT_ID)).thenReturn(Optional.of(sport));

        Sport disabledSport = createSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        disabledSport.setActive(false);
        when(sportRepository.save(any(Sport.class))).thenReturn(disabledSport);

        sportService.disableSport(SPORT_ID);

        verify(sportRepository, times(1)).findById(SPORT_ID);
        verify(sportRepository, times(1)).save(any(Sport.class));
    }

    /**
     * Test : disableSport() lève une exception si le sport n'existe pas
     */
    @Test
    void shouldThrowExceptionWhenDisablingNonExistentSport() {
        when(sportRepository.findById(SPORT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            sportService.disableSport(SPORT_ID);
        });
        verify(sportRepository, times(1)).findById(SPORT_ID);
        verify(sportRepository, never()).save(any(Sport.class));
    }

    /**
     * Test : findAllActive() retourne uniquement les sports actifs
     */
    @Test
    void shouldFindAllActiveSports() {
        Sport activeSport1 = createSport(1, "Course", "Running", 500, SportType.DURATION);
        activeSport1.setActive(true);
        Sport activeSport2 = createSport(2, "Natation", "Swimming", 400, SportType.DISTANCE);
        activeSport2.setActive(true);
        List<Sport> activeSports = List.of(activeSport1, activeSport2);

        when(sportRepository.findAllByActive(true)).thenReturn(activeSports);

        List<Sport> result = sportService.findAllActive();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Sport::isActive));
        verify(sportRepository, times(1)).findAllByActive(true);
    }

    /**
     * Test : findAllActive() retourne une liste vide quand aucun sport n'est actif
     */
    @Test
    void shouldReturnEmptyListWhenNoActiveSports() {
        when(sportRepository.findAllByActive(true)).thenReturn(List.of());

        List<Sport> result = sportService.findAllActive();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sportRepository, times(1)).findAllByActive(true);
    }

    /**
     * Méthode helper pour créer un sport de test
     */
    private Sport createSport(int id, String name, String description, double caloriesPerHour, SportType type) {
        Sport sport = new Sport();
        sport.setId(id);
        sport.setName(name);
        sport.setDescription(description);
        sport.setCaloriesPerHour(caloriesPerHour);
        sport.setType(type);
        return sport;
    }

    @Test
    void shouldReturnSafeSportName() {
        Sport sportWithName = createSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        assertEquals(SPORT_NAME, sportService.safeSportName(sportWithName));

        Sport sportWithNullName = createSport(SPORT_ID, null, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        assertEquals("Autre", sportService.safeSportName(sportWithNullName));

        Sport sportWithEmptyName = createSport(SPORT_ID, "", SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        assertEquals("Autre", sportService.safeSportName(sportWithEmptyName));

        assertEquals("Autre", sportService.safeSportName(null));
    }
}
