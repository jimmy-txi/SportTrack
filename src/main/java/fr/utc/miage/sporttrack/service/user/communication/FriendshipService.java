package fr.utc.miage.sporttrack.service.user.communication;

import fr.utc.miage.sporttrack.dto.RelationshipStatusDTO;
import fr.utc.miage.sporttrack.entity.enumeration.FriendshipStatus;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.user.communication.Friendship;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.repository.user.communication.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class FriendshipService {

    private static final String ATHLETE_NOT_FOUND = "Athlete not found";

    private final FriendshipRepository friendshipRepository;
    private final AthleteRepository athleteRepository;
    private final NotificationService notificationService;

    /**
     * Initializes the FriendshipService with required repositories.
     */
    public FriendshipService(FriendshipRepository friendshipRepository, AthleteRepository athleteRepository) {
        this(friendshipRepository, athleteRepository, null);
    }

    @Autowired
    public FriendshipService(FriendshipRepository friendshipRepository, AthleteRepository athleteRepository, NotificationService notificationService) {
        this.friendshipRepository = friendshipRepository;
        this.athleteRepository = athleteRepository;
        this.notificationService = notificationService;
    }

    // ========================= Friend Request =========================

    /**
     * Sends a friend request from one user to another.
     * Checks for block relationships before allowing the request.
     */
    @Transactional
    public void sendFriendRequest(Integer initiatorId, Integer recipientId) {
        // Cannot send a friend request to yourself
        if (initiatorId.equals(recipientId)) {
            throw new IllegalArgumentException("You cannot send a friend request to yourself");
        }

        // Look up both athletes
        Athlete initiator = findAthleteOrThrow(initiatorId, "Initiator not found");
        Athlete recipient = findAthleteOrThrow(recipientId, "Recipient not found");

        // Check if initiator has blocked recipient
        if (friendshipRepository.existsBlock(initiator, recipient)) {
            throw new IllegalStateException("You have blocked this user. Unblock them first.");
        }

        // Check if recipient has blocked initiator
        if (friendshipRepository.existsBlock(recipient, initiator)) {
            throw new IllegalStateException("Cannot send friend request to this user.");
        }

        // Check if a relationship already exists between the two athletes
        friendshipRepository.findBetweenAthletes(initiator, recipient).ifPresentOrElse(
                existing -> {
                    Friendship saved = handleExistingFriendship(existing, initiator, recipient);
                    if (notificationService != null) {
                        notificationService.notifyFriendRequest(saved);
                    }
                },
                () -> {
                    Friendship saved = friendshipRepository.save(new Friendship(initiator, recipient));
                    if (notificationService != null) {
                        notificationService.notifyFriendRequest(saved);
                    }
                }
        );
    }

    /**
     * Processes an existing friendship record when a new request is made.
     */
    private Friendship handleExistingFriendship(Friendship existing, Athlete initiator, Athlete recipient) {
        switch (existing.getStatus()) {
            case PENDING -> throw new IllegalStateException("A friend request already exists and is pending");
            case ACCEPTED -> throw new IllegalStateException("You are already friends");
            case REJECTED -> {
                // Allow re-sending after rejection: reuse the existing record
                existing.setInitiator(initiator);
                existing.setRecipient(recipient);
                existing.setStatus(FriendshipStatus.PENDING);
                existing.setCreatedAt(LocalDateTime.now());
                return friendshipRepository.save(existing);
            }
            case BLOCKED -> throw new IllegalStateException("A block relationship exists between you and this user");
        }
        return existing;
    }

    /**
     * Accept a pending friend request.
     */
    @Transactional
    public void acceptFriendRequest(Integer friendshipId, Integer currentUserId) {
        Friendship friendship = findFriendshipOrThrow(friendshipId);

        if (!friendship.getRecipient().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Only the recipient can accept a friend request");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Only pending friend requests can be accepted");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        Friendship saved = friendshipRepository.save(friendship);
        if (notificationService != null) {
            notificationService.notifyFriendRequestAccepted(saved);
        }
    }

    /**
     * Rejects a pending friend request.
     */
    @Transactional
    public void rejectFriendRequest(Integer friendshipId, Integer currentUserId) {
        Friendship friendship = findFriendshipOrThrow(friendshipId);

        if (!friendship.getRecipient().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Only the recipient can reject a friend request");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Only pending friend requests can be rejected");
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        friendshipRepository.save(friendship);
    }

    /**
     * Removes an established friendship between two users.
     */
    @Transactional
    public void removeFriend(Integer currentUserId, Integer otherUserId) {
        Athlete currentUser = findAthleteOrThrow(currentUserId, "Current user not found");
        Athlete otherUser = findAthleteOrThrow(otherUserId, "Other user not found");

        Friendship friendship = friendshipRepository.findBetweenAthletes(currentUser, otherUser)
                .orElseThrow(() -> new IllegalArgumentException("Friendship does not exist"));

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new IllegalStateException("Only accepted friendships can be removed");
        }

        friendshipRepository.delete(friendship);
    }

    // ========================= Block / Unblock =========================

    /**
     * Blocks a user. Handles all existing relationship states:
     * - If already friends: removes friendship and blocks.
     * - If pending request: cancels it and blocks.
     * - If rejected: blocks.
     * - If already blocked by blocker: throws exception.
     * - If no relationship: creates a new BLOCKED record.
     *
     * @param blockerId the user performing the block
     * @param blockedId the user being blocked
     */
    @Transactional
    public void blockUser(Integer blockerId, Integer blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException("You cannot block yourself");
        }

        Athlete blocker = findAthleteOrThrow(blockerId, "Blocker not found");
        Athlete blocked = findAthleteOrThrow(blockedId, "User to block not found");

        Optional<Friendship> existingOpt = friendshipRepository.findBetweenAthletes(blocker, blocked);

        if (existingOpt.isPresent()) {
            Friendship existing = existingOpt.get();
            switch (existing.getStatus()) {
                case BLOCKED -> {
                    if (existing.getInitiator().getId().equals(blockerId)) {
                        throw new IllegalStateException("You have already blocked this user");
                    } else {
                        // The other user blocked us — but we still want to block them too.
                        // Update the existing record: make blocker the initiator.
                        existing.setInitiator(blocker);
                        existing.setRecipient(blocked);
                        existing.setStatus(FriendshipStatus.BLOCKED);
                        existing.setCreatedAt(LocalDateTime.now());
                        friendshipRepository.save(existing);
                    }
                }
                case ACCEPTED, PENDING, REJECTED -> {
                    // Any existing relationship → convert to BLOCKED
                    existing.setInitiator(blocker);
                    existing.setRecipient(blocked);
                    existing.setStatus(FriendshipStatus.BLOCKED);
                    existing.setCreatedAt(LocalDateTime.now());
                    friendshipRepository.save(existing);
                }
            }
        } else {
            // No existing relationship → create a new BLOCKED record
            friendshipRepository.save(new Friendship(blocker, blocked, FriendshipStatus.BLOCKED));
        }
    }

    /**
     * Unblocks a previously blocked user.
     * Only the user who initiated the block can unblock.
     *
     * @param blockerId the user who performed the block
     * @param blockedId the user to unblock
     */
    @Transactional
    public void unblockUser(Integer blockerId, Integer blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException("Invalid operation");
        }

        Athlete blocker = findAthleteOrThrow(blockerId, "Blocker not found");
        Athlete blocked = findAthleteOrThrow(blockedId, "Blocked user not found");

        Friendship friendship = friendshipRepository.findBetweenAthletes(blocker, blocked)
                .orElseThrow(() -> new IllegalArgumentException("No relationship exists between these users"));

        if (friendship.getStatus() != FriendshipStatus.BLOCKED) {
            throw new IllegalStateException("This user is not blocked");
        }

        if (!friendship.getInitiator().getId().equals(blockerId)) {
            throw new IllegalStateException("You did not block this user, so you cannot unblock them");
        }

        // Remove the block record entirely
        friendshipRepository.delete(friendship);
    }

    // ========================= Query Methods =========================

    /**
     * Returns a list of all accepted friends for a specific athlete.
     */
    public List<Athlete> getFriendsOfAthlete(Integer athleteId) {
        List<Friendship> friendships = friendshipRepository.findByAthleteAndStatus(athleteId, FriendshipStatus.ACCEPTED);

        List<Athlete> friends = new ArrayList<>();
        for (Friendship f : friendships) {
            if (f.getInitiator().getId().equals(athleteId)) {
                friends.add(f.getRecipient());
            } else {
                friends.add(f.getInitiator());
            }
        }
        return friends;
    }

    /**
     * Returns all pending friend requests waiting for the athlete to accept.
     */
    public List<Friendship> getPendingRequestsForAthlete(Integer athleteId) {
        Athlete athlete = findAthleteOrThrow(athleteId, ATHLETE_NOT_FOUND);
        return friendshipRepository.findByRecipientAndStatus(athlete, FriendshipStatus.PENDING);
    }

    /**
     * Returns all friend requests sent by the athlete that are still pending.
     */
    public List<Friendship> getSentPendingRequests(Integer athleteId) {
        Athlete athlete = findAthleteOrThrow(athleteId, ATHLETE_NOT_FOUND);
        return friendshipRepository.findByInitiatorAndStatus(athlete, FriendshipStatus.PENDING);
    }

    /**
     * Returns all users blocked by the given athlete.
     */
    public List<Friendship> getBlockedUsers(Integer athleteId) {
        Athlete athlete = findAthleteOrThrow(athleteId, ATHLETE_NOT_FOUND);
        return friendshipRepository.findByInitiatorAndStatus(athlete, FriendshipStatus.BLOCKED);
    }

    // ========================= Relationship Status =========================

    /**
     * Computes the relationship status from the perspective of viewerId looking at targetId.
     * Returns a RelationshipStatusDTO that the frontend can use to determine button display.
     *
     * @param viewerId the current user
     * @param targetId the user being viewed
     * @return the computed relationship status
     */
    public RelationshipStatusDTO getRelationshipStatus(Integer viewerId, Integer targetId) {
        if (viewerId.equals(targetId)) {
            return RelationshipStatusDTO.SELF;
        }

        Athlete viewer = findAthleteOrThrow(viewerId, "Viewer not found");
        Athlete target = findAthleteOrThrow(targetId, "Target not found");

        boolean viewerBlockedTarget = friendshipRepository.existsBlock(viewer, target);
        boolean targetBlockedViewer = friendshipRepository.existsBlock(target, viewer);

        if (viewerBlockedTarget && targetBlockedViewer) {
            return RelationshipStatusDTO.MUTUALLY_BLOCKED;
        }
        if (viewerBlockedTarget) {
            return RelationshipStatusDTO.BLOCKED_BY_ME;
        }
        if (targetBlockedViewer) {
            return RelationshipStatusDTO.BLOCKED_ME;
        }

        Optional<Friendship> friendshipOpt = friendshipRepository.findBetweenAthletes(viewer, target);
        if (friendshipOpt.isEmpty()) {
            return RelationshipStatusDTO.NONE;
        }

        Friendship friendship = friendshipOpt.get();
        return switch (friendship.getStatus()) {
            case ACCEPTED -> RelationshipStatusDTO.FRIENDS;
            case PENDING -> {
                if (friendship.getInitiator().getId().equals(viewerId)) {
                    yield RelationshipStatusDTO.REQUEST_SENT;
                } else {
                    yield RelationshipStatusDTO.REQUEST_RECEIVED;
                }
            }
            case REJECTED -> RelationshipStatusDTO.REJECTED;
            case BLOCKED -> {
                // This case should be handled above, but just in case
                if (friendship.getInitiator().getId().equals(viewerId)) {
                    yield RelationshipStatusDTO.BLOCKED_BY_ME;
                } else {
                    yield RelationshipStatusDTO.BLOCKED_ME;
                }
            }
        };
    }

    // ========================= Search =========================

    /**
     * Searches for athletes visible to the current user.
     * Filters out:
     * - The current user themselves
     * - Users the current user has blocked
     * - Users who have blocked the current user
     *
     * @param currentUserId the ID of the user performing the search
     * @param keyword       the search keyword (username)
     * @return list of visible athletes matching the keyword
     */
    public List<Athlete> searchVisibleAthletes(Integer currentUserId, String keyword) {
        // Get all athletes matching the keyword
        List<Athlete> allAthletes;
        if (keyword != null && !keyword.isEmpty()) {
            allAthletes = athleteRepository.findByUsernameContainingIgnoreCase(keyword);
        } else {
            allAthletes = athleteRepository.findAll();
        }

        // Build the set of IDs to exclude
        Set<Integer> excludedIds = new HashSet<>();
        excludedIds.add(currentUserId); // exclude self

        // Exclude users the current user has blocked
        List<Integer> blockedByMe = friendshipRepository.findBlockedUserIds(currentUserId);
        excludedIds.addAll(blockedByMe);

        // Exclude users who have blocked the current user
        List<Integer> blockedMe = friendshipRepository.findBlockedByUserIds(currentUserId);
        excludedIds.addAll(blockedMe);

        // Filter the results
        List<Athlete> visibleAthletes = new ArrayList<>();
        for (Athlete a : allAthletes) {
            if (!excludedIds.contains(a.getId())) {
                visibleAthletes.add(a);
            }
        }
        return visibleAthletes;
    }

    // ========================= Helpers =========================

    /**
     * Finds a friendship by its ID or throws an error if not found.
     */
    private Friendship findFriendshipOrThrow(Integer friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Friendship not found"));
    }

    /**
     * Finds an athlete by ID or throws an error if not found.
     */
    private Athlete findAthleteOrThrow(Integer athleteId, String errorMessage) {
        return athleteRepository.findById(athleteId)
                .orElseThrow(() -> new IllegalArgumentException(errorMessage));
    }
}