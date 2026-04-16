package fr.utc.miage.sporttrack.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.service.event.BadgeService;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Spring MVC controller for athlete-related pages.
 *
 * <p>Provides the athlete list view with optional search and badge display.</p>
 *
 * @author SportTrack Team
 */
@Controller
@RequestMapping("/athlete")
public class AthleteController {

    /** Service for athlete queries. */
    private final AthleteService athleteService;

    /** Service for badge queries. */
    private final BadgeService badgeService;

    /**
     * Constructs an {@code AthleteController} with the required services.
     *
     * @param athleteService the athlete service
     * @param badgeService   the badge service
     */
    public AthleteController(AthleteService athleteService, BadgeService badgeService) {
        this.athleteService = athleteService;
        this.badgeService = badgeService;
    }

    /**
     * Displays the list of all athletes, optionally filtered by a search query.
     * Also includes the three most recent badges per athlete for display.
     *
     * @param query          the optional search keyword for filtering by username
     * @param model          the Spring MVC model
     * @param authentication the current security authentication
     * @return the view name "athlete/list"
     */
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
        List<Athlete> athletes;
        if (query != null && !query.isEmpty()) {
            model.addAttribute("query", query);
            athletes = athleteService.searchAthletesByName(query);
        } else {
            athletes = athleteService.getAllAthletes();
        }
        model.addAttribute("athletes", athletes);

        // Build a map of athleteId -> recent badges (last 3) for display in the list
        Map<Integer, List<?>> athleteBadges = new HashMap<>();
        for (Athlete a : athletes) {
            athleteBadges.put(a.getId(), badgeService.getRecentBadges(a.getId(), 3));
        }
        model.addAttribute("athleteBadges", athleteBadges);

        return "athlete/list";
    }

}
