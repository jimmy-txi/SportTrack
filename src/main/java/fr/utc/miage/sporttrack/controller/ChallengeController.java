package fr.utc.miage.sporttrack.controller;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.entity.event.Challenge;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.activity.SportRepository;
import fr.utc.miage.sporttrack.repository.event.ChallengeRepository;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.service.activity.SportService;
import jakarta.servlet.http.HttpSession;

@Controller
public class ChallengeController {
    
    private final SportRepository sportRepository;
    
    private final AthleteRepository athleteRepository;
    
    private final ChallengeRepository challengeRepository;

    private final SportService sportService;
    
    public ChallengeController(SportRepository sportRepository, AthleteRepository athleteRepository, ChallengeRepository challengeRepository, SportService sportService) {
        this.sportRepository = sportRepository;
        this.athleteRepository = athleteRepository;
        this.challengeRepository = challengeRepository;
        this.sportService = sportService;
    }
    
    @GetMapping("/challenges/new")
    public String showCreateChallengeForm(HttpSession session, Model model) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }
        model.addAttribute("athlete", athlete);
        model.addAttribute("challenge", new Challenge());
        model.addAttribute("allMetrics", Metric.values());
        model.addAttribute("sports", sportService.findAllActive());
        return "challenge/challenge_form";
    }
    
    @PostMapping("/challenges")
    public String createChallenge(
        @ModelAttribute Challenge challenge, 
        @RequestParam(required = false) Integer sportId, 
        HttpSession session, 
        Model model) {
            Athlete athlete = getAuthenticatedAthlete(session);
            LocalDate now = LocalDate.now();
            if (athlete == null) {
                return "redirect:/login";
            }

            if (sportId == null || sportId <= 0) {
                model.addAttribute("athlete", athlete);
                model.addAttribute("error", "Veuillez sélectionner une discipline sportive valide.");
                model.addAttribute("allMetrics", Metric.values());
                model.addAttribute("sports", sportService.findAllActive());
                return "challenge/challenge_form";
            }

            Optional<Sport> sportOpt = sportRepository.findById(sportId);

            if (challenge.getStartDate().isAfter(challenge.getEndDate()) || challenge.getStartDate().isBefore(now) || challenge.getEndDate().isBefore(now)) {
                model.addAttribute("athlete", athlete);
                model.addAttribute("error", "La date de début doit être antérieure ou égale à la date de fin.et les dates doivent être supérieures ou égales à la date actuelle.");
                model.addAttribute("allMetrics", Metric.values());
                model.addAttribute("sports", sportService.findAllActive());
                return "challenge/challenge_form";
            }

            challenge.setOrganizer(athlete);
            challenge.setSport(sportOpt.get());
            challengeRepository.save(challenge);
            
            return "redirect:/challenges";
        }
        
        
        @GetMapping("/challenges")
        public String listChallenges(HttpSession session, Model model) {
            Athlete athlete = getAuthenticatedAthlete(session);
            if (athlete == null) {
                return "redirect:/login";
            }
            model.addAttribute("athlete", athlete);
            model.addAttribute("challenges", challengeRepository.findDistinctByOrganizer_IdOrParticipants_Id(athlete.getId(), athlete.getId()));
            return "challenge/challenge_list";
        }
        
        private Athlete getAuthenticatedAthlete(HttpSession session) {
            Athlete athlete = (Athlete) session.getAttribute("athlete");
            if (athlete != null) {
                return athlete;
            }
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
                return null;
            }
            
            Optional<Athlete> athleteOptional = athleteRepository.findByEmail(authentication.getName());
            if (athleteOptional.isPresent()) {
                athlete = athleteOptional.get();
                session.setAttribute("athlete", athlete);
            }
            
            return athlete;
        }

        @GetMapping("/challenges/participate")
        public String participateInChallenge(@RequestParam int id, HttpSession session) {
            Athlete athlete = getAuthenticatedAthlete(session);
            if (athlete == null) {
                return "redirect:/login";
            }
            
            Optional<Challenge> challengeOpt = challengeRepository.findById(id);
            if (challengeOpt.isPresent()) {
                Challenge challenge = challengeOpt.get();
                if (!challenge.getParticipants().contains(athlete)) {
                    challenge.getParticipants().add(athlete);
                    challengeRepository.save(challenge);
                }
            }
            return "redirect:/challenges";
        }

        @GetMapping("/challenges/list")
        public String listAllChallenges(HttpSession session, Model model) {
            Athlete athlete = getAuthenticatedAthlete(session);
            if (athlete == null) {
                return "redirect:/login";
            }
            model.addAttribute("athlete", athlete);
            model.addAttribute("challenges", challengeRepository.findAll());
            return "challenge/challenges";
    }
}
