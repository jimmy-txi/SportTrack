package fr.utc.miage.sporttrack.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/athlete")
public class AthleteController {

    private final AthleteService athleteService;

    public AthleteController(AthleteService athleteService) {
        this.athleteService = athleteService;
    }

    @GetMapping("/list")
    public String listAthletes(@RequestParam(name = "q", required = false) String query, Model model, Authentication authentication) {
        // Load current athlete for header
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            try {
                Athlete currentAthlete = athleteService.getCurrentAthlete(authentication.getName());
                model.addAttribute("athlete", currentAthlete);
            } catch (Exception e) {
                // User not found as athlete, continue without athlete data
            }
        }
        
        if (query != null && !query.isEmpty()) {
            model.addAttribute("query", query);
            model.addAttribute("athletes", athleteService.searchAthletesByName(query));
        } else {
            model.addAttribute("athletes", athleteService.getAllAthletes());
        }   
        return "athlete/list";
    }

}
