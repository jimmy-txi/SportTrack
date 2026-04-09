package fr.utc.miage.sporttrack.service.User;

import fr.utc.miage.sporttrack.entity.User.Athlete;
import fr.utc.miage.sporttrack.repository.User.AthleteRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AthleteServiceTest {

    private static final String EMAIL = "czy@test.com";
    private static final String SECOND_USERNAME = "Chen1";
    private static final String FIRST_USERNAME = "Chen2";
    private static final String PASSWORD = "123456";

    @Test
    void shouldRefuseCreationWhenEmailIsAlreadyUsed() {
        AthleteRepository repository = new AthleteRepository();
        AthleteService service = new AthleteService(repository);

        Athlete firstAthlete = new Athlete();
        firstAthlete.setUsername(FIRST_USERNAME);
        firstAthlete.setPassword(PASSWORD);
        firstAthlete.setEmail(EMAIL);

        Athlete secondAthlete = new Athlete();
        secondAthlete.setUsername(SECOND_USERNAME);
        secondAthlete.setPassword(PASSWORD);
        secondAthlete.setEmail(EMAIL);

        service.createProfile(firstAthlete);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createProfile(secondAthlete)
        );

        assertEquals("Email is already used", exception.getMessage());
    }

}