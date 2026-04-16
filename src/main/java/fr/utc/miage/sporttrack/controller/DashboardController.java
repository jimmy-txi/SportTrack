package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.service.activity.ActivityService;
import fr.utc.miage.sporttrack.service.activity.SportService;
import fr.utc.miage.sporttrack.service.event.ObjectiveService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final ActivityService activityService;
    private final ObjectiveService objectiveService;
    private final SportService sportService;
    private final AthleteRepository athleteRepository;

    public DashboardController(ActivityService activityService,
                               ObjectiveService objectiveService,
                               SportService sportService,
                               AthleteRepository athleteRepository) {
        this.activityService = activityService;
        this.objectiveService = objectiveService;
        this.sportService = sportService;
        this.athleteRepository = athleteRepository;
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model,
                                @RequestParam(required = false) Integer sportId,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }

        List<Activity> activities = activityService.findAllByAthlete(athlete);
        List<Sport> sports = sportService.findAllActive();
        Sport selectedSport = (sportId != null && sportId > 0)
                ? sportService.findById(sportId).orElse(null)
                : null;

        List<Activity> filteredActivities = activities.stream()
                .filter(activity -> activityService.filterBySport(activity, selectedSport))
                .filter(activity -> activityService.filterByDate(activity, startDate, endDate))
                .toList();

        List<Objective> objectives = new ArrayList<>();
        objectiveService.getObjectivesByUser(athlete).forEach(objectives::add);
        if (selectedSport != null) {
            objectives = objectives.stream()
                    .filter(objective -> objective.getSport() != null && objective.getSport().getId() == selectedSport.getId())
                    .toList();
        }

        int totalActivities = filteredActivities.size();
        double totalDistance = filteredActivities.stream()
                .map(Activity::getDistance)
                .filter(d -> d != null)
                .mapToDouble(Double::doubleValue)
                .sum();
        double totalDuration = filteredActivities.stream()
                .mapToDouble(Activity::getDuration)
                .sum();
        double totalCalories = filteredActivities.stream()
                .mapToDouble(Activity::getCaloriesBurned)
                .sum();
        int totalObjectives = objectives.size();

        Map<String, Integer> activityCountBySport = new LinkedHashMap<>();
        Map<String, Integer> objectiveCountBySport = new LinkedHashMap<>();
        Map<Integer, Boolean> objectiveProgressMap = new LinkedHashMap<>();

        for (Activity activity : filteredActivities) {
            String sportName = sportService.safeSportName(activity.getSportAndType());
            activityCountBySport.merge(sportName, 1, Integer::sum);
        }

        for (Objective objective : objectives) {
            String sportName = sportService.safeSportName(objective.getSport());
            objectiveCountBySport.merge(sportName, 1, Integer::sum);
            objectiveProgressMap.put(objective.getId(), objectiveService.isObjectiveCompleted(objective, filteredActivities));
        }

        Set<String> chartLabelsSet = new LinkedHashSet<>();
        chartLabelsSet.addAll(activityCountBySport.keySet());
        chartLabelsSet.addAll(objectiveCountBySport.keySet());
        List<String> chartLabels = new ArrayList<>(chartLabelsSet);

        List<Integer> activityCounts = chartLabels.stream()
                .map(label -> activityCountBySport.getOrDefault(label, 0))
                .collect(Collectors.toList());

        long objectivesCompleted = objectiveProgressMap.values().stream().filter(Boolean::booleanValue).count();
        long objectivesRemaining = totalObjectives - objectivesCompleted;
        int objectiveProgressRate = totalObjectives > 0 ? (int) Math.round(100.0 * objectivesCompleted / totalObjectives) : 0;
        List<String> objectiveStatusLabels = List.of("Réalisés", "À réaliser");
        List<Integer> objectiveStatusCounts = List.of((int) objectivesCompleted, (int) objectivesRemaining);

        model.addAttribute("athlete", athlete);
        model.addAttribute("activities", filteredActivities);
        model.addAttribute("recentActivities", filteredActivities.stream().limit(5).collect(Collectors.toList()));
        model.addAttribute("objectives", objectives);
        model.addAttribute("sports", sports);
        model.addAttribute("selectedSport", selectedSport);
        model.addAttribute("selectedSportId", selectedSport != null ? selectedSport.getId() : null);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("totalActivities", totalActivities);
        model.addAttribute("totalDistance", totalDistance);
        model.addAttribute("totalDuration", totalDuration);
        model.addAttribute("totalCalories", totalCalories);
        model.addAttribute("totalObjectives", totalObjectives);
        model.addAttribute("objectivesCompleted", objectivesCompleted);
        model.addAttribute("objectivesRemaining", objectivesRemaining);
        model.addAttribute("objectiveProgressRate", objectiveProgressRate);
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("activityCounts", activityCounts);
        model.addAttribute("objectiveStatusLabels", objectiveStatusLabels);
        model.addAttribute("objectiveStatusCounts", objectiveStatusCounts);
        model.addAttribute("objectiveProgressMap", objectiveProgressMap);

        return "dashboard/compare";
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

        return athleteRepository.findByEmail(authentication.getName())
                .map(found -> {
                    session.setAttribute("athlete", found);
                    return found;
                })
                .orElse(null);
    }
}
