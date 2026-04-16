package fr.utc.miage.sporttrack.dto;

/**
 * Enumeration serving as a view-layer Data Transfer Object (DTO) that represents
 * the relationship status between two athletes within the SportTrack application.
 *
 * <p>Used by the frontend (Thymeleaf templates) to determine which action buttons
 * to display (e.g., "Add Friend", "Cancel Request", "Block"). The value is
 * computed from the underlying {@code Friendship} entity and
 * {@link fr.utc.miage.sporttrack.entity.enumeration.FriendshipStatus}.</p>
 *
 * @author SportTrack Team
 */
public enum RelationshipStatusDTO {

    /** The current user is viewing their own profile. */
    SELF,

    /** Both athletes are already friends (FriendshipStatus.ACCEPTED). */
    FRIENDS,

    /** The current user has sent a pending friend request (is the initiator). */
    REQUEST_SENT,

    /** The current user has received a pending friend request (is the recipient). */
    REQUEST_RECEIVED,

    /** A friend request was previously rejected. */
    REJECTED,

    /** The current user has blocked the target user. */
    BLOCKED_BY_ME,

    /** The target user has blocked the current user. */
    BLOCKED_ME,

    /** Both users have blocked each other. */
    MUTUALLY_BLOCKED,

    /** No relationship exists between the two athletes; a friend request can be sent. */
    NONE
}