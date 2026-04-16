package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.dto.AthleteRegisterFormDTO;
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
        verify(model).addAttribute(eq("athlete"), any(AthleteRegisterFormDTO.class));
    }

    @Test
    void testRegisterUserPasswordMismatch() {
        AthleteRegisterFormDTO dto = new AthleteRegisterFormDTO();
        dto.setPassword("1234");

        String viewName = controller.registerUser(dto, "wrongPassword", model);

        assertEquals("register", viewName);
        verify(model).addAttribute("error", "Passwords do not match");
        verifyNoInteractions(athleteService);
    }

    @Test
    void testRegisterUserSuccess() {
        AthleteRegisterFormDTO dto = new AthleteRegisterFormDTO();
        dto.setPassword("1234");

        String viewName = controller.registerUser(dto, "1234", model);

        assertEquals("redirect:/login?registered", viewName);
        verify(athleteService).createProfile(dto);
    }

    @Test
    void testRegisterUserEmailAlreadyUsed() {
        AthleteRegisterFormDTO dto = new AthleteRegisterFormDTO();
        dto.setPassword("1234");

        doThrow(new IllegalArgumentException("Email is already used")).when(athleteService).createProfile(dto);

        String viewName = controller.registerUser(dto, "1234", model);

        assertEquals("register", viewName);
        verify(model).addAttribute("error", "Email is already used");
    }
}
