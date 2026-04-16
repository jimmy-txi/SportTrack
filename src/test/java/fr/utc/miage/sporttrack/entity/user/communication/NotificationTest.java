package fr.utc.miage.sporttrack.entity.user.communication;

import fr.utc.miage.sporttrack.entity.enumeration.NotificationType;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class NotificationTest {

    @Test
    void shouldSetAndGetAllFields() {
        Notification notification = new Notification();
        Athlete recipient = new Athlete();
        Athlete actor = new Athlete();
        LocalDateTime now = LocalDateTime.now();

        notification.setId(1);
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setType(NotificationType.FRIEND_REQUEST);
        notification.setTitle("Titre");
        notification.setContent("Contenu");
        notification.setTargetUrl("/friends");
        notification.setSeen(true);
        notification.setCreatedAt(now);

        assertEquals(1, notification.getId());
        assertSame(recipient, notification.getRecipient());
        assertSame(actor, notification.getActor());
        assertEquals(NotificationType.FRIEND_REQUEST, notification.getType());
        assertEquals("Titre", notification.getTitle());
        assertEquals("Contenu", notification.getContent());
        assertEquals("/friends", notification.getTargetUrl());
        assertEquals(true, notification.isSeen());
        assertEquals(now, notification.getCreatedAt());
    }

    @Test
    void onCreate_shouldInitializeCreatedAtWhenNull() {
        Notification notification = new Notification();

        notification.onCreate();

        assertNotNull(notification.getCreatedAt());
    }

    @Test
    void onCreate_shouldNotOverrideExistingCreatedAt() {
        Notification notification = new Notification();
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 16, 10, 0);
        notification.setCreatedAt(createdAt);

        notification.onCreate();

        assertEquals(createdAt, notification.getCreatedAt());
    }
}
