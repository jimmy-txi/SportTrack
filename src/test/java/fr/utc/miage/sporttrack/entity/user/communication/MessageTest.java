package fr.utc.miage.sporttrack.entity.user.communication;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageTest {

    @Test
    void shouldCreateMessageWithDefaultConstructor() {
        Message message = new Message();
        assertNotNull(message);
        assertFalse(message.isSeen());
    }

    @Test
    void shouldExposeExpectedFieldStructure() throws Exception {
        Field initiator = Message.class.getDeclaredField("initiator");
        Field recipient = Message.class.getDeclaredField("recipient");
        Field content = Message.class.getDeclaredField("content");
        Field seen = Message.class.getDeclaredField("seen");
        Field sentAt = Message.class.getDeclaredField("sentAt");

        assertEquals(Athlete.class, initiator.getType());
        assertEquals(Athlete.class, recipient.getType());
        assertEquals(String.class, content.getType());
        assertEquals(boolean.class, seen.getType());
        assertEquals(LocalDateTime.class, sentAt.getType());

        Message message = new Message();
        seen.setAccessible(true);
        assertFalse(seen.getBoolean(message));
    }

    @Test
    void shouldSetAndGetFields() {
        Message message = new Message();
        Athlete initiator = new Athlete();
        Athlete recipient = new Athlete();
        LocalDateTime now = LocalDateTime.now();

        message.setInitiator(initiator);
        message.setRecipient(recipient);
        message.setContent("Bonjour");
        message.setSentAt(now);
        message.setSeen(true);

        assertSame(initiator, message.getInitiator());
        assertSame(recipient, message.getRecipient());
        assertEquals("Bonjour", message.getContent());
        assertEquals(now, message.getSentAt());
        assertTrue(message.isSeen());
    }

    @Test
    void shouldSetSentAtOnCreateWhenNull() {
        Message message = new Message();

        message.onCreate();

        assertNotNull(message.getSentAt());
    }
}
