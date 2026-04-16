package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.enumeration.InteractionType;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.service.activity.ActivityService;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import fr.utc.miage.sporttrack.service.user.communication.CommentService;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @Mock
    private ActivityService activityService;

    @Mock
    private AthleteService athleteService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CommentController commentController;

    private Athlete mockAthlete;
    private Activity mockActivity;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);

        mockAthlete = new Athlete();
        mockAthlete.setUsername("testUser");

        mockActivity = new Activity();
        mockActivity.setId(10);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRedirectToLoginWhenNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);

        String viewName = commentController.addComment(10, "Hello", "LIKE", "/custom-redirect");

        assertEquals("redirect:/login", viewName);
    }

    @Test
    void shouldRedirectToLoginWhenAuthenticationNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        String viewName = commentController.addComment(10, "Hello", "LIKE", "/custom-redirect");

        assertEquals("redirect:/login", viewName);
    }

    @Test
    void shouldRedirectWithErrorWhenActivityNotFound() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@mail.com");

        when(athleteService.getCurrentAthlete("test@mail.com")).thenReturn(mockAthlete);
        when(activityService.findById(10)).thenReturn(Optional.empty());

        String viewName = commentController.addComment(10, "Hello", "LIKE", "/custom-redirect");

        assertEquals("redirect:/custom-redirect?error=ActivityNotFound", viewName);
    }

    @Test
    void shouldAddCommentSuccessfullyWithValidInteractionType() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@mail.com");

        when(athleteService.getCurrentAthlete("test@mail.com")).thenReturn(mockAthlete);
        when(activityService.findById(10)).thenReturn(Optional.of(mockActivity));

        String viewName = commentController.addComment(10, "Great run!", "CHEER", "/athlete/activities");

        verify(commentService).addComment(mockAthlete, mockActivity, "Great run!", InteractionType.CHEER);
        assertEquals("redirect:/athlete/activities", viewName);
    }

    @Test
    void shouldAddCommentWithNoneWhenInteractionTypeIsInvalid() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@mail.com");

        when(athleteService.getCurrentAthlete("test@mail.com")).thenReturn(mockAthlete);
        when(activityService.findById(10)).thenReturn(Optional.of(mockActivity));

        String viewName = commentController.addComment(10, "Hmm", "INVALID_TYPE", "/friends/profile/5");

        verify(commentService).addComment(mockAthlete, mockActivity, "Hmm", InteractionType.NONE);
        assertEquals("redirect:/friends/profile/5", viewName);
    }
}
