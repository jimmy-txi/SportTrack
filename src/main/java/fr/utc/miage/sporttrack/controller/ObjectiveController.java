package fr.utc.miage.sporttrack.controller;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.activity.SportRepository;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.service.activity.SportService;
import fr.utc.miage.sporttrack.service.event.ObjectiveService;
import jakarta.servlet.http.HttpSession;

/**
 * Controller for objective-related pages and actions.
 *
 * This controller manages objective listing, creation, and the objective form display.
 */
@Controller
public class ObjectiveController {

    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_OBJECTIVES = "redirect:/objectives";
    private static final String ATHLETE_ATTRIBUTE = "athlete";
    
    private final ObjectiveService objectiveService;

    private final SportRepository sportRepository;

    private final AthleteRepository athleteRepository;

    private final SportService sportService;

    public ObjectiveController(ObjectiveService objectiveService, SportRepository sportRepository, AthleteRepository athleteRepository, SportService sportService) {
        this.objectiveService = objectiveService;
        this.sportRepository = sportRepository;
        this.athleteRepository = athleteRepository;
        this.sportService = sportService;
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
            return REDIRECT_LOGIN;
        }

        model.addAttribute(ATHLETE_ATTRIBUTE, athlete);
        model.addAttribute("objectives", objectiveService.getObjectivesByUser(athlete));
        return "objective/objectives";
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
            return REDIRECT_LOGIN;
        }

        Optional<Sport> sportOptional = sportRepository.findById(sportId);
        if (sportOptional.isEmpty()) {
            return "redirect:/objectives/add";
        }

        Sport sport = sportOptional.get();
        Objective objective = new Objective(name, description);
        objectiveService.saveObjective(objective, athlete, sport);
        
        return REDIRECT_OBJECTIVES;
    }

    @PostMapping("/objectives/delete/{id}")
    public String deleteObjective(@PathVariable("id") int id) {
        objectiveService.deleteById(id);
        return REDIRECT_OBJECTIVES;
    }

    @PostMapping("/objectives/complete/{id}")
    public String completeObjective(@PathVariable("id") int id, HttpSession session, RedirectAttributes redirectAttributes) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return REDIRECT_LOGIN;
        }

        try {
            objectiveService.markAsCompleted(id, athlete);
            redirectAttributes.addFlashAttribute("success", "Objectif marqué comme atteint !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return REDIRECT_OBJECTIVES;
    }

    @GetMapping("/objectives/add")
    public String showObjectivesForm(HttpSession session, Model model) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return REDIRECT_LOGIN;
        }

        model.addAttribute(ATHLETE_ATTRIBUTE, athlete);
        model.addAttribute("sports", sportService.findAllActive());
        return "objective/objective_form";
    }

    /**
     * Returns the authenticated athlete from the session or the security context.
     *
     * @param session the current HTTP session
     * @return the authenticated athlete, or null if no valid athlete is authenticated
     */
    private Athlete getAuthenticatedAthlete(HttpSession session) {
        Athlete athlete = (Athlete) session.getAttribute(ATHLETE_ATTRIBUTE);
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
            session.setAttribute(ATHLETE_ATTRIBUTE, athlete);
        }

        return athlete;
    }
}
