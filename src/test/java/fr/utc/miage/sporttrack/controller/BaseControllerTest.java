package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseControllerTest {

    @Mock
    private AthleteService athleteService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BaseController controller;

    @Test
    void testHomeWithAuthenticatedUser() {
        Athlete athlete = new Athlete();
        athlete.setEmail("czy@test.com");
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("czy@test.com");
        when(athleteService.getCurrentAthlete("czy@test.com")).thenReturn(athlete);

        String viewName = controller.home(model, authentication);

        assertEquals("index", viewName);
        verify(model).addAttribute("athlete", athlete);
        verify(model).addAttribute(eq("genders"), any());
        verify(model).addAttribute(eq("practiceLevels"), any());
    }

    @Test
    void testHomeWithoutAuthentication() {
        String viewName = controller.home(model, null);

        assertEquals("index", viewName);
        verifyNoInteractions(model);
        verifyNoInteractions(athleteService);
    }
}
