package fr.utc.miage.sporttrack.entity.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;


import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.entity.user.Athlete;

class ChallengeTest {
 
    private static final String NAME = "Défi de course à pied";
    private static final String DESCRIPTION = "Un défi pour courir la plus grande distance cumulée en une semaine.";
    private static final LocalDate START_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate END_DATE = LocalDate.of(2024, 1, 7);
    private static final Metric TYPE = Metric.DURATION;
    private static final Athlete ATHLETE = new Athlete();
    private static final Sport SPORT = new Sport();

    @Test
    void shouldCreateChallengeSuccessfullyWithValidData() {
        Challenge challenge = new Challenge(NAME, DESCRIPTION, START_DATE, END_DATE, TYPE);
        challenge.setOrganizer(ATHLETE);
        challenge.setSport(SPORT);

        assertEquals(NAME, challenge.getName());
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
        challenge.setName(newName);

        assertEquals(newName, challenge.getName());
    }

    @Test
    void shouldAllowUpdatingDates() {
        Challenge challenge = new Challenge(NAME, DESCRIPTION, START_DATE, END_DATE, Metric.DURATION);
        LocalDate newStartDate = LocalDate.of(2024, 2, 1);
        LocalDate newEndDate = LocalDate.of(2024, 2, 7);
        challenge.setStartDate(newStartDate);
        challenge.setEndDate(newEndDate);

        assertEquals(newStartDate, challenge.getStartDate());
        assertEquals(newEndDate, challenge.getEndDate());
    }

    @Test
    void shouldAllowUpdatingMetric() {
        Challenge challenge = new Challenge(NAME, DESCRIPTION, START_DATE, END_DATE, Metric.DURATION);
        challenge.setMetric(Metric.DISTANCE);

        assertEquals(Metric.DISTANCE, challenge.getMetric());
    }

    @Test
    void shouldAllowUpdatingParticipants() {
        Challenge challenge = new Challenge(NAME, DESCRIPTION, START_DATE, END_DATE, Metric.DURATION);
        Athlete participant1 = new Athlete();
        Athlete participant2 = new Athlete();
        challenge.setParticipants(List.of(participant1, participant2));

        assertEquals(2, challenge.getParticipants().size());
        assertTrue(challenge.getParticipants().contains(participant1));
        assertTrue(challenge.getParticipants().contains(participant2));
    }

    @Test
    void shouldGetParticipants() {
        Challenge challenge = new Challenge(NAME, DESCRIPTION, START_DATE, END_DATE, Metric.DURATION);
        Athlete participant1 = new Athlete();
        Athlete participant2 = new Athlete();
        challenge.setParticipants(List.of(participant1, participant2));

        List<Athlete> participants = challenge.getParticipants();
        assertEquals(2, participants.size());
        assertTrue(participants.contains(participant1));
        assertTrue(participants.contains(participant2));
    }

    @Test
    void shouldGetId() {
        Challenge challenge = new Challenge(NAME, DESCRIPTION, START_DATE, END_DATE, Metric.DURATION);
        int id = challenge.getId();
        assertEquals(0, id); 
    }
}
