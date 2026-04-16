package fr.utc.miage.sporttrack.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AthleteRegisterFormDTOTest {

    @Test
    void shouldSetAndGetEmail() {
        AthleteRegisterFormDTO dto = new AthleteRegisterFormDTO();
        dto.setEmail("test@example.com");
        assertEquals("test@example.com", dto.getEmail());
    }

    @Test
    void shouldSetAndGetPassword() {
        AthleteRegisterFormDTO dto = new AthleteRegisterFormDTO();
        dto.setPassword("securePassword");
        assertEquals("securePassword", dto.getPassword());
    }

    @Test
    void shouldSetAndGetUsername() {
        AthleteRegisterFormDTO dto = new AthleteRegisterFormDTO();
        dto.setUsername("athlete123");
        assertEquals("athlete123", dto.getUsername());
    }

    @Test
    void shouldReturnNullForUninitializedFields() {
        AthleteRegisterFormDTO dto = new AthleteRegisterFormDTO();
        assertNull(dto.getEmail());
        assertNull(dto.getPassword());
        assertNull(dto.getUsername());
    }
}
