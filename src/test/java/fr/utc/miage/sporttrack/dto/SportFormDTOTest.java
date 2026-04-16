package fr.utc.miage.sporttrack.dto;

import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SportFormDTOTest {

    @Test
    void shouldSetAndGetId() {
        SportFormDTO dto = new SportFormDTO();
        dto.setId(42);
        assertEquals(42, dto.getId());
    }

    @Test
    void shouldHaveZeroAsDefaultId() {
        SportFormDTO dto = new SportFormDTO();
        assertEquals(0, dto.getId());
    }

    @Test
    void shouldSetAndGetName() {
        SportFormDTO dto = new SportFormDTO();
        dto.setName("Course à pied");
        assertEquals("Course à pied", dto.getName());
    }

    @Test
    void shouldSetAndGetDescription() {
        SportFormDTO dto = new SportFormDTO();
        dto.setDescription("Sport d'endurance");
        assertEquals("Sport d'endurance", dto.getDescription());
    }

    @Test
    void shouldSetAndGetCaloriesPerHour() {
        SportFormDTO dto = new SportFormDTO();
        dto.setCaloriesPerHour(500.0);
        assertEquals(500.0, dto.getCaloriesPerHour());
    }

    @Test
    void shouldSetAndGetType() {
        SportFormDTO dto = new SportFormDTO();
        dto.setType(SportType.DURATION);
        assertEquals(SportType.DURATION, dto.getType());
    }

    @Test
    void shouldReturnNullForUninitializedStringFields() {
        SportFormDTO dto = new SportFormDTO();
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getType());
    }
}
