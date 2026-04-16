package fr.utc.miage.sporttrack.service.user.communication;

import fr.utc.miage.sporttrack.entity.enumeration.FriendshipStatus;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.user.communication.Friendship;
import fr.utc.miage.sporttrack.entity.user.communication.Message;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.repository.user.communication.FriendshipRepository;
import fr.utc.miage.sporttrack.repository.user.communication.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private MessageService service;

    @Test
    void sendMessage_shouldPersistMessageWhenUsersAreFriends() {
        Athlete sender = createAthlete(1);
        Athlete recipient = createAthlete(2);
        Friendship friendship = createFriendship(sender, recipient, FriendshipStatus.ACCEPTED);

        when(athleteRepository.findById(1)).thenReturn(Optional.of(sender));
        when(athleteRepository.findById(2)).thenReturn(Optional.of(recipient));
        when(friendshipRepository.findBetweenAthletes(sender, recipient)).thenReturn(Optional.of(friendship));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Message result = service.sendMessage(1, 2, " Salut ");

        assertEquals(sender, result.getInitiator());
        assertEquals(recipient, result.getRecipient());
        assertEquals("Salut", result.getContent());
        assertNotNull(result.getSentAt());
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void sendMessage_shouldThrowWhenNotFriends() {
        Athlete sender = createAthlete(1);
        Athlete recipient = createAthlete(2);

        when(athleteRepository.findById(1)).thenReturn(Optional.of(sender));
        when(athleteRepository.findById(2)).thenReturn(Optional.of(recipient));
        when(friendshipRepository.findBetweenAthletes(sender, recipient)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> service.sendMessage(1, 2, "Hello"));
    }

    @Test
    void getConversation_shouldMarkAsSeenAndReturnMessages() {
        Athlete current = createAthlete(1);
        Athlete friend = createAthlete(2);
        Friendship friendship = createFriendship(current, friend, FriendshipStatus.ACCEPTED);
        Message message = new Message();
        message.setInitiator(friend);
        message.setRecipient(current);
        message.setContent("Hello");

        when(athleteRepository.findById(1)).thenReturn(Optional.of(current));
        when(athleteRepository.findById(2)).thenReturn(Optional.of(friend));
        when(friendshipRepository.findBetweenAthletes(current, friend)).thenReturn(Optional.of(friendship));
        when(messageRepository.findConversation(1, 2)).thenReturn(List.of(message));

        List<Message> result = service.getConversation(1, 2);

        assertEquals(1, result.size());
        verify(messageRepository).markConversationAsSeen(1, 2);
        verify(messageRepository).findConversation(1, 2);
    }

    @Test
    void getLatestMessageByFriend_shouldReturnOneMessagePerFriend() {
        Athlete me = createAthlete(1);
        Athlete friendA = createAthlete(2);
        Athlete friendB = createAthlete(3);

        Message latestA = new Message();
        latestA.setInitiator(me);
        latestA.setRecipient(friendA);
        latestA.setContent("new A");

        Message olderA = new Message();
        olderA.setInitiator(friendA);
        olderA.setRecipient(me);
        olderA.setContent("old A");

        Message latestB = new Message();
        latestB.setInitiator(friendB);
        latestB.setRecipient(me);
        latestB.setContent("new B");

        when(messageRepository.findByInitiatorIdOrRecipientIdOrderBySentAtDesc(1, 1))
                .thenReturn(List.of(latestA, latestB, olderA));

        Map<Integer, Message> result = service.getLatestMessageByFriend(1);

        assertEquals(2, result.size());
        assertEquals("new A", result.get(2).getContent());
        assertEquals("new B", result.get(3).getContent());
    }

    @Test
    void getUnreadCountByFriend_shouldCountOnlyIncomingUnseenMessages() {
        Athlete me = createAthlete(1);
        Athlete friend = createAthlete(2);

        Message unseen1 = new Message();
        unseen1.setInitiator(friend);
        unseen1.setRecipient(me);
        unseen1.setSeen(false);

        Message unseen2 = new Message();
        unseen2.setInitiator(friend);
        unseen2.setRecipient(me);
        unseen2.setSeen(false);

        Message seen = new Message();
        seen.setInitiator(friend);
        seen.setRecipient(me);
        seen.setSeen(true);

        Message outgoing = new Message();
        outgoing.setInitiator(me);
        outgoing.setRecipient(friend);
        outgoing.setSeen(false);

        when(messageRepository.findByInitiatorIdOrRecipientIdOrderBySentAtDesc(1, 1))
                .thenReturn(List.of(unseen1, unseen2, seen, outgoing));

        Map<Integer, Integer> result = service.getUnreadCountByFriend(1);

        assertEquals(1, result.size());
        assertEquals(2, result.get(2));
    }

    private Athlete createAthlete(Integer id) {
        Athlete athlete = new Athlete();
        athlete.setUsername("user" + id);
        athlete.setPassword("pass" + id);
        athlete.setEmail("user" + id + "@mail.com");
        try {
            var field = athlete.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(athlete, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return athlete;
    }

    private Friendship createFriendship(Athlete first, Athlete second, FriendshipStatus status) {
        Friendship friendship = new Friendship(first, second);
        friendship.setStatus(status);
        return friendship;
    }
}
