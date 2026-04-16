package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.enumeration.Gender;
import fr.utc.miage.sporttrack.entity.enumeration.PracticeLevel;
import fr.utc.miage.sporttrack.service.event.BadgeService;
import fr.utc.miage.sporttrack.service.activity.ActivityService;
import fr.utc.miage.sporttrack.service.user.communication.FriendshipService;
import fr.utc.miage.sporttrack.service.user.AdminService;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Spring MVC controller that handles the application's home page.
 *
 * <p>Renders the index view with athlete profile data, earned badges,
 * and the latest activities from the authenticated athlete's friends.
 * Unauthenticated or unrecognised users see the public landing page.</p>
 *
 * @author SportTrack Team
 */
@Controller
@RequestMapping("/")
public class BaseController {

    /** Service for athlete data access. */
    private final AthleteService athleteService;

    /** Service for admin authentication checks. */
    private final AdminService adminService;

    /** Service for badge queries. */
    private final BadgeService badgeService;

    /** Service for friendship queries. */
    private final FriendshipService friendshipService;

    /** Service for activity queries. */
    private final ActivityService activityService;

    /**
     * Constructs a {@code BaseController} with the required services.
     *
     * @param athleteService   the athlete service
     * @param adminService     the admin service
     * @param badgeService     the badge service
     * @param friendshipService the friendship service
     * @param activityService  the activity service
     */
    public BaseController(AthleteService athleteService,
                          AdminService adminService, BadgeService badgeService,
                          FriendshipService friendshipService,
                          ActivityService activityService) {
        this.athleteService = athleteService;
        this.adminService = adminService;
        this.badgeService = badgeService;
        this.friendshipService = friendshipService;
        this.activityService = activityService;
    }

    /**
     * Renders the home page. If the current user is an authenticated athlete,
     * populates the model with profile data, badges, and friends' latest activities.
     * If the user is an admin, redirects to the admin dashboard.
     *
     * @param model          the Spring MVC model for view rendering
     * @param authentication the current security authentication, may be {@code null}
     * @return the view name "index" or a redirect to "/admin"
     */
    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            String email = authentication.getName();
            try {
                Athlete currentAthlete = athleteService.getCurrentAthlete(email);
                model.addAttribute("athlete", currentAthlete);
                model.addAttribute("genders", Gender.values());
                model.addAttribute("practiceLevels", PracticeLevel.values());
                model.addAttribute("earnedBadges", badgeService.getEarnedBadges(currentAthlete.getId()));
                List<Integer> friendIds = friendshipService.getFriendsOfAthlete(currentAthlete.getId())
                        .stream()
                        .map(Athlete::getId)
                        .toList();

                List<Activity> latestFriendsActivities = activityService.findAllByAthleteIds(friendIds)
                        .stream()
                        .limit(9)
                        .toList();

                model.addAttribute("latestFriendsActivities", latestFriendsActivities);
            } catch (Exception e) {
                try {
                    adminService.findByEmail(email);
                    return "redirect:/admin";
                } catch (Exception e2) {
                    // Not an admin either: keep rendering the public home page.
                }
            }
        }
        return "index";
    }
}