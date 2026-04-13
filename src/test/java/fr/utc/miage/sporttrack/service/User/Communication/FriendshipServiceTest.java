package fr.utc.miage.sporttrack.service.User.Communication;

import fr.utc.miage.sporttrack.entity.Enumeration.FriendshipStatus;
import fr.utc.miage.sporttrack.entity.User.Athlete;
import fr.utc.miage.sporttrack.entity.User.Communication.Friendship;
import fr.utc.miage.sporttrack.repository.User.AthleteRepository;
import fr.utc.miage.sporttrack.repository.User.Communication.FriendshipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
            var field = a.getClass().getSuperclass().getDeclaredField("idU");
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
        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.sendFriendRequest(USER_ID_1, USER_ID_2));
        assertEquals("Initiator not found", ex.getMessage());
    }

    @Test
    void sendFriendRequest_shouldThrowWhenRecipientNotFound() {
        Athlete initiator = createAthlete(USER_ID_1);
        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.of(initiator));
        when(athleteRepository.findByIdU(USER_ID_2)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.sendFriendRequest(USER_ID_1, USER_ID_2));
        assertEquals("Recipient not found", ex.getMessage());
    }

    @Test
    void sendFriendRequest_shouldThrowWhenPendingAlreadyExists() {
        Athlete initiator = createAthlete(USER_ID_1);
        Athlete recipient = createAthlete(USER_ID_2);
        Friendship existing = createFriendship(FRIENDSHIP_ID, initiator, recipient, FriendshipStatus.PENDING);

        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.of(initiator));
        when(athleteRepository.findByIdU(USER_ID_2)).thenReturn(Optional.of(recipient));
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

        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.of(initiator));
        when(athleteRepository.findByIdU(USER_ID_2)).thenReturn(Optional.of(recipient));
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

        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.of(initiator));
        when(athleteRepository.findByIdU(USER_ID_2)).thenReturn(Optional.of(recipient));
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

        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.of(initiator));
        when(athleteRepository.findByIdU(USER_ID_2)).thenReturn(Optional.of(recipient));
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
        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.removeFriend(USER_ID_1, USER_ID_2));
        assertEquals("Current user not found", ex.getMessage());
    }

    @Test
    void removeFriend_shouldThrowWhenOtherUserNotFound() {
        Athlete currentUser = createAthlete(USER_ID_1);
        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.of(currentUser));
        when(athleteRepository.findByIdU(USER_ID_2)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.removeFriend(USER_ID_1, USER_ID_2));
        assertEquals("Other user not found", ex.getMessage());
    }

    @Test
    void removeFriend_shouldThrowWhenFriendshipDoesNotExist() {
        Athlete currentUser = createAthlete(USER_ID_1);
        Athlete otherUser = createAthlete(USER_ID_2);

        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.of(currentUser));
        when(athleteRepository.findByIdU(USER_ID_2)).thenReturn(Optional.of(otherUser));
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

        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.of(currentUser));
        when(athleteRepository.findByIdU(USER_ID_2)).thenReturn(Optional.of(otherUser));
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

        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.of(currentUser));
        when(athleteRepository.findByIdU(USER_ID_2)).thenReturn(Optional.of(otherUser));
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
        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getPendingRequestsForAthlete(USER_ID_1));
        assertEquals("Athlete not found", ex.getMessage());
    }

    @Test
    void getPendingRequestsForAthlete_shouldReturnPendingRequests() {
        Athlete athlete = createAthlete(USER_ID_1);
        Athlete sender = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, sender, athlete, FriendshipStatus.PENDING);

        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.of(athlete));
        when(friendshipRepository.findByRecipientAndStatus(athlete, FriendshipStatus.PENDING))
                .thenReturn(List.of(f));

        List<Friendship> result = service.getPendingRequestsForAthlete(USER_ID_1);

        assertEquals(1, result.size());
        assertEquals(f, result.get(0));
    }

    @Test
    void getPendingRequestsForAthlete_shouldReturnEmptyListWhenNone() {
        Athlete athlete = createAthlete(USER_ID_1);

        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.of(athlete));
        when(friendshipRepository.findByRecipientAndStatus(athlete, FriendshipStatus.PENDING))
                .thenReturn(List.of());

        List<Friendship> result = service.getPendingRequestsForAthlete(USER_ID_1);

        assertTrue(result.isEmpty());
    }

    // ==================== 7. getSentPendingRequests ====================

    @Test
    void getSentPendingRequests_shouldThrowWhenAthleteNotFound() {
        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getSentPendingRequests(USER_ID_1));
        assertEquals("Athlete not found", ex.getMessage());
    }

    @Test
    void getSentPendingRequests_shouldReturnSentRequests() {
        Athlete athlete = createAthlete(USER_ID_1);
        Athlete recipient = createAthlete(USER_ID_2);
        Friendship f = createFriendship(FRIENDSHIP_ID, athlete, recipient, FriendshipStatus.PENDING);

        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.of(athlete));
        when(friendshipRepository.findByInitiatorAndStatus(athlete, FriendshipStatus.PENDING))
                .thenReturn(List.of(f));

        List<Friendship> result = service.getSentPendingRequests(USER_ID_1);

        assertEquals(1, result.size());
        assertEquals(f, result.get(0));
    }

    @Test
    void getSentPendingRequests_shouldReturnEmptyListWhenNone() {
        Athlete athlete = createAthlete(USER_ID_1);

        when(athleteRepository.findByIdU(USER_ID_1)).thenReturn(Optional.of(athlete));
        when(friendshipRepository.findByInitiatorAndStatus(athlete, FriendshipStatus.PENDING))
                .thenReturn(List.of());

        List<Friendship> result = service.getSentPendingRequests(USER_ID_1);

        assertTrue(result.isEmpty());
    }
}
