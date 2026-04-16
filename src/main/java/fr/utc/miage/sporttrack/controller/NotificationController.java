package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.user.communication.Notification;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.service.user.communication.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Spring MVC controller for notification management.
 *
 * <p>Provides endpoints for listing, opening, and marking notifications
 * as read for the authenticated athlete.</p>
 *
 * @author SportTrack Team
 */
@Controller
@RequestMapping("/notifications")
public class NotificationController {

    /** Session attribute key for the cached athlete. */
    private static final String ATHLETE_ATTRIBUTE = "athlete";

    /** Redirect constant for the login page. */
    private static final String REDIRECT_LOGIN = "redirect:/login";

    /** Redirect constant for the notification list. */
    private static final String REDIRECT_NOTIFICATIONS = "redirect:/notifications";

    /** Service for notification operations. */
    private final NotificationService notificationService;

    /** Repository for athlete authentication resolution. */
    private final AthleteRepository athleteRepository;

    /**
     * Constructs a {@code NotificationController} with the required dependencies.
     *
     * @param notificationService the notification service
     * @param athleteRepository   the athlete repository
     */
    public NotificationController(NotificationService notificationService, AthleteRepository athleteRepository) {
        this.notificationService = notificationService;
        this.athleteRepository = athleteRepository;
    }

    /**
     * Lists all notifications for the authenticated athlete with an unread count.
     *
     * @param session the HTTP session for athlete resolution
     * @param model   the Spring MVC model
     * @return the view name "notification/list", or a redirect to login
     */
    @GetMapping
    public String listNotifications(HttpSession session, Model model) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return REDIRECT_LOGIN;
        }

        List<Notification> notifications = notificationService.getNotificationsForAthlete(athlete.getId());
        model.addAttribute(ATHLETE_ATTRIBUTE, athlete);
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", notificationService.countUnreadNotifications(athlete.getId()));
        return "notification/list";
    }

    /**
     * Opens a notification, marking it as read and redirecting to its target URL.
     *
     * @param id      the notification identifier
     * @param session the HTTP session for athlete resolution
     * @return a redirect to the notification target URL or the notification list
     */
    @GetMapping("/open/{id}")
    public String openNotification(@PathVariable("id") Integer id, HttpSession session) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return REDIRECT_LOGIN;
        }

        Notification notification = notificationService.getNotificationForRecipient(id, athlete.getId());
        if (notification == null) {
            return REDIRECT_NOTIFICATIONS;
        }

        if (!notification.isSeen()) {
            notificationService.markAsRead(notification.getId(), athlete.getId());
        }

        String targetUrl = notification.getTargetUrl();
        if (targetUrl == null || targetUrl.isBlank() || !targetUrl.startsWith("/") || targetUrl.startsWith("//")) {
            return REDIRECT_NOTIFICATIONS;
        }

        return "redirect:" + targetUrl;
    }

    /**
     * Marks a single notification as read.
     *
     * @param id      the notification identifier
     * @param session the HTTP session for athlete resolution
     * @return a redirect to the notification list
     */
    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable("id") Integer id, HttpSession session) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return REDIRECT_LOGIN;
        }

        notificationService.markAsRead(id, athlete.getId());
        return REDIRECT_NOTIFICATIONS;
    }

    /**
     * Marks all notifications as read for the authenticated athlete.
     *
     * @param session             the HTTP session for athlete resolution
     * @param redirectAttributes  flash attributes for success messaging
     * @return a redirect to the notification list
     */
    @PostMapping("/read-all")
    public String markAllAsRead(HttpSession session, RedirectAttributes redirectAttributes) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return REDIRECT_LOGIN;
        }

        notificationService.markAllAsRead(athlete.getId());
        redirectAttributes.addFlashAttribute("success", "Toutes les notifications ont été marquées comme lues.");
        return REDIRECT_NOTIFICATIONS;
    }

    /**
     * Resolves the currently authenticated athlete from the session or security context.
     *
     * @param session the HTTP session
     * @return the authenticated athlete, or {@code null} if not available
     */
    private Athlete getAuthenticatedAthlete(HttpSession session) {
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
