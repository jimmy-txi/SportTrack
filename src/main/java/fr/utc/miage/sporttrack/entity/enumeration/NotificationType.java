package fr.utc.miage.sporttrack.entity.enumeration;

/**
 * Enumeration representing the different types of notifications that can be
 * generated within the SportTrack application.
 *
 * <p>Each notification type corresponds to a specific event or state change
 * in the system, enabling the application to categorize and present
 * notifications appropriately to the user.</p>
 *
 * @author SportTrack Team
 */
public enum NotificationType {

    /** Notification sent when an athlete receives a new message from another user. */
    MESSAGE_RECEIVED,

    /** Notification sent when an athlete receives a new friendship request. */
    FRIEND_REQUEST,

    /** Notification sent when a previously sent friendship request has been accepted. */
    FRIEND_REQUEST_ACCEPTED,

    /** Notification sent when a friend logs a new activity. */
    FRIEND_ACTIVITY,

    /** Notification sent when an athlete earns a new badge achievement. */
    BADGE_EARNED,

    /** Notification sent when an athlete completes a defined objective. */
    OBJECTIVE_COMPLETED,

    /** Notification sent when a challenge has concluded and results are available. */
    CHALLENGE_ENDED
}