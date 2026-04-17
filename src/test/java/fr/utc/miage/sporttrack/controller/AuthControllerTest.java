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
import static org.mockito.ArgumentMatchers.argThat;
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
        verify(model).addAttribute(eq("registerForm"), any(AthleteRegisterFormDTO.class));
    }

    @Test
    void testRegisterUserPasswordMismatch() {
        String viewName = controller.registerUser(
                null,
                "test@mail.com",
                "runner",
                "1234",
                "wrongPassword",
                model
        );

        assertEquals("register", viewName);
        verify(model).addAttribute("error", "Passwords do not match");
        verify(model).addAttribute(eq("registerForm"), any(AthleteRegisterFormDTO.class));
        verifyNoInteractions(athleteService);
    }

    @Test
    void testRegisterUserMissingConfirmPassword() {
        String viewName = controller.registerUser(
            null,
            "test@mail.com",
            "runner",
            "1234",
            null,
            model
        );

        assertEquals("register", viewName);
        verify(model).addAttribute("error", "Passwords do not match");
        verify(model).addAttribute(eq("registerForm"), any(AthleteRegisterFormDTO.class));
        verifyNoInteractions(athleteService);
    }

    @Test
    void testRegisterUserWithNullDto() {
        String viewName = controller.registerUser(
            null,
            "test@mail.com",
            "runner",
            null,
            "1234",
            model
        );

        assertEquals("register", viewName);
        verify(model).addAttribute("error", "Passwords do not match");
        verify(model).addAttribute(eq("registerForm"), any(AthleteRegisterFormDTO.class));
        verifyNoInteractions(athleteService);
    }

    @Test
    void testRegisterUserSuccess() {
        String viewName = controller.registerUser(
            null,
            "test@mail.com",
            "runner",
            "1234",
            "1234",
            model
        );

        assertEquals("redirect:/login?registered", viewName);
        verify(athleteService).createProfile(argThat(dto ->
            "test@mail.com".equals(dto.getEmail())
                && "runner".equals(dto.getUsername())
                && "1234".equals(dto.getPassword())
        ));
    }

    @Test
    void testRegisterUserEmailAlreadyUsed() {
        doThrow(new IllegalArgumentException("Email is already used"))
            .when(athleteService)
            .createProfile(any(AthleteRegisterFormDTO.class));

        String viewName = controller.registerUser(
            null,
            "test@mail.com",
            "runner",
            "1234",
            "1234",
            model
        );

        assertEquals("register", viewName);
        verify(model).addAttribute("error", "Email is already used");
        verify(model).addAttribute(eq("registerForm"), any(AthleteRegisterFormDTO.class));
    }
}
