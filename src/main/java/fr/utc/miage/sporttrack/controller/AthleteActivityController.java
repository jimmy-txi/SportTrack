package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.dto.ActivityFormDTO;
import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.service.activity.ActivityService;
import fr.utc.miage.sporttrack.service.activity.SportService;
import fr.utc.miage.sporttrack.service.activity.WeatherReportService;
import fr.utc.miage.sporttrack.service.event.BadgeService;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/athlete/activities")
public class AthleteActivityController {

    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_ATHLETE_ACTIVITIES = "redirect:/athlete/activities";

    private final ActivityService activityService;
    private final SportService sportService;
    private final WeatherReportService weatherReportService;
    private final AthleteService athleteService;
    private final BadgeService badgeService;

    public AthleteActivityController(ActivityService activityService,
                                     SportService sportService,
                                     WeatherReportService weatherReportService,
                                     AthleteService athleteService,
                                     BadgeService badgeService) {
        this.activityService = activityService;
        this.sportService = sportService;
        this.weatherReportService = weatherReportService;
        this.athleteService = athleteService;
        this.badgeService = badgeService;
    }

    @GetMapping
    public String listMyActivities(Model model, Authentication auth) {
        Athlete athlete = getCurrentAthlete(auth);
        if (athlete == null) {
            return REDIRECT_LOGIN;
        }

        List<Activity> activities = activityService.findAllByAthlete(athlete);
        activities.forEach(activity -> activity.setWeatherReport(
                weatherReportService.findByActivityId(activity.getId()).orElse(null)
        ));

        model.addAttribute("activities", activities);
        return "athlete/activity/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, Authentication auth) {
        Athlete athlete = getCurrentAthlete(auth);
        if (athlete == null) {
            return REDIRECT_LOGIN;
        }

        model.addAttribute("activity", new ActivityFormDTO());
        model.addAttribute("sports", sportService.findAllActive());
        return "athlete/activity/create";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes, Authentication auth) {
        Athlete athlete = getCurrentAthlete(auth);
        if (athlete == null) {
            return REDIRECT_LOGIN;
        }

        Activity activity = activityService.findByIdForAthlete(id, athlete)
                .orElse(null);
        if (activity == null) {
            redirectAttributes.addAttribute("error", "Activite introuvable");
            return REDIRECT_ATHLETE_ACTIVITIES;
        }

        model.addAttribute("activity", toFormDTO(activity));
        model.addAttribute("sports", sportService.findAllActive());
        return "athlete/activity/create";
    }

    @PostMapping("/save")
    public String saveActivity(@ModelAttribute("activity") ActivityFormDTO activity,
                               RedirectAttributes redirectAttributes,
                               Authentication auth) {
        Athlete athlete = getCurrentAthlete(auth);
        if (athlete == null) {
            return REDIRECT_LOGIN;
        }

        try {
            Activity savedActivity;
            if (isNewActivity(activity)) {
                savedActivity = activityService.createActivityForAthlete(
                        athlete,
                        activity.getDuration(),
                        activity.getTitle(),
                        activity.getDescription(),
                        activity.getRepetition() != null ? activity.getRepetition() : 0,
                        activity.getDistance() != null ? activity.getDistance() : 0d,
                        activity.getDateA(),
                        activity.getStartTime(),
                        activity.getLocationCity(),
                        activity.getSportId() != null ? activity.getSportId() : 0
                );
            } else {
                savedActivity = activityService.updateActivityForAthlete(
                        athlete,
                        activity.getId(),
                        activity.getDuration(),
                        activity.getTitle(),
                        activity.getDescription(),
                        activity.getRepetition() != null ? activity.getRepetition() : 0,
                        activity.getDistance() != null ? activity.getDistance() : 0d,
                        activity.getDateA(),
                        activity.getStartTime(),
                        activity.getLocationCity(),
                        activity.getSportId() != null ? activity.getSportId() : 0
                );
            }

            try {
                weatherReportService.refreshWeatherReport(savedActivity);
            } catch (RuntimeException ignored) {
                // Best effort: weather should not block activity creation.
            }

            try {
                badgeService.checkAndAwardBadges(savedActivity);
            } catch (RuntimeException ignored) {
                // Best effort: badge check should not block activity creation.
            }

            redirectAttributes.addAttribute(isNewActivity(activity) ? "created" : "updated", true);
            return REDIRECT_ATHLETE_ACTIVITIES;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            if (!isNewActivity(activity) && activity.getId() != null) {
                return "redirect:/athlete/activities/edit/" + activity.getId();
            }
            return "redirect:/athlete/activities/create";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteActivity(@PathVariable int id, RedirectAttributes redirectAttributes, Authentication auth) {
        Athlete athlete = getCurrentAthlete(auth);
        if (athlete == null) {
            return REDIRECT_LOGIN;
        }

        try {
            activityService.deleteByIdForAthlete(athlete, id);
            redirectAttributes.addAttribute("deleted", true);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addAttribute("error", exception.getMessage());
        }
        return REDIRECT_ATHLETE_ACTIVITIES;
    }

    private boolean isNewActivity(ActivityFormDTO activity) {
        return activity.getId() == null || activity.getId() == 0;
    }

    private ActivityFormDTO toFormDTO(Activity activity) {
        ActivityFormDTO dto = new ActivityFormDTO();
        dto.setId(activity.getId());
        dto.setDuration(activity.getDuration());
        dto.setTitle(activity.getTitle());
        dto.setDescription(activity.getDescription());
        dto.setRepetition(activity.getRepetition());
        dto.setDistance(activity.getDistance());
        dto.setDateA(activity.getDateA());
        dto.setStartTime(activity.getStartTime());
        dto.setLocationCity(activity.getLocationCity());
        dto.setSportId(activity.getSportId());
        return dto;
    }

    private Athlete getCurrentAthlete(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }

        try {
            return athleteService.getCurrentAthlete(auth.getName());
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
