package fr.utc.miage.sporttrack.service.User;

import fr.utc.miage.sporttrack.entity.User.Athlete;
import fr.utc.miage.sporttrack.repository.User.AthleteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

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

        Athlete updatedData = new Athlete();
        updatedData.setUsername("newUsername");
        updatedData.setFirstName("New");
        updatedData.setLastName("Name");
        updatedData.setAge(25);
        updatedData.setHeight(175.5);
        updatedData.setWeight(70.0);
        updatedData.setGender(fr.utc.miage.sporttrack.entity.Enumeration.Gender.MALE);
        updatedData.setPracticeLevel(fr.utc.miage.sporttrack.entity.Enumeration.PracticeLevel.ADVANCED);
        updatedData.setBio("Updated bio");

        when(repository.findByEmail(EMAIL)).thenReturn(java.util.Optional.of(existingAthlete));

        service.updateProfile(EMAIL, updatedData);

        assertEquals("newUsername", existingAthlete.getUsername());
        assertEquals("New", existingAthlete.getFirstName());
        assertEquals("Name", existingAthlete.getLastName());
        assertEquals(25, existingAthlete.getAge());
        assertEquals(175.5, existingAthlete.getHeight());
        assertEquals(70.0, existingAthlete.getWeight());
        assertEquals(fr.utc.miage.sporttrack.entity.Enumeration.Gender.MALE, existingAthlete.getGender());
        assertEquals(fr.utc.miage.sporttrack.entity.Enumeration.PracticeLevel.ADVANCED, existingAthlete.getPracticeLevel());
        assertEquals("Updated bio", existingAthlete.getBio());

        org.mockito.Mockito.verify(repository).save(existingAthlete);
    }
}
