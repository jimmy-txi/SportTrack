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

/**
 * Spring MVC controller for athlete activity management.
 *
 * <p>Provides endpoints for listing, creating, editing, and deleting
 * the authenticated athlete's own activities, including automatic weather
 * report fetching and badge award checks.</p>
 *
 * @author SportTrack Team
 */
@Controller
@RequestMapping("/athlete/activities")
public class AthleteActivityController {

    /** Redirect constant for the login page. */
    private static final String REDIRECT_LOGIN = "redirect:/login";

    /** Redirect constant for the athlete activities list. */
    private static final String REDIRECT_ATHLETE_ACTIVITIES = "redirect:/athlete/activities";

    /** Service for activity CRUD operations. */
    private final ActivityService activityService;

    /** Service for sport lookups. */
    private final SportService sportService;

    /** Service for weather report retrieval. */
    private final WeatherReportService weatherReportService;

    /** Service for athlete authentication resolution. */
    private final AthleteService athleteService;

    /** Service for badge award checks. */
    private final BadgeService badgeService;

    /** Service for comment retrieval on activities. */
    private final fr.utc.miage.sporttrack.service.user.communication.CommentService commentService;
    private static final String ERROR_ATTRIBUTE = "error";
    private static final String ATHLETE_ATTRIBUTE = "athlete";

    /**
     * Constructs an {@code AthleteActivityController} with the required services.
     *
     * @param activityService    the activity service
     * @param sportService       the sport service
     * @param weatherReportService the weather report service
     * @param athleteService     the athlete service
     * @param badgeService       the badge service
     * @param commentService     the comment service
     */
    public AthleteActivityController(ActivityService activityService,
                                     SportService sportService,
                                     WeatherReportService weatherReportService,
                                     AthleteService athleteService,
                                     BadgeService badgeService,
                                     fr.utc.miage.sporttrack.service.user.communication.CommentService commentService) {
        this.activityService = activityService;
        this.sportService = sportService;
        this.weatherReportService = weatherReportService;
        this.athleteService = athleteService;
        this.badgeService = badgeService;
        this.commentService = commentService;
    }

    /**
     * Lists all activities of the authenticated athlete with weather and comment data.
     *
     * @param model the Spring MVC model
     * @param auth  the current security authentication
     * @return the view name "athlete/activity/list", or a redirect to login
     */
    @GetMapping
    public String listMyActivities(Model model, Authentication auth) {
        Athlete athlete = getCurrentAthlete(auth);
        if (athlete == null) {
            return REDIRECT_LOGIN;
        }

        List<Activity> activities = activityService.findAllByAthlete(athlete);
        activities.forEach(activity -> {
            activity.setWeatherReport(
                    weatherReportService.findByActivityId(activity.getId()).orElse(null)
            );
            activity.setComments(
                    commentService.getCommentsForActivity(activity.getId())
            );
        });

        model.addAttribute(ATHLETE_ATTRIBUTE, athlete);
        model.addAttribute("activities", activities);
        return "athlete/activity/list";
    }

    /**
     * Displays the form for creating a new activity.
     *
     * @param model the Spring MVC model
     * @param auth  the current security authentication
     * @return the view name "athlete/activity/create", or a redirect to login
     */
    @GetMapping("/create")
    public String showCreateForm(Model model, Authentication auth) {
        Athlete athlete = getCurrentAthlete(auth);
        if (athlete == null) {
            return REDIRECT_LOGIN;
        }

        model.addAttribute(ATHLETE_ATTRIBUTE, athlete);
        model.addAttribute("activity", new ActivityFormDTO());
        model.addAttribute("sports", sportService.findAllActive());
        return "athlete/activity/create";
    }

    /**
     * Displays the form for editing an existing activity owned by the current athlete.
     *
     * @param id                  the activity identifier
     * @param model               the Spring MVC model
     * @param redirectAttributes  flash attributes for error messaging
     * @param auth                the current security authentication
     * @return the view name "athlete/activity/create", or a redirect on failure
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes, Authentication auth) {
        Athlete athlete = getCurrentAthlete(auth);
        if (athlete == null) {
            return REDIRECT_LOGIN;
        }

        Activity activity = activityService.findByIdForAthlete(id, athlete)
                .orElse(null);
        if (activity == null) {
            redirectAttributes.addAttribute(ERROR_ATTRIBUTE, "Activite introuvable");
            return REDIRECT_ATHLETE_ACTIVITIES;
        }

        model.addAttribute(ATHLETE_ATTRIBUTE, athlete);
        model.addAttribute("activity", toFormDTO(activity));
        model.addAttribute("sports", sportService.findAllActive());
        return "athlete/activity/create";
    }

    /**
     * Creates or updates an activity from the submitted form data.
     * After saving, triggers weather report refresh and badge award checks.
     *
     * @param activity           the form DTO containing the activity data
     * @param redirectAttributes flash attributes for success/error messaging
     * @param auth               the current security authentication
     * @return a redirect to the activity list, create, or edit page
     */
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
            redirectAttributes.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
            if (!isNewActivity(activity) && activity.getId() != null) {
                return "redirect:/athlete/activities/edit/" + activity.getId();
            }
            return "redirect:/athlete/activities/create";
        }
    }

    /**
     * Deletes an activity owned by the authenticated athlete.
     *
     * @param id                  the activity identifier to delete
     * @param redirectAttributes  flash attributes for success/error messaging
     * @param auth                the current security authentication
     * @return a redirect to the activity list
     */
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
            redirectAttributes.addAttribute(ERROR_ATTRIBUTE, exception.getMessage());
        }
        return REDIRECT_ATHLETE_ACTIVITIES;
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
     * Resolves the currently authenticated athlete from the security context.
     *
     * @param auth the current security authentication
     * @return the authenticated athlete, or {@code null} if not available
     */
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
