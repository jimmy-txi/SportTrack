package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.service.activity.SportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/sports")
public class SportController {

    private final SportService sportService;

    public SportController(SportService sportService) {
        this.sportService = sportService;
    }

    /**
     * Affiche la liste de tous les sports
     */
    @GetMapping
    public String listSports(Model model) {
        List<Sport> sports = sportService.findAll();
        model.addAttribute("sports", sports);
        return "admin/sport/list";
    }

    /**
     * Affiche le formulaire de création d'un nouveau sport
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("sport", new Sport());
        return "admin/sport/create";
    }

    /**
     * Affiche le formulaire d'édition d'un sport existant
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Sport> sport = sportService.findById(id);
        if (sport.isEmpty()) {
            redirectAttributes.addAttribute("error", "Sport not found");
            return "redirect:/admin/sports";
        }
        model.addAttribute("sport", sport.get());
        return "admin/sport/create";
    }

    /**
     * Sauvegarde un nouveau sport ou met à jour un sport existant
     */
    @PostMapping("/save")
    public String saveSport(@ModelAttribute Sport sport, RedirectAttributes redirectAttributes) {
        try {
            if (sport.getIdS() == 0) {
                // Création d'un nouveau sport
                sportService.createSport(
                        sport.getName(),
                        sport.getDescription(),
                        sport.getHourlyCalories(),
                        sport.getType()
                );
                redirectAttributes.addAttribute("created", true);
            } else {
                // Mise à jour d'un sport existant
                sportService.updateSport(
                        sport.getIdS(),
                        sport.getName(),
                        sport.getDescription(),
                        sport.getHourlyCalories(),
                        sport.getType()
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
     * Supprime un sport
     */
    @PostMapping("/delete/{id}")
    public String deleteSport(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            sportService.deleteSport(id);
            redirectAttributes.addAttribute("deleted", true);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        }
        return "redirect:/admin/sports";
    }
}
