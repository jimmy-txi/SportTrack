package fr.utc.miage.sporttrack.service.User.Communication;

import fr.utc.miage.sporttrack.entity.Enumeration.FriendshipStatus;
import fr.utc.miage.sporttrack.entity.User.Athlete;
import fr.utc.miage.sporttrack.entity.User.Communication.Friendship;
import fr.utc.miage.sporttrack.repository.User.AthleteRepository;
import fr.utc.miage.sporttrack.repository.User.Communication.FriendshipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final AthleteRepository athleteRepository;

    /**
     * Initializes the FriendshipService with required repositories.
     */
    public FriendshipService(FriendshipRepository friendshipRepository, AthleteRepository athleteRepository) {
        this.friendshipRepository = friendshipRepository;
        this.athleteRepository = athleteRepository;
    }

    /**
     * Sends a friend request from one user to another.
     */
    @Transactional
    public void sendFriendRequest(Integer initiatorId, Integer recipientId) {
        // Cannot send a friend request to yourself
        if (initiatorId.equals(recipientId)) {
            throw new IllegalArgumentException("You cannot send a friend request to yourself");
        }

        // Look up both athletes
        Athlete initiator = athleteRepository.findByIdU(initiatorId).orElseThrow(() -> new IllegalArgumentException("Initiator not found"));
        Athlete recipient = athleteRepository.findByIdU(recipientId).orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        // Check if a relationship already exists between the two athletes
        friendshipRepository.findBetweenAthletes(initiator, recipient).ifPresentOrElse(existing -> handleExistingFriendship(existing, initiator, recipient), () ->
                // No existing relationship → create a new one
                friendshipRepository.save(new Friendship(initiator, recipient))

        );
    }

    /**
     * Processes an existing friendship record when a new request is made.
     */
    private void handleExistingFriendship(Friendship existing, Athlete initiator, Athlete recipient) {
        switch (existing.getStatus()) {
            case PENDING -> throw new IllegalStateException("A friend request already exists and is pending");
            case ACCEPTED -> throw new IllegalStateException("You are already friends");
            case REJECTED -> {
                // Allow re-sending after rejection: reuse the existing record
                existing.setInitiator(initiator);
                existing.setRecipient(recipient);
                existing.setStatus(FriendshipStatus.PENDING);
                existing.setCreatedAt(LocalDateTime.now());
                friendshipRepository.save(existing);
            }
        }
    }

    /**
     * Accept a pending friend request.
     * <ul>
     *   <li>Only the recipient can accept</li>
     *   <li>Only a PENDING request can be accepted</li>
     * </ul>
     *
     * @param friendshipId  the ID of the friendship record
     * @param currentUserId the ID of the current user (must be the recipient)
     */
    @Transactional
    public void acceptFriendRequest(Integer friendshipId, Integer currentUserId) {
        Friendship friendship = findFriendshipOrThrow(friendshipId);

        // Only the recipient can accept
        if (!friendship.getRecipient().getIdU().equals(currentUserId)) {
            throw new IllegalArgumentException("Only the recipient can accept a friend request");
        }

        // Only PENDING requests can be accepted
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Only pending friend requests can be accepted");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
    }

    /**
     * Rejects a pending friend request.
     */
    @Transactional
    public void rejectFriendRequest(Integer friendshipId, Integer currentUserId) {
        Friendship friendship = findFriendshipOrThrow(friendshipId);

        // Only the recipient can reject
        if (!friendship.getRecipient().getIdU().equals(currentUserId)) {
            throw new IllegalArgumentException("Only the recipient can reject a friend request");
        }

        // Only PENDING requests can be rejected
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
        // Look up both athletes
        Athlete currentUser = athleteRepository.findByIdU(currentUserId).orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        Athlete otherUser = athleteRepository.findByIdU(otherUserId).orElseThrow(() -> new IllegalArgumentException("Other user not found"));

        // Find the friendship between them
        Friendship friendship = friendshipRepository.findBetweenAthletes(currentUser, otherUser).orElseThrow(() -> new IllegalArgumentException("Friendship does not exist"));

        // Only ACCEPTED friendships can be removed
        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new IllegalStateException("Only accepted friendships can be removed");
        }

        friendshipRepository.delete(friendship);
    }

    /**
     * Returns a list of all accepted friends for a specific athlete.
     */
    public List<Athlete> getFriendsOfAthlete(Integer athleteId) {
        List<Friendship> friendships = friendshipRepository.findByAthleteAndStatus(athleteId, FriendshipStatus.ACCEPTED);

        List<Athlete> friends = new ArrayList<>();
        for (Friendship f : friendships) {
            if (f.getInitiator().getIdU().equals(athleteId)) {
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
        Athlete athlete = athleteRepository.findByIdU(athleteId).orElseThrow(() -> new IllegalArgumentException("Athlete not found"));

        return friendshipRepository.findByRecipientAndStatus(athlete, FriendshipStatus.PENDING);
    }

    /**
     * Returns all friend requests sent by the athlete that are still pending.
     */
    public List<Friendship> getSentPendingRequests(Integer athleteId) {
        Athlete athlete = athleteRepository.findByIdU(athleteId).orElseThrow(() -> new IllegalArgumentException("Athlete not found"));

        return friendshipRepository.findByInitiatorAndStatus(athlete, FriendshipStatus.PENDING);
    }


    /**
     * Finds a friendship by its ID or throws an error if not found.
     */
    private Friendship findFriendshipOrThrow(Integer friendshipId) {
        return friendshipRepository.findById(friendshipId).orElseThrow(() -> new IllegalArgumentException("Friendship not found"));
    }
}
