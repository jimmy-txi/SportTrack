package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.user.communication.Notification;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.service.user.communication.NotificationService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

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
    private NotificationController controller;

    private Athlete athlete;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        athlete = createAthlete(1, "jimmy", "jimmy@mail.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listNotifications_shouldRedirectWhenNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(null);

        String result = controller.listNotifications(session, model);

        assertEquals("redirect:/login", result);
    }

    @Test
    void listNotifications_shouldPopulateModelWhenAuthenticated() {
        Notification notification = new Notification();
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(notificationService.getNotificationsForAthlete(1)).thenReturn(List.of(notification));
        when(notificationService.countUnreadNotifications(1)).thenReturn(3L);

        String result = controller.listNotifications(session, model);

        assertEquals("notification/list", result);
        verify(model).addAttribute("athlete", athlete);
        verify(model).addAttribute("notifications", List.of(notification));
        verify(model).addAttribute("unreadCount", 3L);
    }

    @Test
    void openNotification_shouldRedirectToLoginWhenNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(null);

        String result = controller.openNotification(10, session);

        assertEquals("redirect:/login", result);
    }

    @Test
    void openNotification_shouldRedirectToNotificationsWhenNotFound() {
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(notificationService.getNotificationForRecipient(10, 1)).thenReturn(null);

        String result = controller.openNotification(10, session);

        assertEquals("redirect:/notifications", result);
    }

    @Test
    void openNotification_shouldMarkAsReadAndRedirectToTarget() {
        Notification notification = new Notification();
        notification.setId(10);
        notification.setSeen(false);
        notification.setTargetUrl("/messages");

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(notificationService.getNotificationForRecipient(10, 1)).thenReturn(notification);

        String result = controller.openNotification(10, session);

        assertEquals("redirect:/messages", result);
        verify(notificationService).markAsRead(10, 1);
    }

    @Test
    void openNotification_shouldNotMarkReadWhenAlreadySeen() {
        Notification notification = new Notification();
        notification.setId(11);
        notification.setSeen(true);
        notification.setTargetUrl("/badges");

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(notificationService.getNotificationForRecipient(11, 1)).thenReturn(notification);

        String result = controller.openNotification(11, session);

        assertEquals("redirect:/badges", result);
        verify(notificationService, never()).markAsRead(11, 1);
    }

    @Test
    void openNotification_shouldRejectUnsafeTargetUrl() {
        Notification notification = new Notification();
        notification.setId(12);
        notification.setSeen(false);
        notification.setTargetUrl("//evil.example.com");

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(notificationService.getNotificationForRecipient(12, 1)).thenReturn(notification);

        String result = controller.openNotification(12, session);

        assertEquals("redirect:/notifications", result);
    }

    @Test
    void markAsRead_shouldDelegateToService() {
        when(session.getAttribute("athlete")).thenReturn(athlete);

        String result = controller.markAsRead(15, session);

        assertEquals("redirect:/notifications", result);
        verify(notificationService).markAsRead(15, 1);
    }

    @Test
    void markAllAsRead_shouldDelegateToServiceAndSetFlash() {
        when(session.getAttribute("athlete")).thenReturn(athlete);

        String result = controller.markAllAsRead(session, redirectAttributes);

        assertEquals("redirect:/notifications", result);
        verify(notificationService).markAllAsRead(1);
        verify(redirectAttributes).addFlashAttribute("success", "Toutes les notifications ont été marquées comme lues.");
    }

    private Athlete createAthlete(int id, String username, String email) {
        Athlete created = new Athlete();
        created.setUsername(username);
        created.setPassword("pwd");
        created.setEmail(email);
        try {
            Field field = created.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(created, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return created;
    }
}
