package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.User.Athlete;
import fr.utc.miage.sporttrack.service.User.AthleteService;
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
        Athlete updatedAthlete = new Athlete();
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("czy@test.com");

        String redirectUrl = controller.updateProfile(updatedAthlete, authentication);

        assertEquals("redirect:/?updated=true", redirectUrl);
        verify(athleteService).updateProfile("czy@test.com", updatedAthlete);
    }

    @Test
    void testUpdateProfileUnauthenticated() {
        String redirectUrl = controller.updateProfile(new Athlete(), null);

        assertEquals("redirect:/login", redirectUrl);
        verifyNoInteractions(athleteService);
    }
}
