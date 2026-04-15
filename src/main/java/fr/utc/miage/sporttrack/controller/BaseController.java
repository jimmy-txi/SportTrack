package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.enumeration.Gender;
import fr.utc.miage.sporttrack.entity.enumeration.PracticeLevel;
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

@Controller
@RequestMapping("/")
public class BaseController {

    private final AthleteService athleteService;
    private final AdminService adminService;
    private final FriendshipService friendshipService;
    private final ActivityService activityService;

    public BaseController(AthleteService athleteService,
                          AdminService adminService,
                          FriendshipService friendshipService,
                          ActivityService activityService) {
        this.athleteService = athleteService;
        this.adminService = adminService;
        this.friendshipService = friendshipService;
        this.activityService = activityService;
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