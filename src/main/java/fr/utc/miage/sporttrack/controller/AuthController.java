package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.dto.AthleteRegisterFormDTO;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Spring MVC controller that handles user authentication flows
 * including login and registration.
 *
 * @author SportTrack Team
 */
@Controller
@RequestMapping("/")
public class AuthController {

    /** Service for athlete registration. */
    private final AthleteService athleteService;
    private static final String REGISTER = "register";
    private static final String ERROR = "error";

    /**
     * Constructs an {@code AuthController} with the required service.
     *
     * @param athleteService the athlete service for profile creation
     */
    public AuthController(AthleteService athleteService) {
        this.athleteService = athleteService;
    }

    /**
     * Displays the login form.
     *
     * @return the view name "login"
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Displays the athlete registration form.
     *
     * @param model the Spring MVC model for view rendering
     * @return the view name "register"
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("athlete", new AthleteRegisterFormDTO());
        return REGISTER;
    }

    /**
     * Processes the registration form submission. Validates that the password
     * and confirmation match before delegating to the athlete service.
     *
     * @param athleteDto     the registration form data bound from the request
     * @param confirmPassword the confirmed password entered by the user
     * @param model          the Spring MVC model for error rendering
     * @return a redirect to the login page on success, or the register view on error
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute AthleteRegisterFormDTO athleteDto,
                               @RequestParam(value = "email", required = false) String email,
                               @RequestParam(value = "username", required = false) String username,
                               @RequestParam(value = "password", required = false) String password,
                               @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                               Model model) {
        if (athleteDto == null) {
            athleteDto = new AthleteRegisterFormDTO();
        }

        athleteDto.setEmail(email);
        athleteDto.setUsername(username);
        athleteDto.setPassword(password);

        if (password == null || confirmPassword == null) {
            model.addAttribute(ERROR, "Passwords do not match");
            model.addAttribute("athlete", athleteDto);
            return REGISTER;
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute(ERROR, "Passwords do not match");
            return REGISTER;
        }

        try {
            athleteService.createProfile(athleteDto);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute(ERROR, e.getMessage());
            return REGISTER;
        }
    }
}
