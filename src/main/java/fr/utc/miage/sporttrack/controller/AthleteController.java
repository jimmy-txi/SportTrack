package fr.utc.miage.sporttrack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import fr.utc.miage.sporttrack.service.User.AthleteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/athlete")
public class AthleteController {

    private AthleteService athleteService;


    @GetMapping("/list")
    public String listAthletes(Model model) {
        model.addAttribute("athletes", athleteService.getAllAthletes());
        return "athlete/list";
    }    



}
