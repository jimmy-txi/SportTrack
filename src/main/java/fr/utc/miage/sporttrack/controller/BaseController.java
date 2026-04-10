package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.User.Athlete;
import fr.utc.miage.sporttrack.entity.Enumeration.Gender;
import fr.utc.miage.sporttrack.entity.Enumeration.PracticeLevel;
import fr.utc.miage.sporttrack.service.User.AthleteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class BaseController {

    private final AthleteService athleteService;

    public BaseController(AthleteService athleteService) {
        this.athleteService = athleteService;
    }

    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        return "Hello, World!";
    }

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            String email = authentication.getName();
            try {
                Athlete currentAthlete = athleteService.getCurrentAthlete(email);
                model.addAttribute("athlete", currentAthlete);
                model.addAttribute("genders", Gender.values());
                model.addAttribute("practiceLevels", PracticeLevel.values());
            } catch (Exception e) {
                // If the user profile isn't found (could be Admin)
            }
        }
        return "index";
    }
}