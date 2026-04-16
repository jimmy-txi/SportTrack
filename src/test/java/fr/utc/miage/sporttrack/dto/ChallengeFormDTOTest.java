package fr.utc.miage.sporttrack.dto;

import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ChallengeFormDTOTest {

    @Test
    void shouldSetAndGetName() {
        ChallengeFormDTO dto = new ChallengeFormDTO();
        dto.setName("Summer Challenge");
        assertEquals("Summer Challenge", dto.getName());
    }

    @Test
    void shouldSetAndGetDescription() {
        ChallengeFormDTO dto = new ChallengeFormDTO();
        dto.setDescription("A summer running challenge");
        assertEquals("A summer running challenge", dto.getDescription());
    }

    @Test
    void shouldSetAndGetStartDate() {
        ChallengeFormDTO dto = new ChallengeFormDTO();
        LocalDate date = LocalDate.of(2026, 6, 1);
        dto.setStartDate(date);
        assertEquals(date, dto.getStartDate());
    }

    @Test
    void shouldSetAndGetEndDate() {
        ChallengeFormDTO dto = new ChallengeFormDTO();
        LocalDate date = LocalDate.of(2026, 6, 30);
        dto.setEndDate(date);
        assertEquals(date, dto.getEndDate());
    }

    @Test
    void shouldSetAndGetMetric() {
        ChallengeFormDTO dto = new ChallengeFormDTO();
        dto.setMetric(Metric.DISTANCE);
        assertEquals(Metric.DISTANCE, dto.getMetric());
    }

    @Test
    void shouldReturnNullForUninitializedFields() {
        ChallengeFormDTO dto = new ChallengeFormDTO();
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getStartDate());
        assertNull(dto.getEndDate());
        assertNull(dto.getMetric());
    }
}
