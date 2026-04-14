package fr.utc.miage.sporttrack.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import fr.utc.miage.sporttrack.entity.enumeration.FriendshipStatus;
import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.dto.RelationshipStatusDTO;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.user.communication.Friendship;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.repository.user.communication.FriendshipRepository;
import fr.utc.miage.sporttrack.service.activity.ActivityService;
import fr.utc.miage.sporttrack.service.activity.WeatherReportService;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import fr.utc.miage.sporttrack.service.user.communication.FriendshipService;
import jakarta.servlet.http.HttpSession;

/**
 * Controller for friendship-related pages and actions.
 * <p>
 * Manages friend listing, sending/accepting/rejecting friend requests,
 * removing friends, blocking/unblocking users, and viewing friend profiles.
 */
@Controller
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final FriendshipRepository friendshipRepository;
    private final AthleteRepository athleteRepository;
    private final AthleteService athleteService;
    private final ActivityService activityService;
    private final WeatherReportService weatherReportService;

    public FriendshipController(FriendshipService friendshipService, FriendshipRepository friendshipRepository, AthleteRepository athleteRepository, AthleteService athleteService, ActivityService activityService, WeatherReportService weatherReportService) {
        this.friendshipService = friendshipService;
        this.friendshipRepository = friendshipRepository;
        this.athleteRepository = athleteRepository;
        this.athleteService = athleteService;
        this.activityService = activityService;
        this.weatherReportService = weatherReportService;
    }

    /**
     * Shows the main friendship management page with 5 tabs.
     * Loads all data at once for all tabs.
     *
     * @param session the current HTTP session
     * @param query   optional search query for the "Add friend" tab
     * @param tab     optional tab identifier to activate
     * @param model   the model used by the view template
     * @return the friends view or a redirect to login
     */
    @GetMapping("/friends")
    public String friendsPage(HttpSession session, @RequestParam(name = "q", required = false) String query, @RequestParam(name = "tab", required = false) String tab, Model model) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }

        // Load data for all tabs
        List<Athlete> friends = friendshipService.getFriendsOfAthlete(athlete.getId());
        List<Friendship> requests = friendshipService.getPendingRequestsForAthlete(athlete.getId());
        List<Friendship> sentRequests = friendshipService.getSentPendingRequests(athlete.getId());
        List<Friendship> blockedUsers = friendshipService.getBlockedUsers(athlete.getId());

        // Search athletes for "Add friend" tab — using the filtered search
        List<Athlete> athletes = friendshipService.searchVisibleAthletes(athlete.getId(), query);
        if (query != null && !query.isEmpty()) {
            model.addAttribute("query", query);
        }

        // Compute relationship status for each athlete in search results
        Map<Integer, RelationshipStatusDTO> relationshipStatuses = new HashMap<>();
        for (Athlete a : athletes) {
            relationshipStatuses.put(a.getId(), friendshipService.getRelationshipStatus(athlete.getId(), a.getId()));
        }

        model.addAttribute("friends", friends);
        model.addAttribute("requests", requests);
        model.addAttribute("sentRequests", sentRequests);
        model.addAttribute("athletes", athletes);
        model.addAttribute("blockedUsers", blockedUsers);
        model.addAttribute("relationshipStatuses", relationshipStatuses);
        model.addAttribute("activeTab", tab != null ? tab : "friends");
        model.addAttribute("currentAthlete", athlete);

        return "athlete/friend/friends";
    }

    /**
     * Shows a friend's profile page with relationship status.
     *
     * @param id      the ID of the athlete to view
     * @param session the current HTTP session
     * @param model   the model used by the view template
     * @return the profile view or a redirect to login
     */
    @GetMapping("/friends/profile/{id}")
    public String friendProfile(@PathVariable("id") Integer id, HttpSession session, Model model) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }

        // Look up the target athlete
        Optional<Athlete> targetOpt = athleteRepository.findById(id);
        if (targetOpt.isEmpty()) {
            return "redirect:/friends";
        }
        Athlete target = targetOpt.get();

        // Compute relationship status using the service
        RelationshipStatusDTO relationshipStatus = friendshipService.getRelationshipStatus(athlete.getId(), target.getId());

        // Also get the raw friendship record for display
        Optional<Friendship> friendshipOpt = friendshipRepository.findBetweenAthletes(athlete, target);

        model.addAttribute("profileAthlete", target);
        model.addAttribute("relationshipStatus", relationshipStatus.name());
        model.addAttribute("friendship", friendshipOpt.orElse(null));
        model.addAttribute("currentAthlete", athlete);
        model.addAttribute("activities", loadVisibleActivities(target, relationshipStatus));

        return "athlete/friend/profile";
    }

    /**
     * Shows the aggregated activity feed for all accepted friends.
     */
    @GetMapping("/friends/activities")
    public String friendsActivities(HttpSession session, Model model) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }

        List<Athlete> friends = friendshipService.getFriendsOfAthlete(athlete.getId());
        List<Integer> friendIds = friends.stream().map(Athlete::getId).toList();
        List<Activity> activities = loadActivitiesForAthletes(friendIds);

        model.addAttribute("activities", activities);
        model.addAttribute("friends", friends);
        model.addAttribute("currentAthlete", athlete);

        return "athlete/friend/activities";
    }

    /**
     * Sends a friend request to another athlete.
     */
    @PostMapping("/friends/send")
    public String sendFriendRequest(HttpSession session, @RequestParam("recipientId") Integer recipientId, RedirectAttributes redirectAttributes) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }

        try {
            friendshipService.sendFriendRequest(athlete.getId(), recipientId);
            redirectAttributes.addFlashAttribute("success", "Demande d'ami envoyée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/friends?tab=add";
    }

    /**
     * Accepts a pending friend request.
     */
    @PostMapping("/friends/accept/{id}")
    public String acceptFriendRequest(@PathVariable("id") Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }

        try {
            friendshipService.acceptFriendRequest(id, athlete.getId());
            redirectAttributes.addFlashAttribute("success", "Demande d'ami acceptée !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/friends?tab=requests";
    }

    /**
     * Rejects a pending friend request.
     */
    @PostMapping("/friends/reject/{id}")
    public String rejectFriendRequest(@PathVariable("id") Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }

        try {
            friendshipService.rejectFriendRequest(id, athlete.getId());
            redirectAttributes.addFlashAttribute("success", "Demande d'ami refusée.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/friends?tab=requests";
    }

    /**
     * Removes an established friendship.
     */
    @PostMapping("/friends/remove/{id}")
    public String removeFriend(@PathVariable("id") Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }

        try {
            friendshipService.removeFriend(athlete.getId(), id);
            redirectAttributes.addFlashAttribute("success", "Ami supprimé avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/friends?tab=friends";
    }

    /**
     * Blocks another user.
     */
    @PostMapping("/friends/block/{id}")
    public String blockUser(@PathVariable("id") Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }

        try {
            friendshipService.blockUser(athlete.getId(), id);
            redirectAttributes.addFlashAttribute("success", "Utilisateur bloqué avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        // Redirect back to the referring page
        return "redirect:/friends?tab=blocked";
    }

    /**
     * Unblocks a previously blocked user.
     */
    @PostMapping("/friends/unblock/{id}")
    public String unblockUser(@PathVariable("id") Integer id, HttpSession session, RedirectAttributes redirectAttributes) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }

        try {
            friendshipService.unblockUser(athlete.getId(), id);
            redirectAttributes.addFlashAttribute("success", "Utilisateur débloqué avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/friends?tab=blocked";
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

    private List<Activity> loadVisibleActivities(Athlete target, RelationshipStatusDTO relationshipStatus) {
        if (relationshipStatus != RelationshipStatusDTO.FRIENDS
                && relationshipStatus != RelationshipStatusDTO.SELF) {
            return List.of();
        }

        return loadActivitiesForAthletes(List.of(target.getId()));
    }

    private List<Activity> loadActivitiesForAthletes(List<Integer> athleteIds) {
        List<Activity> activities = activityService.findAllByAthleteIds(athleteIds);
        activities.forEach(activity -> activity.setWeatherReport(
                weatherReportService.findByActivityId(activity.getId()).orElse(null)
        ));
        return activities;
    }
}
