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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeaderModelAdviceTest {

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private HttpSession session;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private HeaderModelAdvice advice;

    private Athlete athlete;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        athlete = createAthlete(2, "advice", "advice@mail.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void athlete_shouldReturnSessionAthlete() {
        when(session.getAttribute("athlete")).thenReturn(athlete);

        Athlete result = advice.athlete(session);

        assertSame(athlete, result);
        verifyNoInteractions(athleteRepository);
    }

    @Test
    void athlete_shouldResolveFromSecurityContextWhenSessionIsEmpty() {
        when(session.getAttribute("athlete")).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("advice@mail.com");
        when(athleteRepository.findByEmail("advice@mail.com")).thenReturn(Optional.of(athlete));

        Athlete result = advice.athlete(session);

        assertSame(athlete, result);
        verify(session).setAttribute("athlete", athlete);
    }

    @Test
    void notificationUnreadCount_shouldReturnZeroWhenNoAthlete() {
        when(session.getAttribute("athlete")).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(null);

        long result = advice.notificationUnreadCount(session);

        assertEquals(0L, result);
        verifyNoInteractions(notificationService);
    }

    @Test
    void notificationUnreadCount_shouldDelegateWhenAthleteExists() {
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(notificationService.countUnreadNotifications(2)).thenReturn(5L);

        long result = advice.notificationUnreadCount(session);

        assertEquals(5L, result);
        verify(notificationService).countUnreadNotifications(2);
    }

    @Test
    void recentNotifications_shouldReturnEmptyWhenNoAthlete() {
        when(session.getAttribute("athlete")).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(null);

        List<Notification> result = advice.recentNotifications(session);

        assertEquals(List.of(), result);
        verifyNoInteractions(notificationService);
    }

    @Test
    void recentNotifications_shouldDelegateWhenAthleteExists() {
        Notification notification = new Notification();
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(notificationService.getRecentNotifications(2)).thenReturn(List.of(notification));

        List<Notification> result = advice.recentNotifications(session);

        assertEquals(1, result.size());
        verify(notificationService).getRecentNotifications(2);
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
