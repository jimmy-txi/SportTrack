package fr.utc.miage.sporttrack.entity.activity;

import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ActivityTest {

    @Test
    void shouldCoverGettersSettersAndDerivedMethods() {
        Activity activity = new Activity();

        activity.setId(12);
        activity.setDuration(1.75);
        activity.setTitle("Seance");
        activity.setDescription("Description");
        activity.setRepetition(20);
        activity.setDistance(8.5);
        activity.setDateA(LocalDate.of(2026, 4, 14));
        activity.setStartTime(LocalTime.of(9, 30));
        activity.setLocationCity("Compiegne");

        Sport sport = new Sport();
        sport.setId(3);
        sport.setType(SportType.REPETITION);
        activity.setSportAndType(sport);

        WeatherReport weatherReport = new WeatherReport();
        activity.setWeatherReport(weatherReport);

        Athlete athlete = buildAthlete("john", "john@mail.com", "John", "Doe");
        activity.setCreatedBy(athlete);

        assertEquals(12, activity.getId());
        assertEquals(1.75, activity.getDuration());
        assertEquals("Seance", activity.getTitle());
        assertEquals("Description", activity.getDescription());
        assertEquals(20, activity.getRepetition());
        assertEquals(8.5, activity.getDistance());
        assertEquals(LocalDate.of(2026, 4, 14), activity.getDateA());
        assertEquals(LocalTime.of(9, 30), activity.getStartTime());
        assertEquals("Compiegne", activity.getLocationCity());
        assertSame(sport, activity.getSportAndType());
        assertEquals(3, activity.getSportId());
        assertTrue(activity.hasRepetitions());
        assertFalse(activity.hasDistance());
        assertSame(weatherReport, activity.getWeatherReport());
        assertSame(athlete, activity.getCreatedBy());
        assertEquals("John Doe", activity.getCreatedByDisplayName());
    }

    @Test
    void shouldResolveDisplayNameBranches() {
        Activity activity = new Activity();

        assertEquals("Inconnu", activity.getCreatedByDisplayName());

        Athlete usernameOnly = buildAthlete("runner42", "runner@mail.com", null, null);
        activity.setCreatedBy(usernameOnly);
        assertEquals("runner42", activity.getCreatedByDisplayName());

        Athlete emailOnly = buildAthlete("tmp", "emailonly@mail.com", " ", " ");
        setUserField(emailOnly, "username", null);
        activity.setCreatedBy(emailOnly);
        assertEquals("emailonly@mail.com", activity.getCreatedByDisplayName());

        Athlete unknown = buildAthlete("tmp2", "tmp2@mail.com", null, null);
        setUserField(unknown, "username", null);
        setUserField(unknown, "email", null);
        activity.setCreatedBy(unknown);
        assertEquals("Inconnu", activity.getCreatedByDisplayName());
    }

    @Test
    void shouldHandleSportIdTransitionsAndDistanceFlag() {
        Activity activity = new Activity();

        assertNull(activity.getSportId());
        assertFalse(activity.hasDistance());
        assertFalse(activity.hasRepetitions());

        activity.setSportId(7);
        assertNotNull(activity.getSportAndType());
        assertEquals(7, activity.getSportAndType().getId());
        assertEquals(7, activity.getSportId());

        Sport distanceSport = new Sport();
        distanceSport.setId(9);
        distanceSport.setType(SportType.DISTANCE);
        activity.setSportAndType(distanceSport);
        assertTrue(activity.hasDistance());
        assertFalse(activity.hasRepetitions());

        activity.setSportId(0);
        assertNull(activity.getSportAndType());
        assertEquals(0, activity.getSportId());

        activity.setSportId(null);
        assertNull(activity.getSportAndType());
        assertNull(activity.getSportId());
    }

    @Test
    void shouldCalculateCaloriesBurnedFromDurationAndSportCaloriesPerHour() {
        Activity activity = new Activity();
        Sport sport = new Sport();
        sport.setCaloriesPerHour(420.0);

        activity.setSportAndType(sport);
        activity.setDuration(1.5);

        assertEquals(630.0, activity.getCaloriesBurned());
    }

    @Test
    void shouldReturnZeroCaloriesBurnedWhenDurationOrSportIsInvalid() {
        Activity activity = new Activity();
        Sport sport = new Sport();
        sport.setCaloriesPerHour(500.0);

        activity.setDuration(0.0);
        activity.setSportAndType(sport);
        assertEquals(0.0, activity.getCaloriesBurned());

        activity.setDuration(-1.0);
        assertEquals(0.0, activity.getCaloriesBurned());

        activity.setDuration(1.0);
        activity.setSportAndType(null);
        assertEquals(0.0, activity.getCaloriesBurned());
    }

    private Athlete buildAthlete(String username, String email, String firstName, String lastName) {
        Athlete athlete = new Athlete();
        athlete.setUsername(username);
        athlete.setPassword("secret");
        athlete.setEmail(email);
        athlete.setFirstName(firstName);
        athlete.setLastName(lastName);
        return athlete;
    }

    private void setUserField(Athlete athlete, String fieldName, Object value) {
        try {
            Field field = athlete.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(athlete, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
