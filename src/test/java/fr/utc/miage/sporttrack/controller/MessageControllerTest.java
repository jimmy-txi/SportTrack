package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.user.communication.Message;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.service.user.communication.FriendshipService;
import fr.utc.miage.sporttrack.service.user.communication.MessageService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MessageController controller;

    private Athlete current;
    private Athlete friend;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        current = createAthlete(1, "me@mail.com", "me");
        friend = createAthlete(2, "friend@mail.com", "friend");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void inbox_shouldRedirectToLoginWhenNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(null);

        String result = controller.inbox(null, session, model, redirectAttributes);

        assertEquals("redirect:/login", result);
    }

    @Test
    void inbox_shouldLoadConversationForSelectedFriend() {
        Message message = new Message();
        message.setInitiator(current);
        message.setRecipient(friend);
        message.setContent("hello");

        when(session.getAttribute("athlete")).thenReturn(current);
        when(friendshipService.getFriendsOfAthlete(1)).thenReturn(List.of(friend));
        when(messageService.getLatestMessageByFriend(1)).thenReturn(Map.of(2, message));
        when(messageService.getUnreadCountByFriend(1)).thenReturn(Map.of(2, 1));
        when(messageService.getConversation(1, 2)).thenReturn(List.of(message));
        when(messageService.countUnreadMessages(1)).thenReturn(1L);

        String result = controller.inbox(2, session, model, redirectAttributes);

        assertEquals("athlete/friend/messages", result);
        verify(model).addAttribute("selectedFriend", friend);
        verify(model).addAttribute("conversation", List.of(message));
    }

    @Test
    void inbox_shouldRedirectWhenSelectedUserIsNotFriend() {
        Athlete stranger = createAthlete(3, "stranger@mail.com", "stranger");

        when(session.getAttribute("athlete")).thenReturn(current);
        when(friendshipService.getFriendsOfAthlete(1)).thenReturn(List.of(friend));
        when(messageService.getLatestMessageByFriend(1)).thenReturn(Map.of());
        when(messageService.getUnreadCountByFriend(1)).thenReturn(Map.of());

        String result = controller.inbox(stranger.getId(), session, model, redirectAttributes);

        assertEquals("redirect:/messages", result);
        verify(redirectAttributes).addFlashAttribute("error", "Vous ne pouvez discuter qu'avec vos amis.");
        verify(messageService, never()).getConversation(any(), any());
    }

    @Test
    void sendMessage_shouldCallServiceAndRedirectToConversation() {
        when(session.getAttribute("athlete")).thenReturn(current);

        String result = controller.sendMessage(2, "bonjour", session, redirectAttributes);

        assertEquals("redirect:/messages?friendId=2", result);
        verify(messageService).sendMessage(1, 2, "bonjour");
    }

    @Test
    void sendMessage_shouldAddFlashErrorOnFailure() {
        when(session.getAttribute("athlete")).thenReturn(current);
        when(messageService.sendMessage(1, 2, "")).thenThrow(new IllegalArgumentException("Message content cannot be empty"));

        String result = controller.sendMessage(2, "", session, redirectAttributes);

        assertEquals("redirect:/messages?friendId=2", result);
        verify(redirectAttributes).addFlashAttribute("error", "Message content cannot be empty");
    }

    private Athlete createAthlete(Integer id, String email, String username) {
        Athlete athlete = new Athlete();
        athlete.setEmail(email);
        athlete.setUsername(username);
        athlete.setPassword("pwd");
        try {
            Field idField = athlete.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(athlete, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return athlete;
    }
}
