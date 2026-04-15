package fr.utc.miage.sporttrack.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.entity.event.Badge;
import fr.utc.miage.sporttrack.repository.activity.SportRepository;
import fr.utc.miage.sporttrack.service.activity.SportService;
import fr.utc.miage.sporttrack.service.event.BadgeService;
import fr.utc.miage.sporttrack.service.user.AdminService;

@Controller
@RequestMapping("/admin/badges")
public class AdminBadgeController {

    private final BadgeService badgeService;
    private final SportService sportService;
    private final AdminService adminService;
    private final SportRepository sportRepository;

    public AdminBadgeController(BadgeService badgeService,
                                SportService sportService,
                                AdminService adminService,
                                SportRepository sportRepository) {
        this.badgeService = badgeService;
        this.sportService = sportService;
        this.adminService = adminService;
        this.sportRepository = sportRepository;
    }

    @GetMapping
    public String listBadges(Model model, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return "redirect:/login";
        }

        List<Badge> badges = badgeService.findAll();
        model.addAttribute("badges", badges);
        return "admin/badge/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return "redirect:/login";
        }

        model.addAttribute("badge", new Badge());
        model.addAttribute("sports", sportService.findAllActive());
        model.addAttribute("metrics", Metric.values());
        model.addAttribute("icons", PRESET_ICONS);
        return "admin/badge/create";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return "redirect:/login";
        }

        try {
            Badge badge = badgeService.findById(id);
            model.addAttribute("badge", badge);
            model.addAttribute("sports", sportService.findAllActive());
            model.addAttribute("metrics", Metric.values());
            model.addAttribute("icons", PRESET_ICONS);
            return "admin/badge/create";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/admin/badges";
        }
    }

    @PostMapping("/save")
    public String saveBadge(@ModelAttribute Badge badge,
                            @RequestParam("sportId") int sportId,
                            RedirectAttributes redirectAttributes,
                            Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return "redirect:/login";
        }

        try {
            Sport sport = sportRepository.findById(sportId)
                    .orElseThrow(() -> new IllegalArgumentException("Sport not found with id: " + sportId));

            if (badge.getLabel() == null || badge.getLabel().isBlank()) {
                throw new IllegalArgumentException("Badge label is required");
            }
            if (badge.getIcon() == null || badge.getIcon().isBlank()) {
                throw new IllegalArgumentException("Badge icon is required");
            }
            if (badge.getMetric() == null) {
                throw new IllegalArgumentException("Badge metric is required");
            }
            if (badge.getThreshold() <= 0) {
                throw new IllegalArgumentException("Threshold must be greater than zero");
            }

            badgeService.saveBadge(badge, sport);
            redirectAttributes.addAttribute("saved", true);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/admin/badges/create";
        }
        return "redirect:/admin/badges";
    }

    @PostMapping("/delete/{id}")
    public String deleteBadge(@PathVariable int id, RedirectAttributes redirectAttributes, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return "redirect:/login";
        }

        try {
            badgeService.deleteById(id);
            redirectAttributes.addAttribute("deleted", true);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        }
        return "redirect:/admin/badges";
    }

    /** Preset Bootstrap Icons for badge selection */
    private static final List<String[]> PRESET_ICONS = List.of(
        new String[]{"bi-trophy", "Trophée"},
        new String[]{"bi-star-fill", "Étoile"},
        new String[]{"bi-fire", "Feu"},
        new String[]{"bi-lightning-charge-fill", "Éclair"},
        new String[]{"bi-heart-pulse-fill", "Cardio"},
        new String[]{"bi-person-walking", "Coureur"},
        new String[]{"bi-bicycle", "Cyclisme"},
        new String[]{"bi-water", "Natation"},
        new String[]{"bi-snow", "Ski"},
        new String[]{"bi-dribbble", "Football"},
        new String[]{"bi-hand-index-thumb", "Handball"},
        new String[]{"bi-arrow-through-heart-fill", "Passion"},
        new String[]{"bi-shield-fill-check", "Bouclier"},
        new String[]{"bi-gem", "Diamant"}
    );
}