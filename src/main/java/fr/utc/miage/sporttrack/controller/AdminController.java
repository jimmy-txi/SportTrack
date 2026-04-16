package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.service.user.AdminService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Spring MVC controller for the administrator home page.
 *
 * <p>Checks that the current user has admin privileges before granting
 * access to the admin dashboard.</p>
 *
 * @author SportTrack Team
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    /** Service for admin authentication verification. */
    private final AdminService adminService;

    /**
     * Constructs an {@code AdminController} with the required service.
     *
     * @param adminService the admin service for authentication checks
     */
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Renders the admin home page if the current user is an authenticated admin.
     *
     * @param model the Spring MVC model
     * @param auth  the current security authentication
     * @return the view name "admin/home", or a redirect to login if not an admin
     */
    @GetMapping("")
    public String home(Model model, Authentication auth) {

        if (!adminService.checkAdminLoggedIn(auth)) {
            return ControllerConstants.REDIRECT_LOGIN;
        }

        return ControllerConstants.ADMIN_HOME_VIEW;
    }

}
