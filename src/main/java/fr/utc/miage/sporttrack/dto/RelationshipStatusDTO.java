package fr.utc.miage.sporttrack.dto;

/**
 * View-layer enum representing the relationship status between two athletes.
 * Used by the frontend (Thymeleaf templates) to determine which buttons to display.
 * This is computed from the underlying Friendship entity and FriendshipStatus.
 */
public enum RelationshipStatusDTO {

    /** Viewing own profile */
    SELF,

    /** Already friends (FriendshipStatus.ACCEPTED) */
    FRIENDS,

    /** Current user sent a pending friend request (is the initiator) */
    REQUEST_SENT,

    /** Current user received a pending friend request (is the recipient) */
    REQUEST_RECEIVED,

    /** Friend request was rejected */
    REJECTED,

    /** Current user has blocked the target user */
    BLOCKED_BY_ME,

    /** Target user has blocked the current user */
    BLOCKED_ME,

    /** Both users have blocked each other */
    MUTUALLY_BLOCKED,

    /** No relationship exists — can send a friend request */
    NONE
}