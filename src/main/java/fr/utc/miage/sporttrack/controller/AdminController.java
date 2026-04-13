package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.user.Admin;
import fr.utc.miage.sporttrack.service.user.AdminService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("")
    public String home(Model model, Authentication auth) {

        if (!checkAdminLoggedIn(auth)) {
            return "redirect:/login";
        }

        return "admin/home";
    }

    private boolean checkAdminLoggedIn(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return false;
        }
        Admin admin;
        try {
            admin = adminService.findByEmail(auth.getName());
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

}
