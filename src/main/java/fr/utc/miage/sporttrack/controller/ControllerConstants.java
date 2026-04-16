package fr.utc.miage.sporttrack.controller;

/**
 * Centralized constants for Spring MVC controllers.
 * Defines common redirect URLs, view names, model attribute names,
 * and parameter names used across the application.
 *
 * @author SportTrack Team
 */
public final class ControllerConstants {

    // Redirect URLs
    /** Redirect constant for the login page. */
    public static final String REDIRECT_LOGIN = "redirect:/login";

    /** Redirect constant for the admin home page. */
    public static final String REDIRECT_ADMIN = "redirect:/admin";

    /** Redirect constant for the athlete profile page. */
    public static final String REDIRECT_ATHLETE_PROFILE = "redirect:/athlete/profile";

    /** Redirect constant for the athlete list page. */
    public static final String REDIRECT_ATHLETE_LIST = "redirect:/athlete/list";

    /** Redirect constant for the friend list page. */
    public static final String REDIRECT_FRIENDS = "redirect:/friends";

    /** Redirect constant for the badges page. */
    public static final String REDIRECT_BADGES = "redirect:/badges";

    /** Redirect constant for the challenges page. */
    public static final String REDIRECT_CHALLENGES = "redirect:/challenges";

    // View Names
    /** View name for the athlete profile. */
    public static final String ATHLETE_PROFILE_VIEW = "athlete/profile";

    /** View name for the athlete list. */
    public static final String ATHLETE_LIST_VIEW = "athlete/list";

    /** View name for the admin home. */
    public static final String ADMIN_HOME_VIEW = "admin/home";

    /** View name for athlete badges. */
    public static final String ATHLETE_BADGE_LIST_VIEW = "athlete/badge/list";

    // Model Attribute Names
    /** Model attribute name for athlete. */
    public static final String ATHLETE_ATTR = "athlete";

    /** Model attribute name for error messages. */
    public static final String ERROR_ATTR = "error";

    /** Model attribute name for success messages. */
    public static final String SUCCESS_ATTR = "success";

    /** Model attribute name for sports. */
    public static final String SPORTS_ATTR = "sports";

    /** Model attribute name for badges. */
    public static final String BADGES_ATTR = "badges";

    /** Model attribute name for challenges. */
    public static final String CHALLENGES_ATTR = "challenges";

    // Request Parameter Names
    /** Request parameter name for creation. */
    public static final String CREATED_PARAM = "created";

    /** Request parameter name for update. */
    public static final String UPDATED_PARAM = "updated";

    /** Request parameter name for deletion. */
    public static final String DELETED_PARAM = "deleted";

    /** Request parameter name for registration. */
    public static final String REGISTERED_PARAM = "registered";

    // Private constructor to prevent instantiation
    private ControllerConstants() {
        throw new AssertionError("Cannot instantiate ControllerConstants");
    }
}
