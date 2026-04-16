package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.dto.SportFormDTO;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.service.activity.SportService;
import fr.utc.miage.sporttrack.service.user.AdminService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
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

    /** Redirect constant for the login page. */
    private static final String REDIRECT_LOGIN = "redirect:/login";

    /** Redirect constant for the admin sports list. */
    private static final String REDIRECT_SPORTS = "redirect:/admin/sports";

    /** Redirect constant for the admin sports create form. */
    private static final String REDIRECT_SPORTS_CREATE = "redirect:/admin/sports/create";

    /** View name for the sports list. */
    private static final String SPORTS_LIST_VIEW = "admin/sport/list";

    /** View name for the sports form (create/edit). */
    private static final String SPORTS_FORM_VIEW = "admin/sport/create";

    /** Request parameter name for error messages. */
    private static final String ERROR_PARAM = "error";

    /** Model attribute name for sports. */
    private static final String SPORTS_ATTR = "sports";

    /** Model attribute name for sport. */
    private static final String SPORT_ATTR = "sport";

    /** Request parameter name for creation success. */
    private static final String CREATED_PARAM = "created";

    /** Request parameter name for update success. */
    private static final String UPDATED_PARAM = "updated";

    /** Error message when sport is not found. */
    private static final String SPORT_NOT_FOUND_MSG = "Sport not found";

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
            return REDIRECT_LOGIN;
        }

        List<Sport> sports = sportService.findAll();
        model.addAttribute(SPORTS_ATTR, sports);
        return SPORTS_LIST_VIEW;
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
            return REDIRECT_LOGIN;
        }

        model.addAttribute(SPORT_ATTR, new SportFormDTO());
        return SPORTS_FORM_VIEW;
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
            return REDIRECT_LOGIN;
        }

        Optional<Sport> sport = sportService.findById(id);
        if (sport.isEmpty()) {
            redirectAttributes.addAttribute(ERROR_PARAM, SPORT_NOT_FOUND_MSG);
            return REDIRECT_SPORTS;
        }
        Sport s = sport.get();
        SportFormDTO dto = new SportFormDTO();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setDescription(s.getDescription());
        dto.setCaloriesPerHour(s.getCaloriesPerHour());
        dto.setType(s.getType());
        model.addAttribute(SPORT_ATTR, dto);
        return SPORTS_FORM_VIEW;
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
            return REDIRECT_LOGIN;
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
                redirectAttributes.addAttribute(CREATED_PARAM, true);
            } else {
                // Mise à jour d'un sport existant
                sportService.updateSport(
                        dto.getId(),
                        dto.getName(),
                        dto.getDescription(),
                        dto.getCaloriesPerHour(),
                        dto.getType()
                );
                redirectAttributes.addAttribute(UPDATED_PARAM, true);
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute(ERROR_PARAM, e.getMessage());
            return REDIRECT_SPORTS_CREATE;
        }
        return REDIRECT_SPORTS;
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
            return REDIRECT_LOGIN;
        }

        sportService.enableSport(id);
        return REDIRECT_SPORTS;
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
            return REDIRECT_LOGIN;
        }

        sportService.disableSport(id);
        return REDIRECT_SPORTS;
    }
}
