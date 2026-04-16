package fr.utc.miage.sporttrack.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ActivityFormDTOTest {

    @Test
    void shouldSetAndGetId() {
        ActivityFormDTO dto = new ActivityFormDTO();
        dto.setId(42);
        assertEquals(42, dto.getId());
    }

    @Test
    void shouldHaveNullAsDefaultId() {
        ActivityFormDTO dto = new ActivityFormDTO();
        assertNull(dto.getId());
    }

    @Test
    void shouldSetAndGetDuration() {
        ActivityFormDTO dto = new ActivityFormDTO();
        dto.setDuration(1.5);
        assertEquals(1.5, dto.getDuration());
    }

    @Test
    void shouldSetAndGetTitle() {
        ActivityFormDTO dto = new ActivityFormDTO();
        dto.setTitle("Morning Run");
        assertEquals("Morning Run", dto.getTitle());
    }

    @Test
    void shouldSetAndGetDescription() {
        ActivityFormDTO dto = new ActivityFormDTO();
        dto.setDescription("A nice morning jog in the park");
        assertEquals("A nice morning jog in the park", dto.getDescription());
    }

    @Test
    void shouldSetAndGetRepetition() {
        ActivityFormDTO dto = new ActivityFormDTO();
        dto.setRepetition(30);
        assertEquals(30, dto.getRepetition());
    }

    @Test
    void shouldSetAndGetDistance() {
        ActivityFormDTO dto = new ActivityFormDTO();
        dto.setDistance(5.5);
        assertEquals(5.5, dto.getDistance());
    }

    @Test
    void shouldSetAndGetDateA() {
        ActivityFormDTO dto = new ActivityFormDTO();
        LocalDate date = LocalDate.of(2026, 4, 16);
        dto.setDateA(date);
        assertEquals(date, dto.getDateA());
    }

    @Test
    void shouldSetAndGetStartTime() {
        ActivityFormDTO dto = new ActivityFormDTO();
        LocalTime time = LocalTime.of(7, 30);
        dto.setStartTime(time);
        assertEquals(time, dto.getStartTime());
    }

    @Test
    void shouldSetAndGetLocationCity() {
        ActivityFormDTO dto = new ActivityFormDTO();
        dto.setLocationCity("Paris");
        assertEquals("Paris", dto.getLocationCity());
    }

    @Test
    void shouldSetAndGetSportId() {
        ActivityFormDTO dto = new ActivityFormDTO();
        dto.setSportId(3);
        assertEquals(3, dto.getSportId());
    }

    @Test
    void shouldReturnNullForUninitializedReferenceFields() {
        ActivityFormDTO dto = new ActivityFormDTO();
        assertNull(dto.getTitle());
        assertNull(dto.getDescription());
        assertNull(dto.getRepetition());
        assertNull(dto.getDistance());
        assertNull(dto.getDateA());
        assertNull(dto.getStartTime());
        assertNull(dto.getLocationCity());
        assertNull(dto.getSportId());
    }

    @Test
    void shouldTrimTitleWhenSet() {
        ActivityFormDTO dto = new ActivityFormDTO();
        dto.setTitle("  Morning Run  ");
        assertEquals("Morning Run", dto.getTitle());
    }

    @Test
    void shouldHandleNullTitle() {
        ActivityFormDTO dto = new ActivityFormDTO();
        dto.setTitle(null);
        assertNull(dto.getTitle());
    }

    @Test
    void shouldTrimDescriptionWhenSet() {
        ActivityFormDTO dto = new ActivityFormDTO();
        dto.setDescription("  A nice jog  ");
        assertEquals("A nice jog", dto.getDescription());
    }

    @Test
    void shouldHandleNullDescription() {
        ActivityFormDTO dto = new ActivityFormDTO();
        dto.setDescription(null);
        assertNull(dto.getDescription());
    }

    @Test
    void shouldTrimLocationCityWhenSet() {
        ActivityFormDTO dto = new ActivityFormDTO();
        dto.setLocationCity("  Paris  ");
        assertEquals("Paris", dto.getLocationCity());
    }

    @Test
    void shouldHandleNullLocationCity() {
        ActivityFormDTO dto = new ActivityFormDTO();
        dto.setLocationCity(null);
        assertNull(dto.getLocationCity());
    }
}