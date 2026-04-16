package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.user.communication.Message;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.service.user.communication.FriendshipService;
import fr.utc.miage.sporttrack.service.user.communication.MessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Spring MVC controller for private messaging between friends.
 *
 * <p>Provides endpoints for the messaging inbox and sending messages
 * to friends.</p>
 *
 * @author SportTrack Team
 */
@Controller
@RequestMapping("/messages")
public class MessageController {

    /** Session attribute key for the cached athlete. */
    private static final String ATHLETE_ATTRIBUTE = "athlete";

    /** Service for message persistence and retrieval. */
    private final MessageService messageService;

    /** Service for friendship verification. */
    private final FriendshipService friendshipService;

    /** Repository for athlete authentication resolution. */
    private final AthleteRepository athleteRepository;

    /**
     * Constructs a {@code MessageController} with the required dependencies.
     *
     * @param messageService      the message service
     * @param friendshipService   the friendship service
     * @param athleteRepository   the athlete repository
     */
    public MessageController(MessageService messageService,
                             FriendshipService friendshipService,
                             AthleteRepository athleteRepository) {
        this.messageService = messageService;
        this.friendshipService = friendshipService;
        this.athleteRepository = athleteRepository;
    }

    /**
     * Displays the messaging inbox with a conversation view for the selected friend.
     *
     * @param friendId            the optional identifier of the friend whose conversation to display
     * @param session             the HTTP session for athlete resolution
     * @param model               the Spring MVC model
     * @param redirectAttributes  flash attributes for error messaging
     * @return the view name "athlete/friend/messages", or a redirect to login
     */
    @GetMapping
    public String inbox(@RequestParam(name = "friendId", required = false) Integer friendId,
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        Athlete currentAthlete = getAuthenticatedAthlete(session);
        if (currentAthlete == null) {
            return "redirect:/login";
        }

        List<Athlete> friends = friendshipService.getFriendsOfAthlete(currentAthlete.getId());
        Map<Integer, Message> latestByFriend = messageService.getLatestMessageByFriend(currentAthlete.getId());
        Map<Integer, Integer> unreadByFriend = messageService.getUnreadCountByFriend(currentAthlete.getId());

        List<Athlete> sortedFriends = friends.stream()
                .sorted(Comparator.comparing(
                        (Athlete a) -> latestByFriend.get(a.getId()) != null
                                ? latestByFriend.get(a.getId()).getSentAt()
                                : null,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();

        Integer defaultFriendId = null;
        if (!sortedFriends.isEmpty()) {
            defaultFriendId = sortedFriends.get(0).getId();
        }
        final Integer effectiveFriendId = friendId != null ? friendId : defaultFriendId;

        Athlete selectedFriend = null;
        List<Message> conversation = List.of();

        if (effectiveFriendId != null) {
            Optional<Athlete> selectedFriendOpt = sortedFriends.stream()
                    .filter(friend -> friend.getId().equals(effectiveFriendId))
                    .findFirst();

            if (selectedFriendOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vous ne pouvez discuter qu'avec vos amis.");
                return "redirect:/messages";
            }

            selectedFriend = selectedFriendOpt.get();
            conversation = messageService.getConversation(currentAthlete.getId(), selectedFriend.getId());
        }

        model.addAttribute(ATHLETE_ATTRIBUTE, currentAthlete);
        model.addAttribute("friends", sortedFriends);
        model.addAttribute("selectedFriend", selectedFriend);
        model.addAttribute("conversation", conversation);
        model.addAttribute("latestByFriend", latestByFriend);
        model.addAttribute("unreadByFriend", unreadByFriend);
        model.addAttribute("totalUnread", messageService.countUnreadMessages(currentAthlete.getId()));

        return "athlete/friend/messages";
    }

    /**
     * Sends a message to a friend.
     *
     * @param recipientId         the identifier of the message recipient
     * @param content             the message content
     * @param session             the HTTP session for athlete resolution
     * @param redirectAttributes  flash attributes for error messaging
     * @return a redirect to the conversation with the recipient
     */
    @PostMapping("/send")
    public String sendMessage(@RequestParam("recipientId") Integer recipientId,
                              @RequestParam("content") String content,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Athlete currentAthlete = getAuthenticatedAthlete(session);
        if (currentAthlete == null) {
            return "redirect:/login";
        }

        try {
            messageService.sendMessage(currentAthlete.getId(), recipientId, content);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/messages?friendId=" + recipientId;
    }

    /**
     * Resolves the currently authenticated athlete from the session or security context.
     *
     * @param session the HTTP session
     * @return the authenticated athlete, or {@code null} if not available
     */
    private Athlete getAuthenticatedAthlete(HttpSession session) {
        Athlete athlete = (Athlete) session.getAttribute(ATHLETE_ATTRIBUTE);
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
            session.setAttribute(ATHLETE_ATTRIBUTE, athlete);
        }

        return athlete;
    }
}
