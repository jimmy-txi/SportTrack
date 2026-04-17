package fr.utc.miage.sporttrack.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.entity.event.Challenge;
import fr.utc.miage.sporttrack.entity.event.ChallengeRanking;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.activity.ActivityRepository;
import fr.utc.miage.sporttrack.repository.event.ChallengeRepository;

@ExtendWith(MockitoExtension.class)
class ChallengeRankingServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ChallengeRankingService challengeRankingService;

    @Test
    void shouldComputeAndPersistDistanceRanking() throws Exception {
        Athlete a1 = buildAthlete(1, "alice");
        Athlete a2 = buildAthlete(2, "bob");

        Challenge challenge = buildChallenge(10, Metric.DISTANCE, 5, List.of(a1, a2));

        Activity activity1 = buildActivity(a1, 5, LocalDate.of(2026, 4, 10), 1d, 12d, 0);
        Activity activity2 = buildActivity(a2, 5, LocalDate.of(2026, 4, 11), 1d, 6d, 0);

        when(activityRepository.findActivitiesForChallengeRanking(
                List.of(1, 2), 5, challenge.getStartDate(), challenge.getEndDate()))
                .thenReturn(List.of(activity1, activity2));
        when(challengeRepository.save(any(Challenge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        challengeRankingService.recomputeRanking(challenge);

        ArgumentCaptor<Challenge> challengeCaptor = ArgumentCaptor.forClass(Challenge.class);
        verify(challengeRepository).save(challengeCaptor.capture());

        List<ChallengeRanking> rankings = challengeCaptor.getValue().getRankings();
        assertEquals(2, rankings.size());
        assertEquals(1, rankings.get(0).getRankPosition());
        assertEquals("alice", rankings.get(0).getDisplayName());
        assertEquals(12d, rankings.get(0).getScore());
        assertEquals(2, rankings.get(1).getRankPosition());
        assertEquals("bob", rankings.get(1).getDisplayName());
        assertEquals(6d, rankings.get(1).getScore());
    }

    @Test
    void shouldComputeRepsPerMinuteMetric() throws Exception {
        Athlete a1 = buildAthlete(3, "charlie");

        Challenge challenge = buildChallenge(11, Metric.REPS_PER_MINUTE, 7, List.of(a1));

        Activity activity1 = buildActivity(a1, 7, LocalDate.of(2026, 4, 13), 1d, 0d, 120);
        Activity activity2 = buildActivity(a1, 7, LocalDate.of(2026, 4, 14), 1d, 0d, 60);

        when(activityRepository.findActivitiesForChallengeRanking(
                List.of(3), 7, challenge.getStartDate(), challenge.getEndDate()))
                .thenReturn(List.of(activity1, activity2));
        when(challengeRepository.save(any(Challenge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        challengeRankingService.recomputeRanking(challenge);

        assertEquals(1, challenge.getRankings().size());
        assertEquals(1.5d, challenge.getRankings().get(0).getScore());
    }

    @Test
    void shouldRecomputeOnlyImpactedChallenges() throws Exception {
        Challenge impacted = buildChallenge(20, Metric.DURATION, 9, List.of(buildAthlete(4, "david")));

        when(challengeRepository.findChallengesImpactedByActivity(4, 9, LocalDate.of(2026, 4, 15)))
                .thenReturn(List.of(impacted));
        when(activityRepository.findActivitiesForChallengeRanking(
                List.of(4), 9, impacted.getStartDate(), impacted.getEndDate()))
                .thenReturn(List.of());
        when(challengeRepository.save(any(Challenge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        challengeRankingService.recomputeRankingsForActivity(4, 9, LocalDate.of(2026, 4, 15));

        verify(challengeRepository).save(impacted);
    }

    @Test
    void shouldComputeDurationMetric() throws Exception {
        Athlete a1 = buildAthlete(5, "dora");
        Challenge challenge = buildChallenge(30, Metric.DURATION, 4, List.of(a1));

        Activity activity1 = buildActivity(a1, 4, LocalDate.of(2026, 4, 8), 1.5d, 0d, 0);
        Activity activity2 = buildActivity(a1, 4, LocalDate.of(2026, 4, 9), 0.5d, 0d, 0);

        when(activityRepository.findActivitiesForChallengeRanking(
                List.of(5), 4, challenge.getStartDate(), challenge.getEndDate()))
                .thenReturn(List.of(activity1, activity2));
        when(challengeRepository.save(any(Challenge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        challengeRankingService.recomputeRanking(challenge);

        assertEquals(2.0d, challenge.getRankings().get(0).getScore());
    }

    @Test
    void shouldComputeRepetitionMetric() throws Exception {
        Athlete a1 = buildAthlete(6, "eric");
        Challenge challenge = buildChallenge(31, Metric.REPETITION, 12, List.of(a1));

        Activity activity1 = buildActivity(a1, 12, LocalDate.of(2026, 4, 18), 1d, 0d, 40);
        Activity activity2 = buildActivity(a1, 12, LocalDate.of(2026, 4, 19), 1d, 0d, 35);

        when(activityRepository.findActivitiesForChallengeRanking(
                List.of(6), 12, challenge.getStartDate(), challenge.getEndDate()))
                .thenReturn(List.of(activity1, activity2));
        when(challengeRepository.save(any(Challenge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        challengeRankingService.recomputeRanking(challenge);

        assertEquals(75d, challenge.getRankings().get(0).getScore());
    }

    @Test
    void shouldComputeMeanVelocityMetric() throws Exception {
        Athlete a1 = buildAthlete(7, "fiona");
        Challenge challenge = buildChallenge(32, Metric.MEAN_VELOCITY, 13, List.of(a1));

        Activity activity1 = buildActivity(a1, 13, LocalDate.of(2026, 4, 20), 1d, 8d, 0);
        Activity activity2 = buildActivity(a1, 13, LocalDate.of(2026, 4, 21), 3d, 16d, 0);

        when(activityRepository.findActivitiesForChallengeRanking(
                List.of(7), 13, challenge.getStartDate(), challenge.getEndDate()))
                .thenReturn(List.of(activity1, activity2));
        when(challengeRepository.save(any(Challenge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        challengeRankingService.recomputeRanking(challenge);

        assertEquals(6d, challenge.getRankings().get(0).getScore());
    }

    @Test
    void shouldComputeCountMetric() throws Exception {
        Athlete a1 = buildAthlete(8, "grace");
        Athlete a2 = buildAthlete(9, "henry");
        Challenge challenge = buildChallenge(33, Metric.COUNT, 14, List.of(a1, a2));

        assertThrows(IllegalArgumentException.class, () -> challengeRankingService.recomputeRanking(challenge));
    }

    @Test
    void shouldThrowExceptionForCountMetric() throws Exception {
        Athlete a1 = buildAthlete(11, "jack");
        Challenge challenge = buildChallenge(35, Metric.COUNT, 16, List.of(a1));

        assertThrows(IllegalArgumentException.class, () -> challengeRankingService.recomputeRanking(challenge),
                "COUNT metric should not be allowed for challenges");
    }

    private Challenge buildChallenge(int id, Metric metric, int sportId, List<Athlete> participants) throws Exception {
        Challenge challenge = new Challenge();
        setPrivateField(Challenge.class, challenge, "id", id);
        challenge.setMetric(metric);
        challenge.setStartDate(LocalDate.of(2026, 4, 1));
        challenge.setEndDate(LocalDate.of(2026, 4, 30));
        challenge.setParticipants(participants);

        Sport sport = new Sport();
        sport.setId(sportId);
        challenge.setSport(sport);
        return challenge;
    }

    private Athlete buildAthlete(int id, String username) throws Exception {
        Athlete athlete = new Athlete();
        athlete.setUsername(username);
        athlete.setPassword("pwd");
        athlete.setEmail(username + "@mail.com");
        setPrivateField(athlete.getClass().getSuperclass(), athlete, "id", id);
        return athlete;
    }

    // ========== Branch Coverage: Edge Cases ==========

    @Test
    void testRecomputeRankingWithNullChallenge() {
        assertDoesNotThrow(() -> challengeRankingService.recomputeRanking(null));
        verify(challengeRepository, never()).save(any());
    }

    @Test
    void testRecomputeRankingWithZeroId() throws Exception {
        Challenge challenge = buildChallenge(0, Metric.DISTANCE, 5, List.of(buildAthlete(1, "test")));

        assertDoesNotThrow(() -> challengeRankingService.recomputeRanking(challenge));
        verify(challengeRepository, never()).save(any());
    }

    @Test
    void testRecomputeRankingWithNullParticipants() throws Exception {
        Challenge challenge = buildChallenge(10, Metric.DURATION, 5, List.of());
        challenge.setParticipants(null);

        assertDoesNotThrow(() -> challengeRankingService.recomputeRanking(challenge));
        verify(challengeRepository, never()).save(any());
    }

    @Test
    void testRecomputeRankingWithNullSport() throws Exception {
        Challenge challenge = buildChallenge(10, Metric.DISTANCE, 5, List.of(buildAthlete(1, "test")));
        challenge.setSport(null);

        assertDoesNotThrow(() -> challengeRankingService.recomputeRanking(challenge));
        verify(challengeRepository, never()).save(any());
    }

    @Test
    void testRecomputeRankingWithNullStartDate() throws Exception {
        Challenge challenge = buildChallenge(10, Metric.DISTANCE, 5, List.of(buildAthlete(1, "test")));
        challenge.setStartDate(null);

        assertDoesNotThrow(() -> challengeRankingService.recomputeRanking(challenge));
        verify(challengeRepository, never()).save(any());
    }

    @Test
    void testRecomputeRankingWithNullEndDate() throws Exception {
        Challenge challenge = buildChallenge(10, Metric.DISTANCE, 5, List.of(buildAthlete(1, "test")));
        challenge.setEndDate(null);

        assertDoesNotThrow(() -> challengeRankingService.recomputeRanking(challenge));
        verify(challengeRepository, never()).save(any());
    }

    @Test
    void testRecomputeRankingWithNullMetric() throws Exception {
        Challenge challenge = buildChallenge(10, Metric.DISTANCE, 5, List.of(buildAthlete(1, "test")));
        challenge.setMetric(null);

        assertDoesNotThrow(() -> challengeRankingService.recomputeRanking(challenge));
        verify(challengeRepository, never()).save(any());
    }

    @Test
    void testRecomputeRankingWithNullAthleteId() throws Exception {
        Athlete athlete = buildAthlete(1, "test");
        setPrivateField(athlete.getClass().getSuperclass(), athlete, "id", null);
        Challenge challenge = buildChallenge(10, Metric.DISTANCE, 5, List.of(athlete));

        when(activityRepository.findActivitiesForChallengeRanking(
                List.of(), 5, challenge.getStartDate(), challenge.getEndDate()))
                .thenReturn(List.of());
        when(challengeRepository.save(any(Challenge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> challengeRankingService.recomputeRanking(challenge));
        verify(challengeRepository, never()).save(any());
    }

    @Test
    void testRecomputeRankingWithNoParticipantActivities() throws Exception {
        Athlete a1 = buildAthlete(1, "alice");
        Challenge challenge = buildChallenge(10, Metric.DISTANCE, 5, List.of(a1));

        when(activityRepository.findActivitiesForChallengeRanking(
                List.of(1), 5, challenge.getStartDate(), challenge.getEndDate()))
                .thenReturn(List.of());
        when(challengeRepository.save(any(Challenge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        challengeRankingService.recomputeRanking(challenge);

        ArgumentCaptor<Challenge> challengeCaptor = ArgumentCaptor.forClass(Challenge.class);
        verify(challengeRepository).save(challengeCaptor.capture());
        assertEquals(1, challengeCaptor.getValue().getRankings().size());
        assertEquals(0d, challengeCaptor.getValue().getRankings().get(0).getScore());
    }

    @Test
    void testComputeScoreMeanVelocityWithMultipleActivitiesDifferentVelocities() throws Exception {
        Athlete a1 = buildAthlete(1, "alice");
        Challenge challenge = buildChallenge(10, Metric.MEAN_VELOCITY, 5, List.of(a1));

        Activity activity1 = buildActivity(a1, 5, LocalDate.of(2026, 4, 10), 1d, 10d, 0);
        Activity activity2 = buildActivity(a1, 5, LocalDate.of(2026, 4, 11), 2d, 30d, 0); // 15 km/h
        Activity activity3 = buildActivity(a1, 5, LocalDate.of(2026, 4, 12), 1d, 8d, 0); // 8 km/h

        when(activityRepository.findActivitiesForChallengeRanking(
                List.of(1), 5, challenge.getStartDate(), challenge.getEndDate()))
                .thenReturn(List.of(activity1, activity2, activity3));
        when(challengeRepository.save(any(Challenge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        challengeRankingService.recomputeRanking(challenge);

        // Average distance / total duration: (10 + 30 + 8) / (1 + 2 + 1) = 48 / 4 = 12 km/h
        assertEquals(1, challenge.getRankings().size());
        assertEquals(12d, challenge.getRankings().get(0).getScore());
    }

    @Test
    void testRecomputeRankingsForActivityWithNullAthleteId() {
        assertDoesNotThrow(() -> challengeRankingService.recomputeRankingsForActivity(null, 5, LocalDate.now()));
        verify(challengeRepository, never()).findChallengesImpactedByActivity(anyInt(), anyInt(), any());
    }

    @Test
    void testRecomputeRankingsForActivityWithNullSportId() {
        assertDoesNotThrow(() -> challengeRankingService.recomputeRankingsForActivity(1, null, LocalDate.now()));
        verify(challengeRepository, never()).findChallengesImpactedByActivity(anyInt(), anyInt(), any());
    }

    @Test
    void testRecomputeRankingsForActivityWithNullDate() {
        assertDoesNotThrow(() -> challengeRankingService.recomputeRankingsForActivity(1, 5, null));
        verify(challengeRepository, never()).findChallengesImpactedByActivity(anyInt(), anyInt(), any());
    }

    @Test
    void testRecomputeRankingByChallengeIdWithNull() {
        assertDoesNotThrow(() -> challengeRankingService.recomputeRankingByChallengeId(null));
        verify(challengeRepository, never()).findById(anyInt());
    }

    @Test
    void testRecomputeRankingByChallengeIdNotFound() {
        when(challengeRepository.findById(99)).thenReturn(java.util.Optional.empty());

        assertDoesNotThrow(() -> challengeRankingService.recomputeRankingByChallengeId(99));
        verify(challengeRepository, never()).save(any());
    }

    private Activity buildActivity(Athlete athlete, int sportId, LocalDate date, double duration, double distance,

            int repetitions) {
        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setDateA(date);
        activity.setDuration(duration);
        activity.setDistance(distance);
        activity.setRepetition(repetitions);

        Sport sport = new Sport();
        sport.setId(sportId);
        activity.setSportAndType(sport);
        return activity;
    }

    private void setPrivateField(Class<?> ownerClass, Object target, String fieldName, Object value) throws Exception {
        Field field = ownerClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
