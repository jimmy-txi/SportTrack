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

/**
 * Spring MVC controller for adding comments (social interactions) to activities.
 *
 * <p>Handles the form submission for posting a comment on an activity,
 * including like and cheer interactions.</p>
 *
 * @author SportTrack Team
 */
@Controller
@RequestMapping("/comments")
public class CommentController {

    CommentService commentService;
    ActivityService activityService;
    AthleteService athleteService;


    public CommentController(CommentService commentService, ActivityService activityService, AthleteService athleteService) {
        this.commentService = commentService;
        this.activityService = activityService;
        this.athleteService = athleteService;
    }




    /**
     * Adds a comment or interaction to the specified activity.
     *
     * @param activityId        the identifier of the target activity
     * @param content           the textual content of the comment, may be {@code null}
     * @param interactionTypeStr the string representation of the interaction type
     * @param redirectUrl       the URL to redirect to after processing
     * @return a redirect to the specified URL, or to login if unauthenticated
     */
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
