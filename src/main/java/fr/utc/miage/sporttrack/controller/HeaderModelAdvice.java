package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.user.communication.Notification;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.service.user.communication.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.List;
import java.util.Optional;

/**
 * Controller advice that populates the Spring MVC model with common header
 * attributes for all views in the SportTrack application.
 *
 * <p>Automatically resolves the currently authenticated athlete from the
 * HTTP session or Spring Security context and makes the athlete profile,
 * unread notification count, and recent notifications available to every
 * Thymeleaf template via {@code @ModelAttribute} methods.</p>
 *
 * @author SportTrack Team
 */
@ControllerAdvice
public class HeaderModelAdvice {

    /** Session attribute key used to cache the resolved athlete entity. */
    private static final String ATHLETE_ATTRIBUTE = "athlete";

    /** The repository used for looking up athlete accounts. */
    private final AthleteRepository athleteRepository;

    /** The service used for querying notification data. */
    private final NotificationService notificationService;

    /**
     * Constructs a {@code HeaderModelAdvice} with the required dependencies.
     *
     * @param athleteRepository   the repository for athlete data access
     * @param notificationService the service for notification queries
     */
    public HeaderModelAdvice(AthleteRepository athleteRepository, NotificationService notificationService) {
        this.athleteRepository = athleteRepository;
        this.notificationService = notificationService;
    }

    /**
     * Makes the currently authenticated athlete available to all views
     * as a model attribute named {@code "athlete"}.
     *
     * @param session the HTTP session used for athlete caching
     * @return the authenticated athlete, or {@code null} if not logged in
     */
    @ModelAttribute("athlete")
    public Athlete athlete(HttpSession session) {
        return resolveAthlete(session);
    }

    /**
     * Computes the number of unread notifications for the current athlete
     * and makes it available to all views as {@code "notificationUnreadCount"}.
     *
     * @param session the HTTP session used for athlete resolution
     * @return the count of unread notifications, or {@code 0} if not authenticated
     */
    @ModelAttribute("notificationUnreadCount")
    public long notificationUnreadCount(HttpSession session) {
        Athlete athlete = resolveAthlete(session);
        return athlete == null || athlete.getId() == null
                ? 0L
                : notificationService.countUnreadNotifications(athlete.getId());
    }

    /**
     * Retrieves the ten most recent notifications for the current athlete
     * and makes them available to all views as {@code "recentNotifications"}.
     *
     * @param session the HTTP session used for athlete resolution
     * @return a list of recent notifications, or an empty list if not authenticated
     */
    @ModelAttribute("recentNotifications")
    public List<Notification> recentNotifications(HttpSession session) {
        Athlete athlete = resolveAthlete(session);
        return athlete == null || athlete.getId() == null
                ? List.of()
                : notificationService.getRecentNotifications(athlete.getId());
    }

    /**
     * Resolves the currently authenticated athlete by first checking the
     * HTTP session cache, then falling back to the Spring Security context.
     * The resolved athlete is cached in the session for subsequent requests.
     *
     * @param session the HTTP session used for caching the athlete
     * @return the authenticated athlete, or {@code null} if not authenticated
     *         or if the principal is not an athlete
     */
    private Athlete resolveAthlete(HttpSession session) {
        Athlete athlete = (Athlete) session.getAttribute(ATHLETE_ATTRIBUTE);
        if (athlete != null) {
            return athlete;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Optional<Athlete> athleteOptional = athleteRepository.findByEmail(authentication.getName());
        if (athleteOptional.isPresent()) {
            athlete = athleteOptional.get();
            session.setAttribute(ATHLETE_ATTRIBUTE, athlete);
        }

        return athlete;
    }
}
