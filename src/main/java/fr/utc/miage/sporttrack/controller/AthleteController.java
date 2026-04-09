package fr.utc.miage.sporttrack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import fr.utc.miage.sporttrack.service.User.AthleteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/athlete")
public class AthleteController {

    @Autowired
    private AthleteService athleteService;

    @GetMapping("/list")
    public String listAthletes(@RequestParam(name = "q", required = false) String query, Model model) {
        if (query != null && !query.isEmpty()) {
            model.addAttribute("query", query);
            model.addAttribute("athletes", athleteService.searchAthletesByName(query));
        } else {
            model.addAttribute("athletes", athleteService.getAllAthletes());
        }   
        return "athlete/list";
    }    



}
