package fr.utc.miage.sporttrack.entity.enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MetricTest {

    @Test
    void testGetDisplayValue() {
        assertEquals("Durée", Metric.DURATION.getDisplayValue());
        assertEquals("Répétition", Metric.REPETITION.getDisplayValue());
        assertEquals("Distance", Metric.DISTANCE.getDisplayValue());
        assertEquals("Vitesse moyenne", Metric.MEAN_VELOCITY.getDisplayValue());
        assertEquals("Répétitions par minute", Metric.REPS_PER_MINUTE.getDisplayValue());
    }
}
