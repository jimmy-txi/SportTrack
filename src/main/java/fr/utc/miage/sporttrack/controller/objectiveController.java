package fr.utc.miage.sporttrack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.service.event.ObjectiveService;
import jakarta.servlet.http.HttpSession;

@Controller
public class objectiveController {
    
    @Autowired
    private ObjectiveService objectiveService;

    @GetMapping("/objectives")
    public String getObjectives(HttpSession session, Model model) {
        Athlete athlete = (Athlete) session.getAttribute("athlete");
        if (athlete != null) {
            model.addAttribute("objectives", objectiveService.getObjectivesByUser(athlete));
        }
        return "/objective/objectives";
    }

    @PostMapping("/objectives")
    public String createObjective(
        @RequestParam String name,
        @RequestParam String description,
        Model model
    ) {
        Objective objective = new Objective(name, description);
        objectiveService.saveObjective(objective);
        
        return "redirect:/objectives";
    }

    @GetMapping("/objectives/add")
    public String showObjectivesForm() {
        return "/objective/objective_form";
    }
}
