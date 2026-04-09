package fr.utc.miage.sporttrack.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.activity.SportRepository;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.service.event.ObjectiveService;
import jakarta.servlet.http.HttpSession;

@Controller
public class objectiveController {
    
    @Autowired
    private ObjectiveService objectiveService;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private AthleteRepository athleteRepository;

    @GetMapping("/objectives")
    public String getObjectives(HttpSession session, Model model) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }

        model.addAttribute("objectives", objectiveService.getObjectivesByUser(athlete));
        return "/objective/objectives";
    }

    @PostMapping("/objectives")
    public String createObjective(
        HttpSession session,
        @RequestParam String name,
        @RequestParam String description,
        @RequestParam Integer sportId
    ) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }

        Optional<Sport> sportOptional = sportRepository.findById(sportId);
        if (sportOptional.isEmpty()) {
            return "redirect:/objectives/add";
        }

        Sport sport = sportOptional.get();
        Objective objective = new Objective(name, description);
        objectiveService.saveObjective(objective, athlete, sport);
        
        return "redirect:/objectives";
    }

    @GetMapping("/objectives/add")
    public String showObjectivesForm(HttpSession session, Model model) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }

        model.addAttribute("sports", sportRepository.findAll());
        return "/objective/objective_form";
    }

    private Athlete getAuthenticatedAthlete(HttpSession session) {
        Athlete athlete = (Athlete) session.getAttribute("athlete");
        if (athlete != null) {
            return athlete;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Optional<Athlete> athleteOptional = athleteRepository.findByEmail(authentication.getName());
        if (athleteOptional.isPresent()) {
            athlete = athleteOptional.get();
            session.setAttribute("athlete", athlete);
        }

        return athlete;
    }
}
