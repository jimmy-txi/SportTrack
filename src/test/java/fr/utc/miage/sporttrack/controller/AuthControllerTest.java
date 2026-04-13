package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AthleteService athleteService;

    @Mock
    private Model model;

    @InjectMocks
    private AuthController controller;

    @Test
    void testLogin() {
        assertEquals("login", controller.login());
    }

    @Test
    void testShowRegistrationForm() {
        String viewName = controller.showRegistrationForm(model);
        
        assertEquals("register", viewName);
        verify(model).addAttribute(eq("athlete"), any(Athlete.class));
    }

    @Test
    void testRegisterUserPasswordMismatch() {
        Athlete athlete = new Athlete();
        athlete.setPassword("1234");

        String viewName = controller.registerUser(athlete, "wrongPassword", model);

        assertEquals("register", viewName);
        verify(model).addAttribute("error", "Passwords do not match");
        verifyNoInteractions(athleteService);
    }

    @Test
    void testRegisterUserSuccess() {
        Athlete athlete = new Athlete();
        athlete.setPassword("1234");

        String viewName = controller.registerUser(athlete, "1234", model);

        assertEquals("redirect:/login?registered", viewName);
        verify(athleteService).createProfile(athlete);
    }

    @Test
    void testRegisterUserEmailAlreadyUsed() {
        Athlete athlete = new Athlete();
        athlete.setPassword("1234");
        
        doThrow(new IllegalArgumentException("Email is already used")).when(athleteService).createProfile(athlete);

        String viewName = controller.registerUser(athlete, "1234", model);

        assertEquals("register", viewName);
        verify(model).addAttribute("error", "Email is already used");
    }
}
