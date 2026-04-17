package fr.utc.miage.sporttrack.entity.enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

class MetricTest {

    @Test
    void testGetDisplayValue() {
        assertEquals("Durée", Metric.DURATION.getDisplayValue());
        assertEquals("Répétition", Metric.REPETITION.getDisplayValue());
        assertEquals("Distance", Metric.DISTANCE.getDisplayValue());
        assertEquals("Vitesse moyenne", Metric.MEAN_VELOCITY.getDisplayValue());
        assertEquals("Répétitions par minute", Metric.REPS_PER_MINUTE.getDisplayValue());
        assertEquals("Nombre de jours", Metric.COUNT.getDisplayValue());
    }

    @Test
    void testValuesForChallenges() {
        Metric[] challengeMetrics = Metric.valuesForChallenges();

        // Should have 5 metrics (all except COUNT)
        assertEquals(5, challengeMetrics.length);

        List<Metric> challengeMetricsList = Arrays.asList(challengeMetrics);

        // Should contain these metrics
        assertEquals(true, challengeMetricsList.contains(Metric.DURATION));
        assertEquals(true, challengeMetricsList.contains(Metric.REPETITION));
        assertEquals(true, challengeMetricsList.contains(Metric.DISTANCE));
        assertEquals(true, challengeMetricsList.contains(Metric.MEAN_VELOCITY));
        assertEquals(true, challengeMetricsList.contains(Metric.REPS_PER_MINUTE));

        // Should NOT contain COUNT
        assertEquals(false, challengeMetricsList.contains(Metric.COUNT));
    }
}

