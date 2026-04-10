package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.dto.AthleteProfileUpdateDTO;
import fr.utc.miage.sporttrack.service.User.AthleteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class UserController {

    private final AthleteService athleteService;

    public UserController(AthleteService athleteService) {
        this.athleteService = athleteService;
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("athlete") AthleteProfileUpdateDTO updatedData, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            try {
                athleteService.updateProfile(email, updatedData);
                return "redirect:/?updated=true";
            } catch (Exception e) {
                return "redirect:/?error=true";
            }
        }
        return "redirect:/login";
    }
}
