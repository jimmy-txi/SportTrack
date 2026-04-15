package fr.utc.miage.sporttrack.entity.event;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BadgeTest {

    private Badge badge;

    @BeforeEach
    void setUp() {
        badge = new Badge();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(badge);
        assertEquals(0, badge.getId());
        assertNull(badge.getLabel());
        assertNull(badge.getDescription());
        assertNull(badge.getSport());
        assertNull(badge.getMetric());
        assertEquals(0.0, badge.getThreshold());
        assertNull(badge.getIcon());
        assertNull(badge.getEarnedBy());
    }

    @Test
    void testIdGetterSetter() {
        badge.setId(42);
        assertEquals(42, badge.getId());
    }

    @Test
    void testLabelGetterSetter() {
        badge.setLabel("Courreur 50km");
        assertEquals("Courreur 50km", badge.getLabel());
    }

    @Test
    void testDescriptionGetterSetter() {
        badge.setDescription("Cumulez 50 km de course à pied");
        assertEquals("Cumulez 50 km de course à pied", badge.getDescription());
    }

    @Test
    void testSportGetterSetter() {
        Sport sport = new Sport();
        sport.setId(1);
        sport.setName("Course");
        badge.setSport(sport);
        assertNotNull(badge.getSport());
        assertEquals(1, badge.getSport().getId());
        assertEquals("Course", badge.getSport().getName());
    }

    @Test
    void testSportNull() {
        badge.setSport(null);
        assertNull(badge.getSport());
    }

    @Test
    void testMetricGetterSetter() {
        badge.setMetric(Metric.DISTANCE);
        assertEquals(Metric.DISTANCE, badge.getMetric());
    }

    @Test
    void testAllMetrics() {
        for (Metric metric : Metric.values()) {
            badge.setMetric(metric);
            assertEquals(metric, badge.getMetric());
        }
    }

    @Test
    void testThresholdGetterSetter() {
        badge.setThreshold(50.5);
        assertEquals(50.5, badge.getThreshold(), 0.001);
    }

    @Test
    void testThresholdZero() {
        badge.setThreshold(0);
        assertEquals(0.0, badge.getThreshold(), 0.001);
    }

    @Test
    void testIconGetterSetter() {
        badge.setIcon("bi-trophy");
        assertEquals("bi-trophy", badge.getIcon());
    }

    @Test
    void testEarnedByGetterSetter() {
        List<Athlete> athletes = new ArrayList<>();
        Athlete a = new Athlete();
        athletes.add(a);
        badge.setEarnedBy(athletes);
        assertNotNull(badge.getEarnedBy());
        assertEquals(1, badge.getEarnedBy().size());
    }

    @Test
    void testEarnedByNull() {
        badge.setEarnedBy(null);
        assertNull(badge.getEarnedBy());
    }

    @Test
    void testEarnedByEmptyList() {
        badge.setEarnedBy(new ArrayList<>());
        assertNotNull(badge.getEarnedBy());
        assertTrue(badge.getEarnedBy().isEmpty());
    }

    @Test
    void testFullBadgeSetup() {
        Sport sport = new Sport();
        sport.setId(1);
        sport.setName("Natation");

        badge.setId(10);
        badge.setLabel("Nageur 10km");
        badge.setDescription("Cumulez 10 km de natation");
        badge.setSport(sport);
        badge.setMetric(Metric.DISTANCE);
        badge.setThreshold(10.0);
        badge.setIcon("bi-water");
        badge.setEarnedBy(new ArrayList<>());

        assertEquals(10, badge.getId());
        assertEquals("Nageur 10km", badge.getLabel());
        assertEquals("Cumulez 10 km de natation", badge.getDescription());
        assertEquals("Natation", badge.getSport().getName());
        assertEquals(Metric.DISTANCE, badge.getMetric());
        assertEquals(10.0, badge.getThreshold(), 0.001);
        assertEquals("bi-water", badge.getIcon());
        assertTrue(badge.getEarnedBy().isEmpty());
    }
}