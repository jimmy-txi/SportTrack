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

@Controller
@RequestMapping("/messages")
public class MessageController {

    private static final String ATHLETE_ATTRIBUTE = "athlete";

    private final MessageService messageService;
    private final FriendshipService friendshipService;
    private final AthleteRepository athleteRepository;

    public MessageController(MessageService messageService,
                             FriendshipService friendshipService,
                             AthleteRepository athleteRepository) {
        this.messageService = messageService;
        this.friendshipService = friendshipService;
        this.athleteRepository = athleteRepository;
    }

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
