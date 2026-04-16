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
        return "register";
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
    public String registerUser(@ModelAttribute("athlete") AthleteRegisterFormDTO athleteDto,
                               @RequestParam("confirmPassword") String confirmPassword,
                               Model model) {
        if (!athleteDto.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "register";
        }

        try {
            athleteService.createProfile(athleteDto);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}
