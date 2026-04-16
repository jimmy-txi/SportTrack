package fr.utc.miage.sporttrack.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.dto.ActivityFormDTO;
import fr.utc.miage.sporttrack.service.activity.ActivityService;
import fr.utc.miage.sporttrack.service.activity.SportService;
import fr.utc.miage.sporttrack.service.activity.WeatherReportService;
import fr.utc.miage.sporttrack.service.user.AdminService;

import java.util.List;
import java.util.Optional;

/**
 * Spring MVC controller for administrator activity management.
 *
 * <p>Provides endpoints for listing, editing, and deleting any activity
 * in the system. Activity creation is reserved for athletes.</p>
 *
 * @author SportTrack Team
 */
@Controller
@RequestMapping("/admin/activities")
public class AdminActivityController {

    /** Redirect constant for the login page. */
    private static final String REDIRECT_LOGIN = "redirect:/login";

    /** Redirect constant for the admin activity list. */
    private static final String REDIRECT_ACTIVITIES = "redirect:/admin/activities";

    /** Request parameter name for error messages. */
    private static final String ERROR_PARAM = "error";

    /** View name for the activity edit form. */
    private static final String EDIT_VIEW = "admin/activity/edit";

    /** Service for activity CRUD operations. */
    private final ActivityService activityService;

    /** Service for sport lookups. */
    private final SportService sportService;

    /** Service for weather report retrieval. */
    private final WeatherReportService weatherReportService;

    /** Service for admin authentication verification. */
    private final AdminService adminService;

    /**
     * Constructs an {@code AdminActivityController} with the required services.
     *
     * @param activityService       the activity service
     * @param sportService          the sport service
     * @param weatherReportService  the weather report service
     * @param adminService          the admin service
     */
    public AdminActivityController(ActivityService activityService, SportService sportService, WeatherReportService weatherReportService, AdminService adminService) {
        this.activityService = activityService;
        this.sportService = sportService;
        this.weatherReportService = weatherReportService;
        this.adminService = adminService;
    }

    /**
     * Lists all activities in the system for the admin dashboard.
     *
     * @param model the Spring MVC model
     * @param auth  the current security authentication
     * @return the view name "admin/activity/list", or a redirect to login
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
     * Displays the form for editing an existing activity.
     *
     * @param id                  the activity identifier
     * @param model               the Spring MVC model
     * @param redirectAttributes  flash attributes for error messaging
     * @param auth                the current security authentication
     * @return the edit view, or a redirect on failure
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
        model.addAttribute("sports", sportService.findAllActive());
        return EDIT_VIEW;
    }

    /**
     * Updates an existing activity from the submitted form data.
     * Activity creation is not allowed for admins; only updates are supported.
     *
     * @param activity            the form DTO containing the updated activity data
     * @param redirectAttributes  flash attributes for success/error messaging
     * @param auth                the current security authentication
     * @return a redirect to the activity list or edit form on error
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

    /**
     * Determines whether the given DTO represents a new (unsaved) activity.
     *
     * @param activity the form DTO to check
     * @return {@code true} if the activity has no identifier
     */
    private boolean isNewActivity(ActivityFormDTO activity) {
        return activity.getId() == null || activity.getId() == 0;
    }

    /**
     * Updates the activity and refreshes its weather report (best effort).
     *
     * @param activity the form DTO with updated fields
     */
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

    /**
     * Refreshes the weather report for the given activity, catching any failure silently.
     *
     * @param savedActivity the activity whose weather report should be refreshed
     */
    private void persistWeatherReportSafely(Activity savedActivity) {
        try {
            weatherReportService.refreshWeatherReport(savedActivity);
        } catch (RuntimeException ignored) {
            // Weather report generation is best effort and must not block activity saving.
        }
    }

    /**
     * Returns the sport identifier from the DTO, defaulting to zero.
     *
     * @param activity the form DTO
     * @return the sport identifier or {@code 0}
     */
    private int getSportId(ActivityFormDTO activity) {
        return activity.getSportId() != null ? activity.getSportId() : 0;
    }

    /**
     * Returns the repetition count from the DTO, defaulting to zero.
     *
     * @param activity the form DTO
     * @return the repetition count or {@code 0}
     */
    private int getRepetition(ActivityFormDTO activity) {
        return activity.getRepetition() != null ? activity.getRepetition() : 0;
    }

    /**
     * Returns the distance from the DTO, defaulting to zero.
     *
     * @param activity the form DTO
     * @return the distance or {@code 0.0}
     */
    private double getDistance(ActivityFormDTO activity) {
        return activity.getDistance() != null ? activity.getDistance() : 0d;
    }

    /**
     * Converts an {@link Activity} entity to an {@link ActivityFormDTO} for form binding.
     *
     * @param activity the entity to convert
     * @return the populated DTO
     */
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

    /**
     * Deletes the activity with the given identifier.
     *
     * @param id                  the activity identifier to delete
     * @param redirectAttributes  flash attributes for success/error messaging
     * @param auth                the current security authentication
     * @return a redirect to the activity list
     */
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
