package fr.utc.miage.sporttrack.service.User;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.service.user.AthleteService;

import fr.utc.miage.sporttrack.dto.AthleteProfileUpdateDTO;
import fr.utc.miage.sporttrack.entity.enumeration.Gender;
import fr.utc.miage.sporttrack.entity.enumeration.PracticeLevel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AthleteServiceTest {

    private static final String EMAIL = "czy@test.com";
    private static final String SECOND_USERNAME = "Chen1";
    private static final String FIRST_USERNAME = "Chen2";
    private static final String PASSWORD = "123456";

    @Mock
    private AthleteRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AthleteService service;

    @Test
    void shouldRefuseCreationWhenEmailIsAlreadyUsed() {
        Athlete firstAthlete = new Athlete();
        firstAthlete.setUsername(FIRST_USERNAME);
        firstAthlete.setPassword(PASSWORD);
        firstAthlete.setEmail(EMAIL);

        Athlete secondAthlete = new Athlete();
        secondAthlete.setUsername(SECOND_USERNAME);
        secondAthlete.setPassword(PASSWORD);
        secondAthlete.setEmail(EMAIL);

        when(repository.existsByEmail(EMAIL)).thenReturn(false, true);
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("hashedpassword");

        service.createProfile(firstAthlete);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createProfile(secondAthlete)
        );

        assertEquals("Email is already used", exception.getMessage());
    }

    @Test
    void shouldGetCurrentAthlete() {
        Athlete athlete = new Athlete();
        athlete.setEmail(EMAIL);
        when(repository.findByEmail(EMAIL)).thenReturn(java.util.Optional.of(athlete));

        Athlete result = service.getCurrentAthlete(EMAIL);

        assertNotNull(result);
        assertEquals(EMAIL, result.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenGettingNonexistentAthlete() {
        when(repository.findByEmail("notfound@test.com")).thenReturn(java.util.Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getCurrentAthlete("notfound@test.com")
        );

        assertEquals("Athlete not found", exception.getMessage());
    }

    @Test
    void shouldUpdateProfileSuccessfully() {
        Athlete existingAthlete = new Athlete();
        existingAthlete.setEmail(EMAIL);
        existingAthlete.setUsername("oldUsername");

        AthleteProfileUpdateDTO updatedData = new AthleteProfileUpdateDTO();
        updatedData.setUsername("newUsername");
        updatedData.setFirstName("New");
        updatedData.setLastName("Name");
        updatedData.setAge(25);
        updatedData.setHeight(175.5);
        updatedData.setWeight(70.0);
        updatedData.setGender(Gender.MALE);
        updatedData.setPracticeLevel(PracticeLevel.ADVANCED);
        updatedData.setBio("Updated bio");

        when(repository.findByEmail(EMAIL)).thenReturn(java.util.Optional.of(existingAthlete));

        service.updateProfile(EMAIL, updatedData);

        assertEquals("newUsername", existingAthlete.getUsername());
        assertEquals("New", existingAthlete.getFirstName());
        assertEquals("Name", existingAthlete.getLastName());
        assertEquals(25, existingAthlete.getAge());
        assertEquals(175.5, existingAthlete.getHeight());
        assertEquals(70.0, existingAthlete.getWeight());
        assertEquals(Gender.MALE, existingAthlete.getGender());
        assertEquals(PracticeLevel.ADVANCED, existingAthlete.getPracticeLevel());
        assertEquals("Updated bio", existingAthlete.getBio());

        org.mockito.Mockito.verify(repository).save(existingAthlete);
    }
    @Test
    void shouldSearchAthletesByName() {
        Athlete athlete1 = new Athlete();
        athlete1.setUsername("Chen");

        Athlete athlete2 = new Athlete();
        athlete2.setUsername("chen");

        Athlete athlete3 = new Athlete();
        athlete3.setUsername("John");

        when(repository.findByUsernameContainingIgnoreCase("chen")).thenReturn(List.of(athlete1, athlete2));

        List<Athlete> result = service.searchAthletesByName("chen");

        assertEquals(2, result.size());
        assertTrue(result.contains(athlete1));
        assertTrue(result.contains(athlete2));
    }

    @Test
    void shouldSearchAthletesByPartialName() {
        Athlete athlete1 = new Athlete();
        athlete1.setUsername("Chen");

        Athlete athlete2 = new Athlete();
        athlete2.setUsername("chen");

        Athlete athlete3 = new Athlete();
        athlete3.setUsername("John");

        when(repository.findByUsernameContainingIgnoreCase("he")).thenReturn(List.of(athlete1, athlete2));

        List<Athlete> result = service.searchAthletesByName("he");

        assertEquals(2, result.size());
        assertTrue(result.contains(athlete1));
        assertTrue(result.contains(athlete2));
    }


    @Test
    void shouldSearchInvalidAthletes() {
        Athlete athlete1 = new Athlete();
        athlete1.setUsername("Chen");

        Athlete athlete2 = new Athlete();
        athlete2.setUsername("chen");

        Athlete athlete3 = new Athlete();
        athlete3.setUsername("John");

        when(repository.findByUsernameContainingIgnoreCase("xyz")).thenReturn(List.of());

        List<Athlete> result = service.searchAthletesByName("xyz");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetAllAthletes() {
        Athlete athlete1 = new Athlete();
        athlete1.setUsername("Chen");

        Athlete athlete2 = new Athlete();
        athlete2.setUsername("John");

        when(repository.findAll()).thenReturn(List.of(athlete1, athlete2));

        List<Athlete> result = service.getAllAthletes();

        assertEquals(2, result.size());
        assertTrue(result.contains(athlete1));
        assertTrue(result.contains(athlete2));
    }

}
