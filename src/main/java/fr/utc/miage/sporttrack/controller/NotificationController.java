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

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private static final String ATHLETE_ATTRIBUTE = "athlete";
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_NOTIFICATIONS = "redirect:/notifications";

    private final NotificationService notificationService;
    private final AthleteRepository athleteRepository;

    public NotificationController(NotificationService notificationService, AthleteRepository athleteRepository) {
        this.notificationService = notificationService;
        this.athleteRepository = athleteRepository;
    }

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

    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable("id") Integer id, HttpSession session) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return REDIRECT_LOGIN;
        }

        notificationService.markAsRead(id, athlete.getId());
        return REDIRECT_NOTIFICATIONS;
    }

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
