package fr.utc.miage.sporttrack.service.user.communication;

import fr.utc.miage.sporttrack.dto.RelationshipStatusDTO;
import fr.utc.miage.sporttrack.entity.enumeration.FriendshipStatus;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.user.communication.Friendship;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.repository.user.communication.FriendshipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    private static final Integer USER_ID_1 = 1;
    private static final Integer USER_ID_2 = 2;
    private static final Integer USER_ID_3 = 3;
    private static final Integer FRIENDSHIP_ID = 10;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private AthleteRepository athleteRepository;

    @InjectMocks
    private FriendshipService service;

    // ==================== Helper methods ====================

    private Athlete createAthlete(Integer id) {
        Athlete a = new Athlete();
        a.setUsername("user" + id);
        a.setPassword("pass" + id);
        a.setEmail("user" + id + "@test.com");
        try {
            var field = a.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(a, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return a;
    }

    private Friendship createFriendship(Integer id, Athlete initiator, Athlete recipient, FriendshipStatus status) {
        Friendship f = new Friendship(initiator, recipient);
        f.setStatus(status);
        try {
            var field = f.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(f, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return f;
    }

    // ==================== 1. sendFriendRequest ====================

    @Test
    void sendFriendRequest_shouldThrowWhenSendingToSelf() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.sendFriendRequest(USER_ID_1, USER_ID_1));
        assertEquals("You cannot send a friend request to yourself", ex.getMessage());
    }

    @Test
    void sendFriendRequest_shouldThrowWhenInitiatorNotFound() {
        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.sendFriendRequest(USER_ID_1, USER_ID_2));
        assertEquals("Initiator not found", ex.getMessage());
    }

    @Test
    void sendFriendRequest_shouldThrowWhenRecipientNotFound() {
        Athlete initiator = createAthlete(USER_ID_1);
        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(initiator));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.sendFriendRequest(USER_ID_1, USER_ID_2));
        assertEquals("Recipient not found", ex.getMessage());
    }

    @Test
    void sendFriendRequest_shouldThrowWhenPendingAlreadyExists() {
        Athlete initiator = createAthlete(USER_ID_1);
        Athlete recipient = createAthlete(USER_ID_2);
        Friendship existing = createFriendship(FRIENDSHIP_ID, initiator, recipient, FriendshipStatus.PENDING);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(initiator));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(recipient));
        when(friendshipRepository.findBetweenAthletes(initiator, recipient)).thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.sendFriendRequest(USER_ID_1, USER_ID_2));
        assertEquals("A friend request already exists and is pending", ex.getMessage());
    }

    @Test
    void sendFriendRequest_shouldThrowWhenAlreadyFriends() {
        Athlete initiator = createAthlete(USER_ID_1);
        Athlete recipient = createAthlete(USER_ID_2);
        Friendship existing = createFriendship(FRIENDSHIP_ID, initiator, recipient, FriendshipStatus.ACCEPTED);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(initiator));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(recipient));
        when(friendshipRepository.findBetweenAthletes(initiator, recipient)).thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.sendFriendRequest(USER_ID_1, USER_ID_2));
        assertEquals("You are already friends", ex.getMessage());
    }

    @Test
    void sendFriendRequest_shouldReuseRecordWhenRejected() {
        Athlete initiator = createAthlete(USER_ID_1);
        Athlete recipient = createAthlete(USER_ID_2);
        Friendship existing = createFriendship(FRIENDSHIP_ID, initiator, recipient, FriendshipStatus.REJECTED);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(initiator));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(recipient));
        when(friendshipRepository.findBetweenAthletes(initiator, recipient)).thenReturn(Optional.of(existing));

        service.sendFriendRequest(USER_ID_1, USER_ID_2);

        assertEquals(FriendshipStatus.PENDING, existing.getStatus());
        assertNotNull(existing.getCreatedAt());
        verify(friendshipRepository).save(existing);
    }

    @Test
    void sendFriendRequest_shouldCreateNewWhenNoExistingRelationship() {
        Athlete initiator = createAthlete(USER_ID_1);
        Athlete recipient = createAthlete(USER_ID_2);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(initiator));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(recipient));
        when(friendshipRepository.findBetweenAthletes(initiator, recipient)).thenReturn(Optional.empty());

        service.sendFriendRequest(USER_ID_1, USER_ID_2);

        verify(friendshipRepository).save(any(Friendship.class));
    }

    // ==================== 2. acceptFriendRequest ====================

    @Test
    void acceptFriendRequest_shouldThrowWhenFriendshipNotFound() {
        when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.acceptFriendRequest(FRIENDSHIP_ID, USER_ID_2));
        assertEquals("Friendship not found", ex.getMessage());
    }

    @Test
    void acceptFriendRequest_shouldThrowWhenNotRecipient() {
        Athlete initiator = createAthlete(USER_ID_1);
        Athlete recipient = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, initiator, recipient, FriendshipStatus.PENDING);

        when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.of(f));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.acceptFriendRequest(FRIENDSHIP_ID, USER_ID_3));
        assertEquals("Only the recipient can accept a friend request", ex.getMessage());
    }

    @Test
    void acceptFriendRequest_shouldThrowWhenNotPending() {
        Athlete initiator = createAthlete(USER_ID_1);
        Athlete recipient = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, initiator, recipient, FriendshipStatus.ACCEPTED);

        when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.of(f));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.acceptFriendRequest(FRIENDSHIP_ID, USER_ID_2));
        assertEquals("Only pending friend requests can be accepted", ex.getMessage());
    }

    @Test
    void acceptFriendRequest_shouldAcceptSuccessfully() {
        Athlete initiator = createAthlete(USER_ID_1);
        Athlete recipient = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, initiator, recipient, FriendshipStatus.PENDING);

        when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.of(f));

        service.acceptFriendRequest(FRIENDSHIP_ID, USER_ID_2);

        assertEquals(FriendshipStatus.ACCEPTED, f.getStatus());
        verify(friendshipRepository).save(f);
    }

    // ==================== 3. rejectFriendRequest ====================

    @Test
    void rejectFriendRequest_shouldThrowWhenFriendshipNotFound() {
        when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.rejectFriendRequest(FRIENDSHIP_ID, USER_ID_2));
        assertEquals("Friendship not found", ex.getMessage());
    }

    @Test
    void rejectFriendRequest_shouldThrowWhenNotRecipient() {
        Athlete initiator = createAthlete(USER_ID_1);
        Athlete recipient = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, initiator, recipient, FriendshipStatus.PENDING);

        when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.of(f));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.rejectFriendRequest(FRIENDSHIP_ID, USER_ID_3));
        assertEquals("Only the recipient can reject a friend request", ex.getMessage());
    }

    @Test
    void rejectFriendRequest_shouldThrowWhenNotPending() {
        Athlete initiator = createAthlete(USER_ID_1);
        Athlete recipient = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, initiator, recipient, FriendshipStatus.REJECTED);

        when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.of(f));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.rejectFriendRequest(FRIENDSHIP_ID, USER_ID_2));
        assertEquals("Only pending friend requests can be rejected", ex.getMessage());
    }

    @Test
    void rejectFriendRequest_shouldRejectSuccessfully() {
        Athlete initiator = createAthlete(USER_ID_1);
        Athlete recipient = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, initiator, recipient, FriendshipStatus.PENDING);

        when(friendshipRepository.findById(FRIENDSHIP_ID)).thenReturn(Optional.of(f));

        service.rejectFriendRequest(FRIENDSHIP_ID, USER_ID_2);

        assertEquals(FriendshipStatus.REJECTED, f.getStatus());
        verify(friendshipRepository).save(f);
    }

    // ==================== 4. removeFriend ====================

    @Test
    void removeFriend_shouldThrowWhenCurrentUserNotFound() {
        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.removeFriend(USER_ID_1, USER_ID_2));
        assertEquals("Current user not found", ex.getMessage());
    }

    @Test
    void removeFriend_shouldThrowWhenOtherUserNotFound() {
        Athlete currentUser = createAthlete(USER_ID_1);
        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(currentUser));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.removeFriend(USER_ID_1, USER_ID_2));
        assertEquals("Other user not found", ex.getMessage());
    }

    @Test
    void removeFriend_shouldThrowWhenFriendshipDoesNotExist() {
        Athlete currentUser = createAthlete(USER_ID_1);
        Athlete otherUser = createAthlete(USER_ID_2);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(currentUser));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(otherUser));
        when(friendshipRepository.findBetweenAthletes(currentUser, otherUser)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.removeFriend(USER_ID_1, USER_ID_2));
        assertEquals("Friendship does not exist", ex.getMessage());
    }

    @Test
    void removeFriend_shouldThrowWhenNotAccepted() {
        Athlete currentUser = createAthlete(USER_ID_1);
        Athlete otherUser = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, currentUser, otherUser, FriendshipStatus.PENDING);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(currentUser));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(otherUser));
        when(friendshipRepository.findBetweenAthletes(currentUser, otherUser)).thenReturn(Optional.of(f));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.removeFriend(USER_ID_1, USER_ID_2));
        assertEquals("Only accepted friendships can be removed", ex.getMessage());
    }

    @Test
    void removeFriend_shouldRemoveSuccessfully() {
        Athlete currentUser = createAthlete(USER_ID_1);
        Athlete otherUser = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, currentUser, otherUser, FriendshipStatus.ACCEPTED);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(currentUser));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(otherUser));
        when(friendshipRepository.findBetweenAthletes(currentUser, otherUser)).thenReturn(Optional.of(f));

        service.removeFriend(USER_ID_1, USER_ID_2);

        verify(friendshipRepository).delete(f);
    }

    // ==================== 5. getFriendsOfAthlete ====================

    @Test
    void getFriendsOfAthlete_shouldReturnFriendsWhenAthleteIsInitiator() {
        Athlete athlete = createAthlete(USER_ID_1);
        Athlete friend = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, athlete, friend, FriendshipStatus.ACCEPTED);

        when(friendshipRepository.findByAthleteAndStatus(USER_ID_1, FriendshipStatus.ACCEPTED))
                .thenReturn(List.of(f));

        List<Athlete> friends = service.getFriendsOfAthlete(USER_ID_1);

        assertEquals(1, friends.size());
        assertEquals(friend, friends.get(0));
    }

    @Test
    void getFriendsOfAthlete_shouldReturnFriendsWhenAthleteIsRecipient() {
        Athlete athlete = createAthlete(USER_ID_1);
        Athlete friend = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, friend, athlete, FriendshipStatus.ACCEPTED);

        when(friendshipRepository.findByAthleteAndStatus(USER_ID_1, FriendshipStatus.ACCEPTED))
                .thenReturn(List.of(f));

        List<Athlete> friends = service.getFriendsOfAthlete(USER_ID_1);

        assertEquals(1, friends.size());
        assertEquals(friend, friends.get(0));
    }

    @Test
    void getFriendsOfAthlete_shouldReturnEmptyListWhenNoFriends() {
        when(friendshipRepository.findByAthleteAndStatus(USER_ID_1, FriendshipStatus.ACCEPTED))
                .thenReturn(List.of());

        List<Athlete> friends = service.getFriendsOfAthlete(USER_ID_1);

        assertTrue(friends.isEmpty());
    }

    @Test
    void getFriendsOfAthlete_shouldReturnMultipleFriends() {
        Athlete athlete = createAthlete(USER_ID_1);
        Athlete friend2 = createAthlete(USER_ID_2);
        Athlete friend3 = createAthlete(USER_ID_3);
        Friendship f1 = createFriendship(10, athlete, friend2, FriendshipStatus.ACCEPTED);
        Friendship f2 = createFriendship(11, friend3, athlete, FriendshipStatus.ACCEPTED);

        when(friendshipRepository.findByAthleteAndStatus(USER_ID_1, FriendshipStatus.ACCEPTED))
                .thenReturn(List.of(f1, f2));

        List<Athlete> friends = service.getFriendsOfAthlete(USER_ID_1);

        assertEquals(2, friends.size());
        assertTrue(friends.contains(friend2));
        assertTrue(friends.contains(friend3));
    }

    // ==================== 6. getPendingRequestsForAthlete ====================

    @Test
    void getPendingRequestsForAthlete_shouldThrowWhenAthleteNotFound() {
        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getPendingRequestsForAthlete(USER_ID_1));
        assertEquals("Athlete not found", ex.getMessage());
    }

    @Test
    void getPendingRequestsForAthlete_shouldReturnPendingRequests() {
        Athlete athlete = createAthlete(USER_ID_1);
        Athlete sender = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, sender, athlete, FriendshipStatus.PENDING);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(athlete));
        when(friendshipRepository.findByRecipientAndStatus(athlete, FriendshipStatus.PENDING))
                .thenReturn(List.of(f));

        List<Friendship> result = service.getPendingRequestsForAthlete(USER_ID_1);

        assertEquals(1, result.size());
        assertEquals(f, result.get(0));
    }

    @Test
    void getPendingRequestsForAthlete_shouldReturnEmptyListWhenNone() {
        Athlete athlete = createAthlete(USER_ID_1);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(athlete));
        when(friendshipRepository.findByRecipientAndStatus(athlete, FriendshipStatus.PENDING))
                .thenReturn(List.of());

        List<Friendship> result = service.getPendingRequestsForAthlete(USER_ID_1);

        assertTrue(result.isEmpty());
    }

    // ==================== 7. getSentPendingRequests ====================

    @Test
    void getSentPendingRequests_shouldThrowWhenAthleteNotFound() {
        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getSentPendingRequests(USER_ID_1));
        assertEquals("Athlete not found", ex.getMessage());
    }

    @Test
    void getSentPendingRequests_shouldReturnSentRequests() {
        Athlete athlete = createAthlete(USER_ID_1);
        Athlete recipient = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, athlete, recipient, FriendshipStatus.PENDING);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(athlete));
        when(friendshipRepository.findByInitiatorAndStatus(athlete, FriendshipStatus.PENDING))
                .thenReturn(List.of(f));

        List<Friendship> result = service.getSentPendingRequests(USER_ID_1);

        assertEquals(1, result.size());
        assertEquals(f, result.get(0));
    }

    @Test
    void getSentPendingRequests_shouldReturnEmptyListWhenNone() {
        Athlete athlete = createAthlete(USER_ID_1);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(athlete));
        when(friendshipRepository.findByInitiatorAndStatus(athlete, FriendshipStatus.PENDING))
                .thenReturn(List.of());

        List<Friendship> result = service.getSentPendingRequests(USER_ID_1);

        assertTrue(result.isEmpty());
    }

    // ==================== sendFriendRequest - block checks ====================

    @Test
    void sendFriendRequest_shouldThrowWhenInitiatorBlockedRecipient() {
        Athlete initiator = createAthlete(USER_ID_1);
        Athlete recipient = createAthlete(USER_ID_2);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(initiator));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(recipient));
        when(friendshipRepository.existsBlock(initiator, recipient)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.sendFriendRequest(USER_ID_1, USER_ID_2));
        assertEquals("You have blocked this user. Unblock them first.", ex.getMessage());
    }

    @Test
    void sendFriendRequest_shouldThrowWhenRecipientBlockedInitiator() {
        Athlete initiator = createAthlete(USER_ID_1);
        Athlete recipient = createAthlete(USER_ID_2);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(initiator));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(recipient));
        when(friendshipRepository.existsBlock(initiator, recipient)).thenReturn(false);
        when(friendshipRepository.existsBlock(recipient, initiator)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.sendFriendRequest(USER_ID_1, USER_ID_2));
        assertEquals("Cannot send friend request to this user.", ex.getMessage());
    }

    @Test
    void sendFriendRequest_shouldThrowWhenExistingBlockRelationship() {
        Athlete initiator = createAthlete(USER_ID_1);
        Athlete recipient = createAthlete(USER_ID_2);
        Friendship existing = createFriendship(FRIENDSHIP_ID, initiator, recipient, FriendshipStatus.BLOCKED);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(initiator));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(recipient));
        when(friendshipRepository.existsBlock(initiator, recipient)).thenReturn(false);
        when(friendshipRepository.existsBlock(recipient, initiator)).thenReturn(false);
        when(friendshipRepository.findBetweenAthletes(initiator, recipient)).thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.sendFriendRequest(USER_ID_1, USER_ID_2));
        assertEquals("A block relationship exists between you and this user", ex.getMessage());
    }

    // ==================== 8. blockUser ====================

    @Test
    void blockUser_shouldThrowWhenBlockingSelf() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.blockUser(USER_ID_1, USER_ID_1));
        assertEquals("You cannot block yourself", ex.getMessage());
    }

    @Test
    void blockUser_shouldThrowWhenBlockerNotFound() {
        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.blockUser(USER_ID_1, USER_ID_2));
        assertEquals("Blocker not found", ex.getMessage());
    }

    @Test
    void blockUser_shouldThrowWhenBlockedNotFound() {
        Athlete blocker = createAthlete(USER_ID_1);
        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(blocker));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.blockUser(USER_ID_1, USER_ID_2));
        assertEquals("User to block not found", ex.getMessage());
    }

    @Test
    void blockUser_shouldThrowWhenAlreadyBlockedBySameUser() {
        Athlete blocker = createAthlete(USER_ID_1);
        Athlete blocked = createAthlete(USER_ID_2);
        Friendship existing = createFriendship(FRIENDSHIP_ID, blocker, blocked, FriendshipStatus.BLOCKED);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(blocker));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(blocked));
        when(friendshipRepository.findBetweenAthletes(blocker, blocked)).thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.blockUser(USER_ID_1, USER_ID_2));
        assertEquals("You have already blocked this user", ex.getMessage());
    }

    @Test
    void blockUser_shouldReverseBlockWhenOtherUserBlockedUs() {
        Athlete blocker = createAthlete(USER_ID_1);
        Athlete blocked = createAthlete(USER_ID_2);
        // blocked previously blocked blocker (initiator=blocked, recipient=blocker)
        Friendship existing = createFriendship(FRIENDSHIP_ID, blocked, blocker, FriendshipStatus.BLOCKED);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(blocker));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(blocked));
        when(friendshipRepository.findBetweenAthletes(blocker, blocked)).thenReturn(Optional.of(existing));

        service.blockUser(USER_ID_1, USER_ID_2);

        assertEquals(blocker, existing.getInitiator());
        assertEquals(blocked, existing.getRecipient());
        assertEquals(FriendshipStatus.BLOCKED, existing.getStatus());
        verify(friendshipRepository).save(existing);
    }

    @Test
    void blockUser_shouldConvertAcceptedToBlocked() {
        Athlete blocker = createAthlete(USER_ID_1);
        Athlete blocked = createAthlete(USER_ID_2);
        Friendship existing = createFriendship(FRIENDSHIP_ID, blocker, blocked, FriendshipStatus.ACCEPTED);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(blocker));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(blocked));
        when(friendshipRepository.findBetweenAthletes(blocker, blocked)).thenReturn(Optional.of(existing));

        service.blockUser(USER_ID_1, USER_ID_2);

        assertEquals(FriendshipStatus.BLOCKED, existing.getStatus());
        assertEquals(blocker, existing.getInitiator());
        assertEquals(blocked, existing.getRecipient());
        verify(friendshipRepository).save(existing);
    }

    @Test
    void blockUser_shouldConvertPendingToBlocked() {
        Athlete blocker = createAthlete(USER_ID_1);
        Athlete blocked = createAthlete(USER_ID_2);
        Friendship existing = createFriendship(FRIENDSHIP_ID, blocked, blocker, FriendshipStatus.PENDING);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(blocker));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(blocked));
        when(friendshipRepository.findBetweenAthletes(blocker, blocked)).thenReturn(Optional.of(existing));

        service.blockUser(USER_ID_1, USER_ID_2);

        assertEquals(FriendshipStatus.BLOCKED, existing.getStatus());
        assertEquals(blocker, existing.getInitiator());
        assertEquals(blocked, existing.getRecipient());
        verify(friendshipRepository).save(existing);
    }

    @Test
    void blockUser_shouldConvertRejectedToBlocked() {
        Athlete blocker = createAthlete(USER_ID_1);
        Athlete blocked = createAthlete(USER_ID_2);
        Friendship existing = createFriendship(FRIENDSHIP_ID, blocker, blocked, FriendshipStatus.REJECTED);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(blocker));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(blocked));
        when(friendshipRepository.findBetweenAthletes(blocker, blocked)).thenReturn(Optional.of(existing));

        service.blockUser(USER_ID_1, USER_ID_2);

        assertEquals(FriendshipStatus.BLOCKED, existing.getStatus());
        verify(friendshipRepository).save(existing);
    }

    @Test
    void blockUser_shouldCreateNewBlockedWhenNoExisting() {
        Athlete blocker = createAthlete(USER_ID_1);
        Athlete blocked = createAthlete(USER_ID_2);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(blocker));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(blocked));
        when(friendshipRepository.findBetweenAthletes(blocker, blocked)).thenReturn(Optional.empty());

        service.blockUser(USER_ID_1, USER_ID_2);

        verify(friendshipRepository).save(any(Friendship.class));
    }

    // ==================== 9. unblockUser ====================

    @Test
    void unblockUser_shouldThrowWhenUnblockingSelf() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.unblockUser(USER_ID_1, USER_ID_1));
        assertEquals("Invalid operation", ex.getMessage());
    }

    @Test
    void unblockUser_shouldThrowWhenBlockerNotFound() {
        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.unblockUser(USER_ID_1, USER_ID_2));
        assertEquals("Blocker not found", ex.getMessage());
    }

    @Test
    void unblockUser_shouldThrowWhenNoRelationshipExists() {
        Athlete blocker = createAthlete(USER_ID_1);
        Athlete blocked = createAthlete(USER_ID_2);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(blocker));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(blocked));
        when(friendshipRepository.findBetweenAthletes(blocker, blocked)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.unblockUser(USER_ID_1, USER_ID_2));
        assertEquals("No relationship exists between these users", ex.getMessage());
    }

    @Test
    void unblockUser_shouldThrowWhenNotBlocked() {
        Athlete blocker = createAthlete(USER_ID_1);
        Athlete blocked = createAthlete(USER_ID_2);
        Friendship existing = createFriendship(FRIENDSHIP_ID, blocker, blocked, FriendshipStatus.ACCEPTED);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(blocker));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(blocked));
        when(friendshipRepository.findBetweenAthletes(blocker, blocked)).thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.unblockUser(USER_ID_1, USER_ID_2));
        assertEquals("This user is not blocked", ex.getMessage());
    }

    @Test
    void unblockUser_shouldThrowWhenNotTheBlocker() {
        Athlete blocker = createAthlete(USER_ID_1);
        Athlete blocked = createAthlete(USER_ID_2);
        // blocked blocked blocker (initiator=blocked)
        Friendship existing = createFriendship(FRIENDSHIP_ID, blocked, blocker, FriendshipStatus.BLOCKED);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(blocker));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(blocked));
        when(friendshipRepository.findBetweenAthletes(blocker, blocked)).thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.unblockUser(USER_ID_1, USER_ID_2));
        assertEquals("You did not block this user, so you cannot unblock them", ex.getMessage());
    }

    @Test
    void unblockUser_shouldSucceedWhenBlockerUnblocks() {
        Athlete blocker = createAthlete(USER_ID_1);
        Athlete blocked = createAthlete(USER_ID_2);
        Friendship existing = createFriendship(FRIENDSHIP_ID, blocker, blocked, FriendshipStatus.BLOCKED);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(blocker));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(blocked));
        when(friendshipRepository.findBetweenAthletes(blocker, blocked)).thenReturn(Optional.of(existing));

        service.unblockUser(USER_ID_1, USER_ID_2);

        verify(friendshipRepository).delete(existing);
    }

    // ==================== 10. getBlockedUsers ====================

    @Test
    void getBlockedUsers_shouldThrowWhenAthleteNotFound() {
        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getBlockedUsers(USER_ID_1));
        assertEquals("Athlete not found", ex.getMessage());
    }

    @Test
    void getBlockedUsers_shouldReturnBlockedUsers() {
        Athlete athlete = createAthlete(USER_ID_1);
        Athlete blocked = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, athlete, blocked, FriendshipStatus.BLOCKED);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(athlete));
        when(friendshipRepository.findByInitiatorAndStatus(athlete, FriendshipStatus.BLOCKED))
                .thenReturn(List.of(f));

        List<Friendship> result = service.getBlockedUsers(USER_ID_1);

        assertEquals(1, result.size());
        assertEquals(f, result.get(0));
    }

    @Test
    void getBlockedUsers_shouldReturnEmptyList() {
        Athlete athlete = createAthlete(USER_ID_1);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(athlete));
        when(friendshipRepository.findByInitiatorAndStatus(athlete, FriendshipStatus.BLOCKED))
                .thenReturn(List.of());

        List<Friendship> result = service.getBlockedUsers(USER_ID_1);

        assertTrue(result.isEmpty());
    }

    // ==================== 11. getRelationshipStatus ====================

    @Test
    void getRelationshipStatus_shouldReturnSELF() {
        RelationshipStatusDTO result = service.getRelationshipStatus(USER_ID_1, USER_ID_1);
        assertEquals(RelationshipStatusDTO.SELF, result);
    }

    @Test
    void getRelationshipStatus_shouldThrowWhenViewerNotFound() {
        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.getRelationshipStatus(USER_ID_1, USER_ID_2));
    }

    @Test
    void getRelationshipStatus_shouldReturnMutuallyBlocked() {
        Athlete viewer = createAthlete(USER_ID_1);
        Athlete target = createAthlete(USER_ID_2);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(viewer));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(target));
        when(friendshipRepository.existsBlock(viewer, target)).thenReturn(true);
        when(friendshipRepository.existsBlock(target, viewer)).thenReturn(true);

        RelationshipStatusDTO result = service.getRelationshipStatus(USER_ID_1, USER_ID_2);
        assertEquals(RelationshipStatusDTO.MUTUALLY_BLOCKED, result);
    }

    @Test
    void getRelationshipStatus_shouldReturnBlockedByMe() {
        Athlete viewer = createAthlete(USER_ID_1);
        Athlete target = createAthlete(USER_ID_2);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(viewer));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(target));
        when(friendshipRepository.existsBlock(viewer, target)).thenReturn(true);
        when(friendshipRepository.existsBlock(target, viewer)).thenReturn(false);

        RelationshipStatusDTO result = service.getRelationshipStatus(USER_ID_1, USER_ID_2);
        assertEquals(RelationshipStatusDTO.BLOCKED_BY_ME, result);
    }

    @Test
    void getRelationshipStatus_shouldReturnBlockedMe() {
        Athlete viewer = createAthlete(USER_ID_1);
        Athlete target = createAthlete(USER_ID_2);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(viewer));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(target));
        when(friendshipRepository.existsBlock(viewer, target)).thenReturn(false);
        when(friendshipRepository.existsBlock(target, viewer)).thenReturn(true);

        RelationshipStatusDTO result = service.getRelationshipStatus(USER_ID_1, USER_ID_2);
        assertEquals(RelationshipStatusDTO.BLOCKED_ME, result);
    }

    @Test
    void getRelationshipStatus_shouldReturnNoneWhenNoFriendship() {
        Athlete viewer = createAthlete(USER_ID_1);
        Athlete target = createAthlete(USER_ID_2);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(viewer));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(target));
        when(friendshipRepository.existsBlock(viewer, target)).thenReturn(false);
        when(friendshipRepository.existsBlock(target, viewer)).thenReturn(false);
        when(friendshipRepository.findBetweenAthletes(viewer, target)).thenReturn(Optional.empty());

        RelationshipStatusDTO result = service.getRelationshipStatus(USER_ID_1, USER_ID_2);
        assertEquals(RelationshipStatusDTO.NONE, result);
    }

    @Test
    void getRelationshipStatus_shouldReturnFriends() {
        Athlete viewer = createAthlete(USER_ID_1);
        Athlete target = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, viewer, target, FriendshipStatus.ACCEPTED);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(viewer));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(target));
        when(friendshipRepository.existsBlock(viewer, target)).thenReturn(false);
        when(friendshipRepository.existsBlock(target, viewer)).thenReturn(false);
        when(friendshipRepository.findBetweenAthletes(viewer, target)).thenReturn(Optional.of(f));

        RelationshipStatusDTO result = service.getRelationshipStatus(USER_ID_1, USER_ID_2);
        assertEquals(RelationshipStatusDTO.FRIENDS, result);
    }

    @Test
    void getRelationshipStatus_shouldReturnRequestSent() {
        Athlete viewer = createAthlete(USER_ID_1);
        Athlete target = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, viewer, target, FriendshipStatus.PENDING);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(viewer));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(target));
        when(friendshipRepository.existsBlock(viewer, target)).thenReturn(false);
        when(friendshipRepository.existsBlock(target, viewer)).thenReturn(false);
        when(friendshipRepository.findBetweenAthletes(viewer, target)).thenReturn(Optional.of(f));

        RelationshipStatusDTO result = service.getRelationshipStatus(USER_ID_1, USER_ID_2);
        assertEquals(RelationshipStatusDTO.REQUEST_SENT, result);
    }

    @Test
    void getRelationshipStatus_shouldReturnRequestReceived() {
        Athlete viewer = createAthlete(USER_ID_1);
        Athlete target = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, target, viewer, FriendshipStatus.PENDING);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(viewer));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(target));
        when(friendshipRepository.existsBlock(viewer, target)).thenReturn(false);
        when(friendshipRepository.existsBlock(target, viewer)).thenReturn(false);
        when(friendshipRepository.findBetweenAthletes(viewer, target)).thenReturn(Optional.of(f));

        RelationshipStatusDTO result = service.getRelationshipStatus(USER_ID_1, USER_ID_2);
        assertEquals(RelationshipStatusDTO.REQUEST_RECEIVED, result);
    }

    @Test
    void getRelationshipStatus_shouldReturnRejected() {
        Athlete viewer = createAthlete(USER_ID_1);
        Athlete target = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, viewer, target, FriendshipStatus.REJECTED);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(viewer));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(target));
        when(friendshipRepository.existsBlock(viewer, target)).thenReturn(false);
        when(friendshipRepository.existsBlock(target, viewer)).thenReturn(false);
        when(friendshipRepository.findBetweenAthletes(viewer, target)).thenReturn(Optional.of(f));

        RelationshipStatusDTO result = service.getRelationshipStatus(USER_ID_1, USER_ID_2);
        assertEquals(RelationshipStatusDTO.REJECTED, result);
    }

    @Test
    void getRelationshipStatus_shouldReturnBlockedByMeFromFriendship() {
        Athlete viewer = createAthlete(USER_ID_1);
        Athlete target = createAthlete(USER_ID_2);
        // BLOCKED via friendship record but existsBlock returned false (edge case)
        Friendship f = createFriendship(FRIENDSHIP_ID, viewer, target, FriendshipStatus.BLOCKED);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(viewer));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(target));
        when(friendshipRepository.existsBlock(viewer, target)).thenReturn(false);
        when(friendshipRepository.existsBlock(target, viewer)).thenReturn(false);
        when(friendshipRepository.findBetweenAthletes(viewer, target)).thenReturn(Optional.of(f));

        RelationshipStatusDTO result = service.getRelationshipStatus(USER_ID_1, USER_ID_2);
        assertEquals(RelationshipStatusDTO.BLOCKED_BY_ME, result);
    }

    @Test
    void getRelationshipStatus_shouldReturnBlockedMeFromFriendship() {
        Athlete viewer = createAthlete(USER_ID_1);
        Athlete target = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, target, viewer, FriendshipStatus.BLOCKED);

        when(athleteRepository.findById(USER_ID_1)).thenReturn(Optional.of(viewer));
        when(athleteRepository.findById(USER_ID_2)).thenReturn(Optional.of(target));
        when(friendshipRepository.existsBlock(viewer, target)).thenReturn(false);
        when(friendshipRepository.existsBlock(target, viewer)).thenReturn(false);
        when(friendshipRepository.findBetweenAthletes(viewer, target)).thenReturn(Optional.of(f));

        RelationshipStatusDTO result = service.getRelationshipStatus(USER_ID_1, USER_ID_2);
        assertEquals(RelationshipStatusDTO.BLOCKED_ME, result);
    }

    // ==================== 12. searchVisibleAthletes ====================

    @Test
    void searchVisibleAthletes_shouldFilterSelf() {
        Athlete athlete = createAthlete(USER_ID_1);
        Athlete other = createAthlete(USER_ID_2);

        when(athleteRepository.findByUsernameContainingIgnoreCase("user")).thenReturn(List.of(athlete, other));
        when(friendshipRepository.findBlockedUserIds(USER_ID_1)).thenReturn(List.of());
        when(friendshipRepository.findBlockedByUserIds(USER_ID_1)).thenReturn(List.of());

        List<Athlete> result = service.searchVisibleAthletes(USER_ID_1, "user");

        assertEquals(1, result.size());
        assertEquals(other, result.get(0));
    }

    @Test
    void searchVisibleAthletes_shouldFilterBlockedByMe() {
        Athlete athlete = createAthlete(USER_ID_1);
        Athlete other = createAthlete(USER_ID_2);
        Athlete blocked = createAthlete(USER_ID_3);

        when(athleteRepository.findByUsernameContainingIgnoreCase("user")).thenReturn(List.of(other, blocked));
        when(friendshipRepository.findBlockedUserIds(USER_ID_1)).thenReturn(List.of(USER_ID_3));
        when(friendshipRepository.findBlockedByUserIds(USER_ID_1)).thenReturn(List.of());

        List<Athlete> result = service.searchVisibleAthletes(USER_ID_1, "user");

        assertEquals(1, result.size());
        assertEquals(other, result.get(0));
    }

    @Test
    void searchVisibleAthletes_shouldFilterBlockedMe() {
        Athlete athlete = createAthlete(USER_ID_1);
        Athlete other = createAthlete(USER_ID_2);
        Athlete blocker = createAthlete(USER_ID_3);

        when(athleteRepository.findByUsernameContainingIgnoreCase("user")).thenReturn(List.of(other, blocker));
        when(friendshipRepository.findBlockedUserIds(USER_ID_1)).thenReturn(List.of());
        when(friendshipRepository.findBlockedByUserIds(USER_ID_1)).thenReturn(List.of(USER_ID_3));

        List<Athlete> result = service.searchVisibleAthletes(USER_ID_1, "user");

        assertEquals(1, result.size());
        assertEquals(other, result.get(0));
    }

    @Test
    void searchVisibleAthletes_shouldReturnAllWhenNoKeyword() {
        Athlete athlete = createAthlete(USER_ID_1);
        Athlete other = createAthlete(USER_ID_2);

        when(athleteRepository.findAll()).thenReturn(List.of(athlete, other));
        when(friendshipRepository.findBlockedUserIds(USER_ID_1)).thenReturn(List.of());
        when(friendshipRepository.findBlockedByUserIds(USER_ID_1)).thenReturn(List.of());

        List<Athlete> result = service.searchVisibleAthletes(USER_ID_1, null);

        assertEquals(1, result.size());
        assertEquals(other, result.get(0));
    }

    @Test
    void searchVisibleAthletes_shouldReturnAllWhenEmptyKeyword() {
        Athlete athlete = createAthlete(USER_ID_1);
        Athlete other = createAthlete(USER_ID_2);

        when(athleteRepository.findAll()).thenReturn(List.of(athlete, other));
        when(friendshipRepository.findBlockedUserIds(USER_ID_1)).thenReturn(List.of());
        when(friendshipRepository.findBlockedByUserIds(USER_ID_1)).thenReturn(List.of());

        List<Athlete> result = service.searchVisibleAthletes(USER_ID_1, "");

        assertEquals(1, result.size());
        assertEquals(other, result.get(0));
    }
}
