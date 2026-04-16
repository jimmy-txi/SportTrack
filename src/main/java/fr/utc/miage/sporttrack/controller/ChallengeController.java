package fr.utc.miage.sporttrack.controller;

import java.time.LocalDate;
import java.util.List;
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

import fr.utc.miage.sporttrack.dto.ChallengeFormDTO;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.entity.event.Challenge;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.activity.SportRepository;
import fr.utc.miage.sporttrack.repository.event.ChallengeRepository;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.service.activity.SportService;
import fr.utc.miage.sporttrack.service.event.ChallengeRankingService;
import jakarta.servlet.http.HttpSession;

@Controller
public class ChallengeController {
    
    private final SportRepository sportRepository;
    
    private final AthleteRepository athleteRepository;
    
    private final ChallengeRepository challengeRepository;

    private final SportService sportService;

    private final ChallengeRankingService challengeRankingService;

    private final String REDIRECT_LOGIN = "redirect:/login";
    private final String ATHLETE_ATTR = "athlete";
    private final String ALL_METRICS_ATTR = "allMetrics";
    private final String SPORTS_ATTR = "sports";
    private final String CHALLENGE_FORM_VIEW = "challenge/challenge_form";
    private final String ERROR_ATTR = "error";
    
    public ChallengeController(SportRepository sportRepository, AthleteRepository athleteRepository, ChallengeRepository challengeRepository, SportService sportService, ChallengeRankingService challengeRankingService) {
        this.sportRepository = sportRepository;
        this.athleteRepository = athleteRepository;
        this.challengeRepository = challengeRepository;
        this.sportService = sportService;
        this.challengeRankingService = challengeRankingService;
    }
    
    @GetMapping("/challenges/new")
    public String showCreateChallengeForm(HttpSession session, Model model) {
        Athlete athlete = getAuthenticatedAthlete(session);
        if (athlete == null) {
            return "redirect:/login";
        }
        model.addAttribute(ATHLETE_ATTR, athlete);
        model.addAttribute("challenge", new ChallengeFormDTO());
        model.addAttribute(ALL_METRICS_ATTR, Metric.values());
        model.addAttribute(SPORTS_ATTR, sportService.findAllActive());
        return CHALLENGE_FORM_VIEW;
    }
    
    @PostMapping("/challenges")
    public String createChallenge(
        @ModelAttribute ChallengeFormDTO challengeDto,
        @RequestParam(required = false) Integer sportId,
        HttpSession session,
        Model model) {
            Athlete athlete = getAuthenticatedAthlete(session);
            LocalDate now = LocalDate.now();
            if (athlete == null) {
                return "redirect:/login";
            }

            if (sportId == null || sportId <= 0) {
                model.addAttribute(ATHLETE_ATTR, athlete);
                model.addAttribute(ERROR_ATTR, "Veuillez sélectionner une discipline sportive valide.");
                model.addAttribute(ALL_METRICS_ATTR, Metric.values());
                model.addAttribute(SPORTS_ATTR, sportService.findAllActive());
                return CHALLENGE_FORM_VIEW;
            }

            Optional<Sport> sportOpt = sportRepository.findById(sportId);
            if (sportOpt.isEmpty()) {
                model.addAttribute(ATHLETE_ATTR, athlete);
                model.addAttribute(ERROR_ATTR, "La discipline sportive sélectionnée est introuvable.");
                model.addAttribute(ALL_METRICS_ATTR, Metric.values());
                model.addAttribute(SPORTS_ATTR, sportService.findAllActive());
                return CHALLENGE_FORM_VIEW;
            }

            if (challengeDto.getStartDate().isAfter(challengeDto.getEndDate()) || challengeDto.getStartDate().isBefore(now) || challengeDto.getEndDate().isBefore(now)) {
                model.addAttribute(ATHLETE_ATTR, athlete);
                model.addAttribute(ERROR_ATTR, "La date de début doit être antérieure ou égale à la date de fin.et les dates doivent être supérieures ou égales à la date actuelle.");
                model.addAttribute(ALL_METRICS_ATTR, Metric.values());
                model.addAttribute(SPORTS_ATTR, sportService.findAllActive());
                return CHALLENGE_FORM_VIEW;
            }

            Challenge challenge = new Challenge(
                challengeDto.getName(),
                challengeDto.getDescription(),
                challengeDto.getStartDate(),
                challengeDto.getEndDate(),
                challengeDto.getMetric()
            );
            challenge.setOrganizer(athlete);
            challenge.setSport(sportOpt.get());
            Challenge savedChallenge = challengeRepository.save(challenge);
            challengeRankingService.recomputeRanking(savedChallenge);

            return "redirect:/challenges";
        }
        
        
        @GetMapping("/challenges")
        public String listChallenges(HttpSession session, Model model) {
            Athlete athlete = getAuthenticatedAthlete(session);
            if (athlete == null) {
                return "redirect:/login";
            }
            List<Challenge> challenges = challengeRepository.findDistinctByOrganizer_IdOrParticipants_Id(athlete.getId(), athlete.getId());
            model.addAttribute(ATHLETE_ATTR, athlete);
            model.addAttribute("challenges", challenges);
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
                session.setAttribute(ATHLETE_ATTR, athlete);
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
                    Challenge savedChallenge = challengeRepository.save(challenge);
                    challengeRankingService.recomputeRanking(savedChallenge);
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
            List<Challenge> challenges = challengeRepository.findAll();
            model.addAttribute(ATHLETE_ATTR, athlete);
            model.addAttribute("challenges", challenges);
            return "challenge/challenges";
    }
}
