package fr.utc.miage.sporttrack.entity.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Metric;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.user.Athlete;

public class ChallengeTest {
 
    private static final String NAME = "Défi de course à pied";
    private static final String DESCRIPTION = "Un défi pour courir la plus grande distance cumulée en une semaine.";
    private static final LocalDate START_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate END_DATE = LocalDate.of(2024, 1, 7);
    private static final Athlete ATHLETE = new Athlete();
    private static final Sport SPORT = new Sport();

    @Test
    void shouldCreateChallengeSuccessfullyWithValidData() {
        Challenge challenge = new Challenge(NAME, DESCRIPTION, START_DATE, END_DATE, Metric.DURATION);
        challenge.setOrganizer(ATHLETE);
        challenge.setSport(SPORT);

        assertEquals(NAME, challenge.getNom());
        assertEquals(DESCRIPTION, challenge.getDescription());
        assertEquals(ATHLETE, challenge.getOrganizer());
        assertEquals(SPORT, challenge.getSport());
    }

    @Test
    void shouldAllowUpdatingDescription() {
        Challenge challenge = new Challenge(NAME, DESCRIPTION, START_DATE, END_DATE, Metric.DURATION);
        String newDescription = "Un défi pour courir la plus grande distance cumulée en deux semaines.";
        challenge.setDescription(newDescription);

        assertEquals(newDescription, challenge.getDescription());
    }

    @Test
    void shouldAllowUpdatingName() {
        Challenge challenge = new Challenge(NAME, DESCRIPTION, START_DATE, END_DATE, Metric.DURATION);
        String newName = "Défi de course à pied - Version 2";
        challenge.setNom(newName);

        assertEquals(newName, challenge.getNom());
    }

    @Test
    void shouldAllowUpdatingDates() {
        Challenge challenge = new Challenge(NAME, DESCRIPTION, START_DATE, END_DATE, Metric.DURATION);
        LocalDate newStartDate = LocalDate.of(2024, 2, 1);
        LocalDate newEndDate = LocalDate.of(2024, 2, 7);
        challenge.setDateDebut(newStartDate);
        challenge.setDateFin(newEndDate);

        assertEquals(newStartDate, challenge.getDateDebut());
        assertEquals(newEndDate, challenge.getDateFin());
    }

    @Test
    void shouldAllowUpdatingMetric() {
        Challenge challenge = new Challenge(NAME, DESCRIPTION, START_DATE, END_DATE, Metric.DURATION);
        challenge.setMetric(Metric.DISTANCE);

        assertEquals(Metric.DISTANCE, challenge.getMetric());
    }
}
