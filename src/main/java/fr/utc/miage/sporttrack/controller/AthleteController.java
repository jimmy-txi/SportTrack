package fr.utc.miage.sporttrack.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.service.event.BadgeService;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/athlete")
public class AthleteController {

    private final AthleteService athleteService;
    private final BadgeService badgeService;

    public AthleteController(AthleteService athleteService, BadgeService badgeService) {
        this.athleteService = athleteService;
        this.badgeService = badgeService;
    }

    @GetMapping("/list")
    public String listAthletes(@RequestParam(name = "q", required = false) String query, Model model) {
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
