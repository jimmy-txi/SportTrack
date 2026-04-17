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

@Controller
@RequestMapping("/")
public class UserController {

    private final AthleteService athleteService;

    public UserController(AthleteService athleteService) {
        this.athleteService = athleteService;
    }

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
