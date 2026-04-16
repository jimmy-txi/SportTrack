package fr.utc.miage.sporttrack.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.dto.BadgeFormDTO;
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
    private static final String ERROR_ATTRIBUTE = "error";
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String REDIRECT_BADGES = "redirect:/admin/badges";

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
            return REDIRECT_LOGIN;
        }

        List<Badge> badges = badgeService.findAll();
        model.addAttribute("badges", badges);
        return "admin/badge/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return REDIRECT_LOGIN;
        }

        model.addAttribute("badge", new BadgeFormDTO());
        model.addAttribute("sports", sportService.findAllActive());
        model.addAttribute("metrics", Metric.values());
        model.addAttribute("icons", PRESET_ICONS);
        return "admin/badge/create";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return REDIRECT_LOGIN;
        }

        try {
            Badge badge = badgeService.findById(id);
            BadgeFormDTO dto = new BadgeFormDTO();
            dto.setId(badge.getId());
            dto.setLabel(badge.getLabel());
            dto.setDescription(badge.getDescription());
            dto.setSportId(badge.getSport() != null ? badge.getSport().getId() : null);
            dto.setMetric(badge.getMetric());
            dto.setThreshold(badge.getThreshold());
            dto.setIcon(badge.getIcon());
            model.addAttribute("badge", dto);
            model.addAttribute("sports", sportService.findAllActive());
            model.addAttribute("metrics", Metric.values());
            model.addAttribute("icons", PRESET_ICONS);
            return "admin/badge/create";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
            return REDIRECT_BADGES;
        }
    }

    @PostMapping("/save")
    public String saveBadge(@ModelAttribute BadgeFormDTO badgeForm,
                            RedirectAttributes redirectAttributes,
                            Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return REDIRECT_LOGIN;
        }

        try {
            Sport sport = sportRepository.findById(badgeForm.getSportId())
                    .orElseThrow(() -> new IllegalArgumentException("Sport not found with id: " + badgeForm.getSportId()));

            if (badgeForm.getLabel() == null || badgeForm.getLabel().isBlank()) {
                throw new IllegalArgumentException("Badge label is required");
            }
            if (badgeForm.getIcon() == null || badgeForm.getIcon().isBlank()) {
                throw new IllegalArgumentException("Badge icon is required");
            }
            if (badgeForm.getMetric() == null) {
                throw new IllegalArgumentException("Badge metric is required");
            }
            if (badgeForm.getThreshold() <= 0) {
                throw new IllegalArgumentException("Threshold must be greater than zero");
            }

            Badge badge = new Badge();
            badge.setId(badgeForm.getId() != null ? badgeForm.getId() : 0);
            badge.setLabel(badgeForm.getLabel());
            badge.setDescription(badgeForm.getDescription());
            badge.setMetric(badgeForm.getMetric());
            badge.setThreshold(badgeForm.getThreshold());
            badge.setIcon(badgeForm.getIcon());

            badgeService.saveBadge(badge, sport);
            redirectAttributes.addAttribute("saved", true);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
            return "redirect:/admin/badges/create";
        }
        return REDIRECT_BADGES;
    }

    @PostMapping("/delete/{id}")
    public String deleteBadge(@PathVariable int id, RedirectAttributes redirectAttributes, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return REDIRECT_LOGIN;
        }

        try {
            badgeService.deleteById(id);
            redirectAttributes.addAttribute("deleted", true);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute(ERROR_ATTRIBUTE, e.getMessage());
        }
        return REDIRECT_BADGES;
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