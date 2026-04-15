package fr.utc.miage.sporttrack.service.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.entity.event.Badge;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.activity.ActivityRepository;
import fr.utc.miage.sporttrack.repository.event.BadgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private BadgeService badgeService;

    private Sport sport;
    private Badge badge;
    private Athlete athlete;

    @BeforeEach
    void setUp() throws Exception {
        sport = new Sport();
        sport.setId(1);
        sport.setName("Course");

        badge = new Badge();
        badge.setId(1);
        badge.setLabel("Courreur 50km");
        badge.setMetric(Metric.DISTANCE);
        badge.setThreshold(50.0);
        badge.setIcon("bi-trophy");
        badge.setEarnedBy(new ArrayList<>());

        athlete = new Athlete();
        athlete.setEmail("test@mail.com");
        setField(athlete, "id", 1);
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ========== saveBadge ==========

    @Test
    void testSaveBadgeSuccess() {
        badgeService.saveBadge(badge, sport);

        assertEquals(sport, badge.getSport());
        assertNotNull(badge.getEarnedBy());
        verify(badgeRepository).save(badge);
    }

    @Test
    void testSaveBadgeInitializesEmptyEarnedBy() {
        badge.setEarnedBy(null);
        badgeService.saveBadge(badge, sport);

        assertNotNull(badge.getEarnedBy());
        assertTrue(badge.getEarnedBy().isEmpty());
        verify(badgeRepository).save(badge);
    }

    @Test
    void testSaveBadgePreservesExistingEarnedBy() {
        Athlete existingAthlete = new Athlete();
        setField(existingAthlete, "id", 2);
        badge.setEarnedBy(new ArrayList<>(List.of(existingAthlete)));

        badgeService.saveBadge(badge, sport);

        assertEquals(1, badge.getEarnedBy().size());
        verify(badgeRepository).save(badge);
    }

    @Test
    void testSaveBadgeNullBadgeThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> badgeService.saveBadge(null, sport));
        assertEquals("Badge and Sport are required", ex.getMessage());
        verify(badgeRepository, never()).save(any());
    }

    @Test
    void testSaveBadgeNullSportThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> badgeService.saveBadge(badge, null));
        assertEquals("Badge and Sport are required", ex.getMessage());
        verify(badgeRepository, never()).save(any());
    }

    @Test
    void testSaveBadgeBothNullThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> badgeService.saveBadge(null, null));
        verify(badgeRepository, never()).save(any());
    }

    // ========== findAll ==========

    @Test
    void testFindAllReturnsAllBadges() {
        List<Badge> badges = List.of(badge);
        when(badgeRepository.findAll()).thenReturn(badges);

        List<Badge> result = badgeService.findAll();

        assertEquals(1, result.size());
        assertEquals(badge, result.get(0));
    }

    @Test
    void testFindAllEmpty() {
        when(badgeRepository.findAll()).thenReturn(Collections.emptyList());

        List<Badge> result = badgeService.findAll();

        assertTrue(result.isEmpty());
    }

    // ========== findById ==========

    @Test
    void testFindByIdExists() {
        when(badgeRepository.findById(1)).thenReturn(Optional.of(badge));

        Badge result = badgeService.findById(1);

        assertEquals(badge, result);
    }

    @Test
    void testFindByIdNotExists() {
        when(badgeRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> badgeService.findById(99));
        assertTrue(ex.getMessage().contains("Badge not found"));
    }

    // ========== deleteById ==========

    @Test
    void testDeleteByIdExists() {
        when(badgeRepository.existsById(1)).thenReturn(true);

        badgeService.deleteById(1);

        verify(badgeRepository).deleteById(1);
    }

    @Test
    void testDeleteByIdNotExists() {
        when(badgeRepository.existsById(99)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> badgeService.deleteById(99));
        assertTrue(ex.getMessage().contains("Badge not found"));
        verify(badgeRepository, never()).deleteById(anyInt());
    }

    // ========== getEarnedBadges ==========

    @Test
    void testGetEarnedBadges() {
        when(badgeRepository.findByEarnedBy_Id(1)).thenReturn(List.of(badge));

        List<Badge> result = badgeService.getEarnedBadges(1);

        assertEquals(1, result.size());
        assertEquals(badge, result.get(0));
    }

    @Test
    void testGetEarnedBadgesEmpty() {
        when(badgeRepository.findByEarnedBy_Id(1)).thenReturn(Collections.emptyList());

        List<Badge> result = badgeService.getEarnedBadges(1);

        assertTrue(result.isEmpty());
    }

    // ========== getUnearnedBadges ==========

    @Test
    void testGetUnearnedBadgesWithEarned() {
        Badge unearned = new Badge();
        unearned.setId(2);
        unearned.setLabel("Unearned Badge");

        when(badgeRepository.findByEarnedBy_Id(1)).thenReturn(List.of(badge));
        when(badgeRepository.findByIdNotIn(List.of(1))).thenReturn(List.of(unearned));

        List<Badge> result = badgeService.getUnearnedBadges(1);

        assertEquals(1, result.size());
        assertEquals("Unearned Badge", result.get(0).getLabel());
    }

    @Test
    void testGetUnearnedBadgesNoneEarned() {
        when(badgeRepository.findByEarnedBy_Id(1)).thenReturn(Collections.emptyList());
        when(badgeRepository.findAll()).thenReturn(List.of(badge));

        List<Badge> result = badgeService.getUnearnedBadges(1);

        assertEquals(1, result.size());
    }

    // ========== getRecentBadges ==========

    @Test
    void testGetRecentBadgesFewerThanLimit() {
        when(badgeRepository.findByEarnedBy_Id(1)).thenReturn(List.of(badge));

        List<Badge> result = badgeService.getRecentBadges(1, 5);

        assertEquals(1, result.size());
    }

    @Test
    void testGetRecentBadgesMoreThanLimit() {
        Badge b1 = new Badge();
        b1.setId(1);
        Badge b2 = new Badge();
        b2.setId(2);
        Badge b3 = new Badge();
        b3.setId(3);
        Badge b4 = new Badge();
        b4.setId(4);

        when(badgeRepository.findByEarnedBy_Id(1)).thenReturn(List.of(b1, b2, b3, b4));

        List<Badge> result = badgeService.getRecentBadges(1, 2);

        assertEquals(2, result.size());
        assertEquals(3, result.get(0).getId());
        assertEquals(4, result.get(1).getId());
    }

    @Test
    void testGetRecentBadgesEqualToLimit() {
        Badge b1 = new Badge();
        b1.setId(1);
        Badge b2 = new Badge();
        b2.setId(2);

        when(badgeRepository.findByEarnedBy_Id(1)).thenReturn(List.of(b1, b2));

        List<Badge> result = badgeService.getRecentBadges(1, 2);

        assertEquals(2, result.size());
    }

    // ========== checkAndAwardBadges ==========

    @Test
    void testCheckAndAwardBadgesNullActivity() {
        assertDoesNotThrow(() -> badgeService.checkAndAwardBadges(null));
        verify(badgeRepository, never()).save(any());
    }

    @Test
    void testCheckAndAwardBadgesNullCreatedBy() {
        Activity activity = new Activity();
        activity.setCreatedBy(null);

        assertDoesNotThrow(() -> badgeService.checkAndAwardBadges(activity));
        verify(badgeRepository, never()).save(any());
    }

    @Test
    void testCheckAndAwardBadgesNullSport() {
        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(null);

        assertDoesNotThrow(() -> badgeService.checkAndAwardBadges(activity));
        verify(badgeRepository, never()).save(any());
    }

    @Test
    void testCheckAndAwardBadgesNoBadgesForSport() {
        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);

        when(badgeRepository.findBySportId(1)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> badgeService.checkAndAwardBadges(activity));
        verify(badgeRepository, never()).save(any());
    }

    @Test
    void testCheckAndAwardBadgesAwardsWhenThresholdMet() {
        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setDistance(60.0);

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity));

        badgeService.checkAndAwardBadges(activity);

        verify(badgeRepository).save(badge);
        assertEquals(1, badge.getEarnedBy().size());
        assertEquals(athlete, badge.getEarnedBy().get(0));
    }

    @Test
    void testCheckAndAwardBadgesSkipsWhenAlreadyEarned() {
        badge.setEarnedBy(new ArrayList<>(List.of(athlete)));

        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setDistance(100.0);

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity));

        badgeService.checkAndAwardBadges(activity);

        verify(badgeRepository, never()).save(badge);
    }

    @Test
    void testCheckAndAwardBadgesNoAwardWhenThresholdNotMet() {
        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setDistance(10.0);

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity));

        badgeService.checkAndAwardBadges(activity);

        verify(badgeRepository, never()).save(badge);
        assertTrue(badge.getEarnedBy().isEmpty());
    }

    @Test
    void testCheckAndAwardBadgesDurationMetric() {
        badge.setMetric(Metric.DURATION);
        badge.setThreshold(120.0);

        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setDuration(130.0);

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity));

        badgeService.checkAndAwardBadges(activity);

        verify(badgeRepository).save(badge);
        assertEquals(1, badge.getEarnedBy().size());
    }

    @Test
    void testCheckAndAwardBadgesRepetitionMetric() {
        badge.setMetric(Metric.REPETITION);
        badge.setThreshold(100.0);

        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setRepetition(120);

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity));

        badgeService.checkAndAwardBadges(activity);

        verify(badgeRepository).save(badge);
    }

    @Test
    void testCheckAndAwardBadgesNullDistance() {
        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setDistance(null);

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity));

        badgeService.checkAndAwardBadges(activity);

        verify(badgeRepository, never()).save(badge);
    }

    @Test
    void testCheckAndAwardBadgesMultipleActivities() {
        badge.setMetric(Metric.DISTANCE);
        badge.setThreshold(50.0);

        Activity a1 = new Activity();
        a1.setCreatedBy(athlete);
        a1.setSportAndType(sport);
        a1.setDistance(30.0);

        Activity a2 = new Activity();
        a2.setCreatedBy(athlete);
        a2.setSportAndType(sport);
        a2.setDistance(25.0);

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(a1, a2));

        badgeService.checkAndAwardBadges(a1);

        verify(badgeRepository).save(badge);
        assertEquals(1, badge.getEarnedBy().size());
    }

    @Test
    void testCheckAndAwardBadgesFiltersBySport() {
        Sport otherSport = new Sport();
        otherSport.setId(2);
        otherSport.setName("Natation");

        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setDistance(60.0);

        Activity otherActivity = new Activity();
        otherActivity.setCreatedBy(athlete);
        otherActivity.setSportAndType(otherSport);
        otherActivity.setDistance(100.0);

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity, otherActivity));

        badgeService.checkAndAwardBadges(activity);

        verify(badgeRepository).save(badge);
    }

    @Test
    void testCheckAndAwardBadgesNullEarnedByInitializes() {
        badge.setEarnedBy(null);

        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setDistance(60.0);

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity));

        badgeService.checkAndAwardBadges(activity);

        assertNotNull(badge.getEarnedBy());
        assertEquals(1, badge.getEarnedBy().size());
        verify(badgeRepository).save(badge);
    }

    @Test
    void testCheckAndAwardBadgesMeanVelocityMetric() {
        badge.setMetric(Metric.MEAN_VELOCITY);
        badge.setThreshold(10.0); // 10 km/h average

        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setDistance(20.0);  // 20 km
        activity.setDuration(60.0); // 60 min => 20 km/h

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity));

        badgeService.checkAndAwardBadges(activity);

        verify(badgeRepository).save(badge);
        assertEquals(1, badge.getEarnedBy().size());
    }

    @Test
    void testCheckAndAwardBadgesMeanVelocityNullDistance() {
        badge.setMetric(Metric.MEAN_VELOCITY);
        badge.setThreshold(10.0);

        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setDistance(null);
        activity.setDuration(60.0);

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity));

        badgeService.checkAndAwardBadges(activity);

        verify(badgeRepository, never()).save(badge);
    }

    @Test
    void testCheckAndAwardBadgesMeanVelocityZeroDuration() {
        badge.setMetric(Metric.MEAN_VELOCITY);
        badge.setThreshold(10.0);

        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setDistance(20.0);
        activity.setDuration(0.0); // zero duration

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity));

        badgeService.checkAndAwardBadges(activity);

        verify(badgeRepository, never()).save(badge);
    }

    @Test
    void testCheckAndAwardBadgesRepsPerMinuteMetric() {
        badge.setMetric(Metric.REPS_PER_MINUTE);
        badge.setThreshold(30.0); // 30 reps/min

        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setRepetition(60);
        activity.setDuration(60.0); // 60 reps / 60 min = 1 rep/min... not enough
        // Let's set threshold lower
        badge.setThreshold(0.5);

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity));

        badgeService.checkAndAwardBadges(activity);

        verify(badgeRepository).save(badge);
    }

    @Test
    void testCheckAndAwardBadgesRepsPerMinuteNullRepetition() {
        badge.setMetric(Metric.REPS_PER_MINUTE);
        badge.setThreshold(1.0);

        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setRepetition(null);
        activity.setDuration(60.0);

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity));

        badgeService.checkAndAwardBadges(activity);

        verify(badgeRepository, never()).save(badge);
    }

    @Test
    void testCheckAndAwardBadgesRepsPerMinuteZeroDuration() {
        badge.setMetric(Metric.REPS_PER_MINUTE);
        badge.setThreshold(1.0);

        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setRepetition(60);
        activity.setDuration(0.0);

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity));

        badgeService.checkAndAwardBadges(activity);

        verify(badgeRepository, never()).save(badge);
    }

    @Test
    void testCheckAndAwardBadgesFiltersActivitiesWithNullSport() {
        badge.setMetric(Metric.DISTANCE);
        badge.setThreshold(50.0);

        Activity validActivity = new Activity();
        validActivity.setCreatedBy(athlete);
        validActivity.setSportAndType(sport);
        validActivity.setDistance(60.0);

        Activity nullSportActivity = new Activity();
        nullSportActivity.setCreatedBy(athlete);
        nullSportActivity.setSportAndType(null); // null sport
        nullSportActivity.setDistance(100.0);

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(validActivity, nullSportActivity));

        badgeService.checkAndAwardBadges(validActivity);

        // Only the validActivity (60km) should be counted, nullSportActivity filtered out
        verify(badgeRepository).save(badge);
        assertEquals(1, badge.getEarnedBy().size());
    }

    @Test
    void testCheckAndAwardBadgesNullRepetitionMetric() {
        badge.setMetric(Metric.REPETITION);
        badge.setThreshold(50.0);

        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setRepetition(null); // null repetition

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity));

        badgeService.checkAndAwardBadges(activity);

        verify(badgeRepository, never()).save(badge);
    }

    @Test
    void testCheckAndAwardBadgesMeanVelocityNotMet() {
        badge.setMetric(Metric.MEAN_VELOCITY);
        badge.setThreshold(100.0); // 100 km/h - very high

        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setDistance(10.0);
        activity.setDuration(60.0); // 10 km/h, not enough

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity));

        badgeService.checkAndAwardBadges(activity);

        verify(badgeRepository, never()).save(badge);
    }

    @Test
    void testCheckAndAwardBadgesRepsPerMinuteNotMet() {
        badge.setMetric(Metric.REPS_PER_MINUTE);
        badge.setThreshold(100.0);

        Activity activity = new Activity();
        activity.setCreatedBy(athlete);
        activity.setSportAndType(sport);
        activity.setRepetition(30);
        activity.setDuration(60.0); // 0.5 reps/min

        when(badgeRepository.findBySportId(1)).thenReturn(List.of(badge));
        when(activityRepository.findByCreatedBy_IdOrderByDateADescStartTimeDesc(1))
                .thenReturn(List.of(activity));

        badgeService.checkAndAwardBadges(activity);

        verify(badgeRepository, never()).save(badge);
    }
}
