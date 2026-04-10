package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.dto.AthleteProfileUpdateDTO;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private AthleteService athleteService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController controller;

    @Test
    void testUpdateProfileSuccess() {
        AthleteProfileUpdateDTO updatedAthlete = new AthleteProfileUpdateDTO();
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("czy@test.com");

        String redirectUrl = controller.updateProfile(updatedAthlete, authentication);

        assertEquals("redirect:/?updated=true", redirectUrl);
        verify(athleteService).updateProfile("czy@test.com", updatedAthlete);
    }

    @Test
    void testUpdateProfileUnauthenticated() {
        String redirectUrl = controller.updateProfile(new AthleteProfileUpdateDTO(), null);

        assertEquals("redirect:/login", redirectUrl);
        verifyNoInteractions(athleteService);
    }

    @Test
    void testUpdateProfileException() {
        AthleteProfileUpdateDTO updatedAthlete = new AthleteProfileUpdateDTO();
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("czy@test.com");
        
        doThrow(new RuntimeException("Simulated exception")).when(athleteService).updateProfile(anyString(), any(AthleteProfileUpdateDTO.class));

        String redirectUrl = controller.updateProfile(updatedAthlete, authentication);

        assertEquals("redirect:/?error=true", redirectUrl);
    }
}
