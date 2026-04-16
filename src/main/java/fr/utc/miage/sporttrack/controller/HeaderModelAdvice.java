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

@ControllerAdvice
public class HeaderModelAdvice {

    private static final String ATHLETE_ATTRIBUTE = "athlete";

    private final AthleteRepository athleteRepository;
    private final NotificationService notificationService;

    public HeaderModelAdvice(AthleteRepository athleteRepository, NotificationService notificationService) {
        this.athleteRepository = athleteRepository;
        this.notificationService = notificationService;
    }

    @ModelAttribute("athlete")
    public Athlete athlete(HttpSession session) {
        return resolveAthlete(session);
    }

    @ModelAttribute("notificationUnreadCount")
    public long notificationUnreadCount(HttpSession session) {
        Athlete athlete = resolveAthlete(session);
        return athlete == null || athlete.getId() == null
                ? 0L
                : notificationService.countUnreadNotifications(athlete.getId());
    }

    @ModelAttribute("recentNotifications")
    public List<Notification> recentNotifications(HttpSession session) {
        Athlete athlete = resolveAthlete(session);
        return athlete == null || athlete.getId() == null
                ? List.of()
                : notificationService.getRecentNotifications(athlete.getId());
    }

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
