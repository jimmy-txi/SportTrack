package fr.utc.miage.sporttrack.util;

/**
 * Centralized error and information messages for the SportTrack application.
 * Provides consistent messaging across controllers and services.
 *
 * @author SportTrack Team
 */
public final class Messages {

    // Authentication messages
    /** Error message when password confirmation does not match. */
    public static final String PASSWORD_MISMATCH = "Passwords do not match";

    /** Error message for invalid registration. */
    public static final String INVALID_REGISTRATION = "Invalid registration form";

    // Entity not found messages
    /** Error message when sport is not found. */
    public static final String SPORT_NOT_FOUND = "Sport not found";

    /** Error message when activity is not found. */
    public static final String ACTIVITY_NOT_FOUND = "Activity not found";

    /** Error message when badge is not found. */
    public static final String BADGE_NOT_FOUND = "Badge not found";

    /** Error message when challenge is not found. */
    public static final String CHALLENGE_NOT_FOUND = "Challenge not found";

    /** Error message when user/athlete is not found. */
    public static final String ATHLETE_NOT_FOUND = "Athlete not found";

    // Activity messages
    /** Error message when activity creation is reserved for athletes. */
    public static final String ACTIVITY_CREATION_RESERVED = "Creation d'activite reservee aux athletes";

    /** Error message for invalid activity. */
    public static final String INVALID_ACTIVITY = "Activite introuvable";

    // Challenge messages
    /** Error message for invalid sport selection. */
    public static final String INVALID_SPORT_SELECTION = "Veuillez sélectionner une discipline sportive valide.";

    /** Error message when selected sport is not found. */
    public static final String SELECTED_SPORT_NOT_FOUND = "La discipline sportive sélectionnée est introuvable.";

    /** Error message for invalid challenge dates. */
    public static final String INVALID_CHALLENGE_DATES = "La date de début doit être antérieure ou égale à la date de fin.et les dates doivent être supérieures ou égales à la date actuelle.";

    // Friendship messages
    /** Success message for friend request sent. */
    public static final String FRIEND_REQUEST_SENT = "Demande d'ami envoyée avec succès !";

    /** Success message for friend request accepted. */
    public static final String FRIEND_REQUEST_ACCEPTED = "Demande d'ami acceptée !";

    /** Success message for friend request rejected. */
    public static final String FRIEND_REQUEST_REJECTED = "Demande d'ami refusée.";

    /** Success message for friend removed. */
    public static final String FRIEND_REMOVED = "Ami supprimé avec succès.";

    /** Success message for user blocked. */
    public static final String USER_BLOCKED = "Utilisateur bloqué avec succès.";

    /** Success message for user unblocked. */
    public static final String USER_UNBLOCKED = "Utilisateur débloqué avec succès.";

    // Private constructor to prevent instantiation
    private Messages() {
        throw new AssertionError("Cannot instantiate Messages");
    }
}
