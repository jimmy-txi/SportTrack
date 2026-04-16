package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.dto.SportFormDTO;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.service.activity.SportService;
import fr.utc.miage.sporttrack.service.user.AdminService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Spring MVC controller for administrator sport management.
 *
 * <p>Provides CRUD endpoints for sports, including creation, editing,
 * and enabling/disabling, accessible only to authenticated admins.</p>
 *
 * @author SportTrack Team
 */
@Controller
@RequestMapping("/admin/sports")
public class SportController {

    /** Service for sport operations. */
    private final SportService sportService;

    /** Service for admin authentication verification. */
    private final AdminService adminService;

    /**
     * Constructs a {@code SportController} with the required services.
     *
     * @param sportService the sport service
     * @param adminService the admin service
     */
    public SportController(SportService sportService, AdminService adminService) {
        this.sportService = sportService;
        this.adminService = adminService;
    }

    /**
     * Lists all sports for the admin dashboard.
     *
     * @param model the Spring MVC model
     * @param auth  the current security authentication
     * @return the view name "admin/sport/list", or a redirect to login
     */
    @GetMapping
    public String listSports(Model model, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return "redirect:/login";
        }

        List<Sport> sports = sportService.findAll();
        model.addAttribute("sports", sports);
        return "admin/sport/list";
    }

    /**
     * Displays the form for creating a new sport.
     *
     * @param model the Spring MVC model
     * @param auth  the current security authentication
     * @return the view name "admin/sport/create", or a redirect to login
     */
    @GetMapping("/create")
    public String showCreateForm(Model model, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return "redirect:/login";
        }

        model.addAttribute("sport", new SportFormDTO());
        return "admin/sport/create";
    }

    /**
     * Displays the form for editing an existing sport.
     *
     * @param id                  the sport identifier
     * @param model               the Spring MVC model
     * @param redirectAttributes  flash attributes for error messaging
     * @param auth                the current security authentication
     * @return the edit view, or a redirect on failure
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return "redirect:/login";
        }

        Optional<Sport> sport = sportService.findById(id);
        if (sport.isEmpty()) {
            redirectAttributes.addAttribute("error", "Sport not found");
            return "redirect:/admin/sports";
        }
        Sport s = sport.get();
        SportFormDTO dto = new SportFormDTO();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setDescription(s.getDescription());
        dto.setCaloriesPerHour(s.getCaloriesPerHour());
        dto.setType(s.getType());
        model.addAttribute("sport", dto);
        return "admin/sport/create";
    }

    /**
     * Creates or updates a sport from the submitted form data.
     *
     * @param dto                 the form DTO containing sport data
     * @param redirectAttributes  flash attributes for success/error messaging
     * @param auth                the current security authentication
     * @return a redirect to the sport list or create form on error
     */
    @PostMapping("/save")
    public String saveSport(@ModelAttribute SportFormDTO dto, RedirectAttributes redirectAttributes, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return "redirect:/login";
        }

        try {
            if (dto.getId() == 0) {
                // Création d'un nouveau sport
                sportService.createSport(
                        dto.getName(),
                        dto.getDescription(),
                        dto.getCaloriesPerHour(),
                        dto.getType()
                );
                redirectAttributes.addAttribute("created", true);
            } else {
                // Mise à jour d'un sport existant
                sportService.updateSport(
                        dto.getId(),
                        dto.getName(),
                        dto.getDescription(),
                        dto.getCaloriesPerHour(),
                        dto.getType()
                );
                redirectAttributes.addAttribute("updated", true);
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/admin/sports/create";
        }
        return "redirect:/admin/sports";
    }

    /**
     * Enables the sport with the given identifier, making it available for athlete use.
     *
     * @param id   the sport identifier to enable
     * @param auth the current security authentication
     * @return a redirect to the sport list
     */
    @PostMapping("/enable/{id}")
    public String enable(@PathVariable int id, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return "redirect:/login";
        }

        sportService.enableSport(id);
        return "redirect:/admin/sports";
    }

    /**
     * Disables the sport with the given identifier, hiding it from athlete selection.
     *
     * @param id   the sport identifier to disable
     * @param auth the current security authentication
     * @return a redirect to the sport list
     */
    @PostMapping("/disable/{id}")
    public String disable(@PathVariable int id, Authentication auth) {
        if (!adminService.checkAdminLoggedIn(auth)) {
            return "redirect:/login";
        }

        sportService.disableSport(id);
        return "redirect:/admin/sports";
    }
}
