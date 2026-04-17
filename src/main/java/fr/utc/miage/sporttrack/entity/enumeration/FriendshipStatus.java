package fr.utc.miage.sporttrack.entity.enumeration;

/**
 * Enumeration representing the possible statuses of a friendship relationship
 * between two athletes within the SportTrack application.
 *
 * <p>Each status reflects a distinct stage in the friendship lifecycle,
 * from the initial request through to acceptance, rejection, or blocking.</p>
 *
 * @author SportTrack Team
 */
public enum FriendshipStatus {

    /** The friendship request has been sent but not yet responded to by the recipient. */
    PENDING,

    /** The friendship request has been accepted; both athletes are now connected as friends. */
    ACCEPTED,

    /** The friendship request has been explicitly declined by the recipient. */
    REJECTED,

    /** One athlete has blocked the other, preventing any further communication or interaction. */
    BLOCKED

}