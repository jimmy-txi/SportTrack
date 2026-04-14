package fr.utc.miage.sporttrack.entity.user;

import fr.utc.miage.sporttrack.entity.enumeration.Gender;
import fr.utc.miage.sporttrack.entity.enumeration.PracticeLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AthleteTest {

    private static final String USERNAME = "Chen";
    private static final String PASSWORD = "123456";
    private static final String EMAIL = "czy@test.com";
    private static final String FIRST_NAME = "Zheng yi";
    private static final String LAST_NAME = "Chen";
    private static final Gender GENDER = Gender.MALE;
    private static final Integer AGE = 23;
    private static final Double HEIGHT = 185.0;
    private static final Double WEIGHT = 85.0;
    private static final PracticeLevel PRACTICE_LEVEL = PracticeLevel.BEGINNER;
    private static final String BIO = "I like running";

    //for US #1
    @Test
    void shouldCreateAthleteSuccessfullyWithValidData() {
        Athlete athlete = new Athlete();

        athlete.setUsername(USERNAME);
        athlete.setPassword(PASSWORD);
        athlete.setEmail(EMAIL);
        athlete.setFirstName(FIRST_NAME);
        athlete.setLastName(LAST_NAME);
        athlete.setGender(GENDER);
        athlete.setAge(AGE);
        athlete.setHeight(HEIGHT);
        athlete.setWeight(WEIGHT);
        athlete.setPracticeLevel(PRACTICE_LEVEL);
        athlete.setBio(BIO);

        assertEquals(USERNAME, athlete.getUsername());
        assertEquals(PASSWORD, athlete.getPassword());
        assertEquals(EMAIL, athlete.getEmail());
        assertEquals(FIRST_NAME, athlete.getFirstName());
        assertEquals(LAST_NAME, athlete.getLastName());
        assertEquals(GENDER, athlete.getGender());
        assertEquals(AGE, athlete.getAge());
        assertEquals(HEIGHT, athlete.getHeight());
        assertEquals(WEIGHT, athlete.getWeight());
        assertEquals(PRACTICE_LEVEL, athlete.getPracticeLevel());
        assertEquals(BIO, athlete.getBio());
    }

    //for US #1
    @Test
    void shouldAllowOptionalFieldsToBeNull() {
        Athlete athlete = new Athlete();

        athlete.setUsername(USERNAME);
        athlete.setPassword(PASSWORD);
        athlete.setEmail(EMAIL);

        assertEquals(USERNAME, athlete.getUsername());
        assertEquals(PASSWORD, athlete.getPassword());
        assertEquals(EMAIL, athlete.getEmail());

        assertNull(athlete.getGender());
        assertNull(athlete.getAge());
        assertNull(athlete.getHeight());
        assertNull(athlete.getWeight());
        assertNull(athlete.getPracticeLevel());
        assertNull(athlete.getBio());
    }

    //for US #1
    @Test
    void shouldFailWhenUsernameIsMissing() {
        Athlete athlete = new Athlete();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> athlete.setUsername(null)
        );

        assertEquals("Username is required", exception.getMessage());
    }

    //for US #1
    @Test
    void shouldFailWhenPasswordIsMissing() {
        Athlete athlete = new Athlete();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> athlete.setPassword(null)
        );

        assertEquals("Password is required", exception.getMessage());
    }

    //for US #1
    @Test
    void shouldFailWhenEmailIsMissing() {
        Athlete athlete = new Athlete();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> athlete.setEmail(null)
        );

        assertEquals("Email is required", exception.getMessage());
    }

    //for US #1
    @Test
    void shouldFailWhenUsernameIsBlank() {
        Athlete athlete = new Athlete();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> athlete.setUsername("   ")
        );

        assertEquals("Username is required", exception.getMessage());
    }

    //for US #1
    @Test
    void shouldFailWhenPasswordIsBlank() {
        Athlete athlete = new Athlete();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> athlete.setPassword("   ")
        );

        assertEquals("Password is required", exception.getMessage());
    }

    //for US #1
    @Test
    void shouldFailWhenEmailIsBlank() {
        Athlete athlete = new Athlete();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> athlete.setEmail("   ")
        );

        assertEquals("Email is required", exception.getMessage());
    }
}
