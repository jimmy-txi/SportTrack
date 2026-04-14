package fr.utc.miage.sporttrack.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.dto.ActivityFormDTO;
import fr.utc.miage.sporttrack.service.activity.ActivityService;
import fr.utc.miage.sporttrack.service.activity.SportService;
import fr.utc.miage.sporttrack.service.activity.WeatherReportService;
import fr.utc.miage.sporttrack.service.user.AdminService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/activities")
public class ActivityController {

    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_ACTIVITIES = "redirect:/admin/activities";
    private static final String ERROR_PARAM = "error";
    private static final String EDIT_VIEW = "admin/activity/edit";

    private final ActivityService activityService;
    private final SportService sportService;
    private final WeatherReportService weatherReportService;
    private final AdminService adminService;

    public ActivityController(ActivityService activityService, SportService sportService, WeatherReportService weatherReportService, AdminService adminService) {
        this.activityService = activityService;
        this.sportService = sportService;
        this.weatherReportService = weatherReportService;
        this.adminService = adminService;
    }

    /**
     * Affiche la liste de tous les activities
     */
    @GetMapping
    public String listActivities(Model model, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return REDIRECT_LOGIN;
        }

        List<Activity> activities = activityService.findAll();
        activities.forEach(activity -> activity.setWeatherReport(
                weatherReportService.findByActivityId(activity.getId()).orElse(null)
        ));
        model.addAttribute("activities", activities);
        return "admin/activity/list";
    }

    /**
     * Affiche le formulaire d'édition d'un Activity existant
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return REDIRECT_LOGIN;
        }

        Optional<Activity> activity = activityService.findById(id);
        if (activity.isEmpty()) {
            redirectAttributes.addAttribute(ERROR_PARAM, "Activity not found");
            return REDIRECT_ACTIVITIES;
        }
        model.addAttribute("activity", toFormDTO(activity.get()));
        model.addAttribute("sports", sportService.findAll());
        return EDIT_VIEW;
    }

    /**
     * Sauvegarde un nouveau Activity ou met à jour un Activity existant
     */
    @PostMapping("/save")
    public String saveActivity(@ModelAttribute("activity") ActivityFormDTO activity, RedirectAttributes redirectAttributes, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return REDIRECT_LOGIN;
        }

        if (isNewActivity(activity)) {
            redirectAttributes.addAttribute(ERROR_PARAM, "Creation d'activite reservee aux athletes");
            return REDIRECT_ACTIVITIES;
        }

        try {
            updateActivityAndWeatherReport(activity);
            redirectAttributes.addAttribute("updated", true);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute(ERROR_PARAM, e.getMessage());
            return "redirect:/admin/activities/edit/" + activity.getId();
        }
        return REDIRECT_ACTIVITIES;
    }

    private boolean isNewActivity(ActivityFormDTO activity) {
        return activity.getId() == null || activity.getId() == 0;
    }

    private void updateActivityAndWeatherReport(ActivityFormDTO activity) {
        Activity savedActivity = activityService.updateActivity(
                activity.getId(),
                activity.getDuration(),
            activity.getTitle(),
            activity.getDescription(),
                getRepetition(activity),
                getDistance(activity),
                activity.getDateA(),
            activity.getStartTime(),
                activity.getLocationCity(),
                getSportId(activity)
        );
        persistWeatherReportSafely(savedActivity);
    }

    private void persistWeatherReportSafely(Activity savedActivity) {
        try {
            weatherReportService.refreshWeatherReport(savedActivity);
        } catch (RuntimeException ignored) {
            // Weather report generation is best effort and must not block activity saving.
        }
    }

    private int getSportId(ActivityFormDTO activity) {
        return activity.getSportId() != null ? activity.getSportId() : 0;
    }

    private int getRepetition(ActivityFormDTO activity) {
        return activity.getRepetition() != null ? activity.getRepetition() : 0;
    }

    private double getDistance(ActivityFormDTO activity) {
        return activity.getDistance() != null ? activity.getDistance() : 0d;
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

    @PostMapping("/delete/{id}")
    public String deleteActivity(@PathVariable int id, RedirectAttributes redirectAttributes, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return REDIRECT_LOGIN;
        }

        try {
            activityService.deleteById(id);
            redirectAttributes.addAttribute("deleted", true);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute(ERROR_PARAM, e.getMessage());
        }
        return REDIRECT_ACTIVITIES;
    }
}
