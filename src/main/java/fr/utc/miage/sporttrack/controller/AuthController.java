package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.dto.AthleteRegisterFormDTO;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class AuthController {

    private final AthleteService athleteService;

    public AuthController(AthleteService athleteService) {
        this.athleteService = athleteService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("athlete", new AthleteRegisterFormDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("athlete") AthleteRegisterFormDTO athleteDto,
                               @RequestParam("confirmPassword") String confirmPassword,
                               Model model) {
        if (!athleteDto.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "register";
        }

        try {
            athleteService.createProfile(athleteDto);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}
