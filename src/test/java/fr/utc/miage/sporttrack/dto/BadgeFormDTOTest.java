package fr.utc.miage.sporttrack.dto;

import static org.junit.jupiter.api.Assertions.*;

import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BadgeFormDTOTest {

    private BadgeFormDTO dto;

    @BeforeEach
    void setUp() {
        dto = new BadgeFormDTO();
    }

    @Test
    void testDefaultValues() {
        assertNull(dto.getId());
        assertNull(dto.getLabel());
        assertNull(dto.getDescription());
        assertNull(dto.getSportId());
        assertNull(dto.getMetric());
        assertEquals(0.0, dto.getThreshold(), 0.001);
        assertNull(dto.getIcon());
    }

    @Test
    void testIdGetterSetter() {
        dto.setId(5);
        assertEquals(5, dto.getId());
    }

    @Test
    void testIdNull() {
        dto.setId(null);
        assertNull(dto.getId());
    }

    @Test
    void testLabelGetterSetter() {
        dto.setLabel("Courreur 50km");
        assertEquals("Courreur 50km", dto.getLabel());
    }

    @Test
    void testDescriptionGetterSetter() {
        dto.setDescription("Cumulez 50 km");
        assertEquals("Cumulez 50 km", dto.getDescription());
    }

    @Test
    void testSportIdGetterSetter() {
        dto.setSportId(3);
        assertEquals(3, dto.getSportId());
    }

    @Test
    void testSportIdNull() {
        dto.setSportId(null);
        assertNull(dto.getSportId());
    }

    @Test
    void testMetricGetterSetter() {
        dto.setMetric(Metric.DURATION);
        assertEquals(Metric.DURATION, dto.getMetric());
    }

    @Test
    void testAllMetrics() {
        for (Metric metric : Metric.values()) {
            dto.setMetric(metric);
            assertEquals(metric, dto.getMetric());
        }
    }

    @Test
    void testMetricNull() {
        dto.setMetric(null);
        assertNull(dto.getMetric());
    }

    @Test
    void testThresholdGetterSetter() {
        dto.setThreshold(100.5);
        assertEquals(100.5, dto.getThreshold(), 0.001);
    }

    @Test
    void testThresholdZero() {
        dto.setThreshold(0.0);
        assertEquals(0.0, dto.getThreshold(), 0.001);
    }

    @Test
    void testIconGetterSetter() {
        dto.setIcon("bi-star-fill");
        assertEquals("bi-star-fill", dto.getIcon());
    }

    @Test
    void testFullDTOSetup() {
        dto.setId(1);
        dto.setLabel("Cycliste 100km");
        dto.setDescription("Parcourez 100 km à vélo");
        dto.setSportId(2);
        dto.setMetric(Metric.DISTANCE);
        dto.setThreshold(100.0);
        dto.setIcon("bi-bicycle");

        assertEquals(1, dto.getId());
        assertEquals("Cycliste 100km", dto.getLabel());
        assertEquals("Parcourez 100 km à vélo", dto.getDescription());
        assertEquals(2, dto.getSportId());
        assertEquals(Metric.DISTANCE, dto.getMetric());
        assertEquals(100.0, dto.getThreshold(), 0.001);
        assertEquals("bi-bicycle", dto.getIcon());
    }
}