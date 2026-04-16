package fr.utc.miage.sporttrack.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.utc.miage.sporttrack.entity.event.Badge;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.service.event.BadgeService;
import jakarta.servlet.http.HttpSession;

/**
 * Spring MVC controller for athlete badge views.
 *
 * <p>Provides endpoints for viewing the current athlete's earned and unearned badges,
 * as well as viewing badges of other athletes.</p>
 *
 * @author SportTrack Team
 */
@Controller
@RequestMapping("/badges")
public class BadgeController {

    /** Service for badge queries. */
    private final BadgeService badgeService;

    /** Repository for athlete lookups. */
    private final AthleteRepository athleteRepository;

    /**
     * Constructs a {@code BadgeController} with the required dependencies.
     *
     * @param badgeService       the badge service
     * @param athleteRepository  the athlete repository
     */
    public BadgeController(BadgeService badgeService, AthleteRepository athleteRepository) {
        this.badgeService = badgeService;
        this.athleteRepository = athleteRepository;
    }

    /**
     * Displays all badges for the authenticated athlete, split into earned and unearned.
     *
     * @param session the HTTP session for athlete resolution
     * @param model   the Spring MVC model
     * @return the view name "athlete/badge/list", or a redirect to login
     */
    @GetMapping
    public String listMyBadges(HttpSession session, Model model) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return ControllerConstants.REDIRECT_LOGIN;
        }

        List<Badge> earned = badgeService.getEarnedBadges(athlete.getId());
        List<Badge> unearned = badgeService.getUnearnedBadges(athlete.getId());

        model.addAttribute("earned", earned);
        model.addAttribute("unearned", unearned);
        return ControllerConstants.ATHLETE_BADGE_LIST_VIEW;
    }

    /**
     * Displays the earned badges of a specific athlete.
     *
     * @param id      the identifier of the target athlete
     * @param session the HTTP session for current user resolution
     * @param model   the Spring MVC model
     * @return the view name "athlete/badge/list", or a redirect on failure
     */
    @GetMapping("/athlete/{id}")
    public String listAthleteBadges(@PathVariable int id, HttpSession session, Model model) {
        Athlete currentUser = getAuthenticatedAthlete(session);
        if (currentUser == null) {
            return ControllerConstants.REDIRECT_LOGIN;
        }

        Optional<Athlete> targetAthlete = athleteRepository.findById(id);
        if (targetAthlete.isEmpty()) {
            return ControllerConstants.REDIRECT_ATHLETE_LIST;
        }

        List<Badge> earned = badgeService.getEarnedBadges(id);
        model.addAttribute("targetAthlete", targetAthlete.get());
        model.addAttribute("earned", earned);
        return ControllerConstants.ATHLETE_BADGE_LIST_VIEW;
    }

    /**
     * Resolves the currently authenticated athlete from the session or security context.
     *
     * @param session the HTTP session
     * @return the authenticated athlete, or {@code null} if not available
     */
    private Athlete getAuthenticatedAthlete(HttpSession session) {
        Athlete athlete = (Athlete) session.getAttribute(ControllerConstants.ATHLETE_ATTR);
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
            session.setAttribute(ControllerConstants.ATHLETE_ATTR, athlete);
        }

        return athlete;
    }
}