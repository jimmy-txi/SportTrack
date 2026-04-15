package fr.utc.miage.sporttrack.entity.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import fr.utc.miage.sporttrack.entity.user.Athlete;

class ChallengeRankingTest {

    @Test
    void shouldResolveDisplayNameWithFirstAndLastName() {
        Athlete athlete = new Athlete();
        athlete.setFirstName("Ada");
        athlete.setLastName("Lovelace");
        athlete.setUsername("adal");
        athlete.setPassword("pwd");
        athlete.setEmail("ada@mail.com");

        ChallengeRanking ranking = new ChallengeRanking();
        ranking.setAthlete(athlete);

        assertEquals("Ada Lovelace", ranking.getDisplayName());
    }

    @Test
    void shouldResolveDisplayNameWithUsernameFallback() {
        Athlete athlete = new Athlete();
        athlete.setUsername("runner42");
        athlete.setPassword("pwd");
        athlete.setEmail("runner@mail.com");

        ChallengeRanking ranking = new ChallengeRanking();
        ranking.setAthlete(athlete);

        assertEquals("runner42", ranking.getDisplayName());
    }

    @Test
    void shouldFormatScoreWithoutDecimalsWhenInteger() {
        ChallengeRanking ranking = new ChallengeRanking();
        ranking.setScore(10d);

        assertEquals("10", ranking.getFormattedScore());
    }

    @Test
    void shouldFormatScoreWithTwoDecimalsWhenNeeded() {
        ChallengeRanking ranking = new ChallengeRanking();
        ranking.setScore(10.126d);

        assertEquals("10,13", ranking.getFormattedScore());
    }
}
