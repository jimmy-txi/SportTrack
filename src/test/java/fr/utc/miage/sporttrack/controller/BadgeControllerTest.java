package fr.utc.miage.sporttrack.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import fr.utc.miage.sporttrack.entity.event.Badge;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.service.event.BadgeService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

@ExtendWith(MockitoExtension.class)
class BadgeControllerTest {

    @Mock
    private BadgeService badgeService;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private Model model;

    @Mock
    private HttpSession session;

    @InjectMocks
    private BadgeController controller;

    private Athlete athlete;

    @BeforeEach
    void setUp() {
        athlete = new Athlete();
        athlete.setEmail("test@mail.com");
    }

    // ========== listMyBadges ==========

    @Test
    void testListMyBadgesAuthenticatedViaSession() {
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(badgeService.getEarnedBadges(any())).thenReturn(Collections.emptyList());
        when(badgeService.getUnearnedBadges(any())).thenReturn(Collections.emptyList());

        String viewName = controller.listMyBadges(session, model);

        assertEquals("athlete/badge/list", viewName);
        verify(model).addAttribute("earned", Collections.emptyList());
        verify(model).addAttribute("unearned", Collections.emptyList());
    }

    @Test
    void testListMyBadgesNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);

        String viewName = controller.listMyBadges(session, model);

        assertEquals("redirect:/login", viewName);
        verifyNoInteractions(badgeService);
    }

    @Test
    void testListMyBadgesWithEarnedBadges() {
        Badge earnedBadge = new Badge();
        earnedBadge.setId(1);
        earnedBadge.setLabel("Test Badge");

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(badgeService.getEarnedBadges(any())).thenReturn(List.of(earnedBadge));
        when(badgeService.getUnearnedBadges(any())).thenReturn(Collections.emptyList());

        String viewName = controller.listMyBadges(session, model);

        assertEquals("athlete/badge/list", viewName);
        verify(model).addAttribute("earned", List.of(earnedBadge));
    }

    // ========== listAthleteBadges ==========

    @Test
    void testListAthleteBadgesAuthenticated() {
        Athlete targetAthlete = new Athlete();
        targetAthlete.setEmail("target@mail.com");

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(athleteRepository.findById(2)).thenReturn(Optional.of(targetAthlete));
        when(badgeService.getEarnedBadges(2)).thenReturn(Collections.emptyList());

        String viewName = controller.listAthleteBadges(2, session, model);

        assertEquals("athlete/badge/list", viewName);
        verify(model).addAttribute("targetAthlete", targetAthlete);
        verify(model).addAttribute("earned", Collections.emptyList());
    }

    @Test
    void testListAthleteBadgesNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);

        String viewName = controller.listAthleteBadges(2, session, model);

        assertEquals("redirect:/login", viewName);
        verifyNoInteractions(badgeService);
        verifyNoInteractions(athleteRepository);
    }

    @Test
    void testListAthleteBadgesTargetNotFound() {
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(athleteRepository.findById(99)).thenReturn(Optional.empty());

        String viewName = controller.listAthleteBadges(99, session, model);

        assertEquals("redirect:/athlete/list", viewName);
        verifyNoInteractions(badgeService);
    }

    @Test
    void testListAthleteBadgesWithEarnedBadges() {
        Athlete targetAthlete = new Athlete();
        targetAthlete.setEmail("target@mail.com");

        Badge badge = new Badge();
        badge.setId(1);
        badge.setLabel("Target Badge");

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(athleteRepository.findById(2)).thenReturn(Optional.of(targetAthlete));
        when(badgeService.getEarnedBadges(2)).thenReturn(List.of(badge));

        String viewName = controller.listAthleteBadges(2, session, model);

        assertEquals("athlete/badge/list", viewName);
        verify(model).addAttribute("earned", List.of(badge));
    }

    // ========== SecurityContext fallback tests ==========

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testListMyBadgesViaSecurityContextAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);
        when(athleteRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(athlete));
        when(badgeService.getEarnedBadges(any())).thenReturn(Collections.emptyList());
        when(badgeService.getUnearnedBadges(any())).thenReturn(Collections.emptyList());

        // Set up SecurityContext with authenticated user
        TestingAuthenticationToken auth = new TestingAuthenticationToken("test@mail.com", "password", "ROLE_USER");
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        String viewName = controller.listMyBadges(session, model);

        assertEquals("athlete/badge/list", viewName);
        verify(session).setAttribute("athlete", athlete);
    }

    @Test
    void testListMyBadgesViaSecurityContextNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);

        // Set up SecurityContext with anonymous authentication
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
                "key", "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        String viewName = controller.listMyBadges(session, model);

        assertEquals("redirect:/login", viewName);
    }

    @Test
    void testListMyBadgesViaSecurityContextNullAuthentication() {
        when(session.getAttribute("athlete")).thenReturn(null);

        // No authentication in SecurityContext (already null by default)
        SecurityContextHolder.clearContext();

        String viewName = controller.listMyBadges(session, model);

        assertEquals("redirect:/login", viewName);
    }

    @Test
    void testListMyBadgesViaSecurityContextUserNotFound() {
        when(session.getAttribute("athlete")).thenReturn(null);
        when(athleteRepository.findByEmail("unknown@mail.com")).thenReturn(Optional.empty());

        TestingAuthenticationToken auth = new TestingAuthenticationToken("unknown@mail.com", "password", "ROLE_USER");
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        String viewName = controller.listMyBadges(session, model);

        assertEquals("redirect:/login", viewName);
    }

    @Test
    void testListAthleteBadgesViaSecurityContext() {
        Athlete targetAthlete = new Athlete();
        targetAthlete.setEmail("target@mail.com");

        when(session.getAttribute("athlete")).thenReturn(null);
        when(athleteRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(athlete));
        when(athleteRepository.findById(2)).thenReturn(Optional.of(targetAthlete));
        when(badgeService.getEarnedBadges(2)).thenReturn(Collections.emptyList());

        TestingAuthenticationToken auth = new TestingAuthenticationToken("test@mail.com", "password", "ROLE_USER");
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        String viewName = controller.listAthleteBadges(2, session, model);

        assertEquals("athlete/badge/list", viewName);
    }
}
