package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.dto.AthleteProfileUpdateDTO;
import fr.utc.miage.sporttrack.entity.enumeration.Gender;
import fr.utc.miage.sporttrack.entity.enumeration.PracticeLevel;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Spring MVC controller for the current athlete's profile management.
 *
 * <p>Provides endpoints for viewing and updating the authenticated athlete's profile.</p>
 *
 * @author SportTrack Team
 */
@Controller
@RequestMapping("/")
public class UserController {

    /** Service for athlete profile operations. */
    private final AthleteService athleteService;

    /**
     * Constructs a {@code UserController} with the required service.
     *
     * @param athleteService the athlete service
     */
    public UserController(AthleteService athleteService) {
        this.athleteService = athleteService;
    }

    /**
     * Displays the profile page of the currently authenticated athlete.
     *
     * @param authentication the current security authentication
     * @param model          the Spring MVC model
     * @return the view name "athlete/profile", or a redirect to login if unauthenticated
     */
    @GetMapping("/profile")
    public String showProfile(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/login";
        }

        try {
            Athlete athlete = athleteService.getCurrentAthlete(authentication.getName());
            model.addAttribute("athlete", athlete);
            model.addAttribute("genders", Gender.values());
            model.addAttribute("practiceLevels", PracticeLevel.values());
            return "athlete/profile";
        } catch (Exception e) {
            return "redirect:/";
        }
    }

    /**
     * Processes the profile update form submission.
     *
     * @param updatedData    the DTO containing the updated profile fields
     * @param authentication the current security authentication
     * @return a redirect to the profile page with a success or error parameter
     */
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute AthleteProfileUpdateDTO updatedData, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            try {
                athleteService.updateProfile(email, updatedData);
                return "redirect:/profile?updated=true";
            } catch (Exception e) {
                return "redirect:/profile?error=true";
            }
        }
        return "redirect:/login";
    }
}
