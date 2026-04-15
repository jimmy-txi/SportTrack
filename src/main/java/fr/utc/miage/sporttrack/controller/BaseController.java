package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.user.Admin;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.enumeration.Gender;
import fr.utc.miage.sporttrack.entity.enumeration.PracticeLevel;
import fr.utc.miage.sporttrack.entity.user.User;
import fr.utc.miage.sporttrack.service.event.BadgeService;
import fr.utc.miage.sporttrack.service.user.AdminService;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import fr.utc.miage.sporttrack.service.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class BaseController {

    private final AthleteService athleteService;
    private final AdminService adminService;
    private final BadgeService badgeService;

    public BaseController(AthleteService athleteService, AdminService adminService, BadgeService badgeService) {
        this.athleteService = athleteService;
        this.adminService = adminService;
        this.badgeService = badgeService;
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
                model.addAttribute("earnedBadges", badgeService.getEarnedBadges(currentAthlete.getId()));
            } catch (Exception e) {
                try {
                    Admin currentAdmin = adminService.findByEmail(email);
                    return "redirect:/admin";
                } catch (Exception e2) {

                }
            }
        }
        return "index";
    }
}