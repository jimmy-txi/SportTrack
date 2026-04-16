package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.enumeration.InteractionType;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.service.activity.ActivityService;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import fr.utc.miage.sporttrack.service.user.communication.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private AthleteService athleteService;

    @PostMapping("/add")
    public String addComment(
            @RequestParam("activityId") int activityId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam("interactionType") String interactionTypeStr,
            @RequestParam(value = "redirectUrl", defaultValue = "/friends") String redirectUrl
    ) {
        if (redirectUrl == null || !redirectUrl.startsWith("/") || redirectUrl.startsWith("//")) {
            redirectUrl = "/friends";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String email = auth.getName();
        Athlete author = athleteService.getCurrentAthlete(email);

        Optional<Activity> activityOpt = activityService.findById(activityId);
        if (activityOpt.isEmpty()) {
            return "redirect:" + redirectUrl + "?error=ActivityNotFound";
        }
        
        InteractionType type;
        try {
            type = InteractionType.valueOf(interactionTypeStr);
        } catch (IllegalArgumentException e) {
            type = InteractionType.NONE;
        }

        commentService.addComment(author, activityOpt.get(), content, type);

        return "redirect:" + redirectUrl;
    }
}
