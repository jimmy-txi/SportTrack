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

/**
 * Controller for objective-related pages and actions.
 *
 * This controller manages objective listing, creation, and the objective form display.
 */
@Controller
public class objectiveController {
    
    private final ObjectiveService objectiveService;

    private final SportRepository sportRepository;

    private final AthleteRepository athleteRepository;

    public objectiveController(ObjectiveService objectiveService, SportRepository sportRepository, AthleteRepository athleteRepository) {
        this.objectiveService = objectiveService;
        this.sportRepository = sportRepository;
        this.athleteRepository = athleteRepository;
    }

    /**
     * Shows the list of objectives for the authenticated athlete.
     *
     * @param session the current HTTP session
     * @param model the model used by the view template
     * @return the objectives view or a redirect to login
     */
    @GetMapping("/objectives")
    public String getObjectives(HttpSession session, Model model) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }

        model.addAttribute("objectives", objectiveService.getObjectivesByUser(athlete));
        return "/objective/objectives";
    }

    /**
     * Creates a new objective for the authenticated athlete.
     *
     * @param session the current HTTP session
     * @param name the objective title
     * @param description the objective description
     * @param sportId the selected sport identifier
     * @return a redirect to the objectives list, add form, or login page
     */
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

    /**
     * Displays the objective creation form for the authenticated athlete.
     *
     * @param session the current HTTP session
     * @param model the model used by the view template
     * @return the objective form view or a redirect to login
     */
    @GetMapping("/objectives/add")
    public String showObjectivesForm(HttpSession session, Model model) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }

        model.addAttribute("sports", sportRepository.findAll());
        return "/objective/objective_form";
    }

    /**
     * Returns the authenticated athlete from the session or the security context.
     *
     * @param session the current HTTP session
     * @return the authenticated athlete, or null if no valid athlete is authenticated
     */
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
