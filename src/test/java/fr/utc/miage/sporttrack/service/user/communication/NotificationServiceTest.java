package fr.utc.miage.sporttrack.service.user.communication;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.enumeration.NotificationType;
import fr.utc.miage.sporttrack.entity.event.Badge;
import fr.utc.miage.sporttrack.entity.event.Challenge;
import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.user.communication.Friendship;
import fr.utc.miage.sporttrack.entity.user.communication.Message;
import fr.utc.miage.sporttrack.entity.user.communication.Notification;
import fr.utc.miage.sporttrack.repository.user.communication.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService service;

    @Test
    void createNotification_shouldReturnNullWhenRecipientIsInvalid() {
        Notification result = service.createNotification(null, null, NotificationType.MESSAGE_RECEIVED,
                "Titre", "Contenu", "/messages");

        assertNull(result);
        verifyNoInteractions(notificationRepository);
    }

    @Test
    void createNotification_shouldSaveNotificationWhenRecipientIsValid() {
        Athlete recipient = createAthlete(1, "recipient", "recipient@mail.com");
        Athlete actor = createAthlete(2, "actor", "actor@mail.com");
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = service.createNotification(recipient, actor, NotificationType.MESSAGE_RECEIVED,
                "Nouveau message", "Message reçu", "/messages");

        assertNotNull(result);
        assertSame(recipient, result.getRecipient());
        assertSame(actor, result.getActor());
        assertEquals(NotificationType.MESSAGE_RECEIVED, result.getType());
        assertEquals("Nouveau message", result.getTitle());
        assertEquals("Message reçu", result.getContent());
        assertEquals("/messages", result.getTargetUrl());
        assertFalse(result.isSeen());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getNotificationsForAthlete_shouldHandleNullAthleteId() {
        assertEquals(List.of(), service.getNotificationsForAthlete(null));
        verifyNoInteractions(notificationRepository);
    }

    @Test
    void getNotificationsForAthlete_shouldDelegateToRepository() {
        Notification notification = new Notification();
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(3)).thenReturn(List.of(notification));

        List<Notification> result = service.getNotificationsForAthlete(3);

        assertEquals(1, result.size());
        verify(notificationRepository).findByRecipientIdOrderByCreatedAtDesc(3);
    }

    @Test
    void getNotificationForRecipient_shouldReturnNullWhenArgumentsAreInvalid() {
        assertNull(service.getNotificationForRecipient(null, 1));
        assertNull(service.getNotificationForRecipient(1, null));
        verifyNoInteractions(notificationRepository);
    }

    @Test
    void getNotificationForRecipient_shouldReturnNotificationWhenFound() {
        Notification expected = new Notification();
        when(notificationRepository.findByIdAndRecipientId(5, 9)).thenReturn(Optional.of(expected));

        Notification result = service.getNotificationForRecipient(5, 9);

        assertSame(expected, result);
        verify(notificationRepository).findByIdAndRecipientId(5, 9);
    }

    @Test
    void countUnreadNotifications_shouldReturnZeroWhenAthleteIdIsNull() {
        assertEquals(0L, service.countUnreadNotifications(null));
        verifyNoInteractions(notificationRepository);
    }

    @Test
    void countUnreadNotifications_shouldDelegateToRepository() {
        when(notificationRepository.countByRecipientIdAndSeenFalse(7)).thenReturn(4L);

        long result = service.countUnreadNotifications(7);

        assertEquals(4L, result);
        verify(notificationRepository).countByRecipientIdAndSeenFalse(7);
    }

    @Test
    void markAsRead_shouldDoNothingWhenArgumentsAreInvalid() {
        service.markAsRead(null, 1);
        service.markAsRead(1, null);
        verifyNoInteractions(notificationRepository);
    }

    @Test
    void markAsRead_shouldThrowWhenNotificationIsMissing() {
        when(notificationRepository.findByIdAndRecipientId(4, 2)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.markAsRead(4, 2));
        verify(notificationRepository).findByIdAndRecipientId(4, 2);
    }

    @Test
    void markAsRead_shouldPersistSeenState() {
        Notification notification = new Notification();
        notification.setSeen(false);
        when(notificationRepository.findByIdAndRecipientId(4, 2)).thenReturn(Optional.of(notification));

        service.markAsRead(4, 2);

        assertEquals(true, notification.isSeen());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAllAsRead_shouldCallRepositoryOnlyWhenRecipientExists() {
        service.markAllAsRead(null);
        verify(notificationRepository, never()).markAllAsSeen(any());

        service.markAllAsRead(12);
        verify(notificationRepository).markAllAsSeen(12);
    }

    @Test
    void notifyMessageReceived_shouldCreateNotification() {
        Athlete sender = createAthlete(10, "alice", "alice@mail.com");
        sender.setFirstName("Alice");
        sender.setLastName("Martin");
        Athlete recipient = createAthlete(11, "bob", "bob@mail.com");

        Message message = new Message();
        message.setInitiator(sender);
        message.setRecipient(recipient);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.notifyMessageReceived(message);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification created = captor.getValue();
        assertEquals(NotificationType.MESSAGE_RECEIVED, created.getType());
        assertEquals("Nouveau message", created.getTitle());
        assertEquals("Alice Martin vous a envoyé un message.", created.getContent());
        assertEquals("/messages", created.getTargetUrl());
    }

    @Test
    void notifyFriendRequest_shouldCreateNotification() {
        Athlete initiator = createAthlete(20, "initiator", "init@mail.com");
        Athlete recipient = createAthlete(21, "recipient", "rec@mail.com");

        Friendship friendship = new Friendship();
        friendship.setInitiator(initiator);
        friendship.setRecipient(recipient);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.notifyFriendRequest(friendship);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification created = captor.getValue();
        assertEquals(NotificationType.FRIEND_REQUEST, created.getType());
        assertEquals("/friends?tab=requests", created.getTargetUrl());
    }

    @Test
    void notifyFriendRequestAccepted_shouldCreateNotification() {
        Athlete initiator = createAthlete(30, "initiator", "init2@mail.com");
        Athlete recipient = createAthlete(31, "recipient", "rec2@mail.com");
        recipient.setUsername("laura");

        Friendship friendship = new Friendship();
        friendship.setInitiator(initiator);
        friendship.setRecipient(recipient);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.notifyFriendRequestAccepted(friendship);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification created = captor.getValue();
        assertEquals(NotificationType.FRIEND_REQUEST_ACCEPTED, created.getType());
        assertEquals("laura a accepté votre demande d'ami.", created.getContent());
    }

    @Test
    void notifyActivityPublished_shouldSkipActorAndInvalidRecipients() {
        Athlete actor = createAthlete(40, "actor", "actor2@mail.com");
        Athlete friend = createAthlete(41, "friend", "friend@mail.com");
        Athlete invalid = new Athlete();
        invalid.setUsername("invalid");
        invalid.setPassword("pwd");
        invalid.setEmail("invalid@mail.com");

        Activity activity = new Activity();
        activity.setTitle("Sortie footing");

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.notifyActivityPublished(actor, activity, Arrays.asList(actor, friend, null, invalid));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());
        Notification created = captor.getValue();
        assertSame(friend, created.getRecipient());
        assertEquals(NotificationType.FRIEND_ACTIVITY, created.getType());
        assertEquals("/friends/activities", created.getTargetUrl());
    }

    @Test
    void notifyBadgeEarned_shouldCreateNotification() {
        Athlete athlete = createAthlete(50, "badgeuser", "badge@mail.com");
        Badge badge = new Badge();
        badge.setLabel("Marathonien");

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.notifyBadgeEarned(athlete, badge);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification created = captor.getValue();
        assertEquals(NotificationType.BADGE_EARNED, created.getType());
        assertEquals("Vous avez gagné le badge Marathonien.", created.getContent());
    }

    @Test
    void notifyObjectiveCompleted_shouldCreateNotification() {
        Athlete athlete = createAthlete(60, "obj", "obj@mail.com");
        Objective objective = new Objective("10 km", "objectif");

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.notifyObjectiveCompleted(athlete, objective);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification created = captor.getValue();
        assertEquals(NotificationType.OBJECTIVE_COMPLETED, created.getType());
        assertEquals("Vous avez validé l'objectif 10 km.", created.getContent());
    }

    @Test
    void notifyChallengeEnded_shouldNotifyOrganizerAndParticipants() {
        Athlete organizer = createAthlete(70, "orga", "orga@mail.com");
        Athlete participant = createAthlete(71, "part", "part@mail.com");

        Challenge challenge = new Challenge();
        challenge.setName("Défi du mois");
        challenge.setOrganizer(organizer);
        challenge.setParticipants(List.of(participant));

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.notifyChallengeEnded(challenge);

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    private Athlete createAthlete(int id, String username, String email) {
        Athlete athlete = new Athlete();
        athlete.setUsername(username);
        athlete.setPassword("pwd");
        athlete.setEmail(email);
        setUserId(athlete, id);
        return athlete;
    }

    private void setUserId(Athlete athlete, int id) {
        try {
            Field field = athlete.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(athlete, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
