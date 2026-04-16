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
 * Spring MVC controller for objective management.
 *
 * <p>Manages objective listing, creation, completion, and deletion
 * for the authenticated athlete.</p>
 *
 * @author SportTrack Team
 */
@Controller
public class ObjectiveController {

    /** Redirect constant for the login page. */
    private static final String REDIRECT_LOGIN = "redirect:/login";

    /** Redirect constant for the objectives list. */
    private static final String REDIRECT_OBJECTIVES = "redirect:/objectives";

    /** Session attribute key for the cached athlete. */
    private static final String ATHLETE_ATTRIBUTE = "athlete";

    /** Service for objective operations. */
    private final ObjectiveService objectiveService;

    /** Repository for sport lookups. */
    private final SportRepository sportRepository;

    /** Repository for athlete authentication resolution. */
    private final AthleteRepository athleteRepository;

    /** Service for sport lookups. */
    private final SportService sportService;

    /**
     * Constructs an {@code ObjectiveController} with the required dependencies.
     *
     * @param objectiveService  the objective service
     * @param sportRepository   the sport repository
     * @param athleteRepository the athlete repository
     * @param sportService      the sport service
     */
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

    /**
     * Deletes the objective with the given identifier.
     *
     * @param id the objective identifier to delete
     * @return a redirect to the objectives list
     */
    @PostMapping("/objectives/delete/{id}")
    public String deleteObjective(@PathVariable("id") int id) {
        objectiveService.deleteById(id);
        return REDIRECT_OBJECTIVES;
    }

    /**
     * Marks the specified objective as completed for the authenticated athlete.
     *
     * @param id                  the objective identifier
     * @param session             the HTTP session for athlete resolution
     * @param redirectAttributes  flash attributes for success/error messaging
     * @return a redirect to the objectives list
     */
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

    /**
     * Displays the form for creating a new objective.
     *
     * @param session the HTTP session for athlete resolution
     * @param model   the Spring MVC model
     * @return the view name "objective/objective_form", or a redirect to login
     */
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
