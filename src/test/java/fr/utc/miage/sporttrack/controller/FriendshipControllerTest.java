package fr.utc.miage.sporttrack.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.enumeration.FriendshipStatus;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.user.communication.Friendship;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.repository.user.communication.FriendshipRepository;
import fr.utc.miage.sporttrack.service.activity.ActivityService;
import fr.utc.miage.sporttrack.service.activity.WeatherReportService;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import fr.utc.miage.sporttrack.service.user.communication.FriendshipService;
import jakarta.servlet.http.HttpSession;

/**
 * Comprehensive unit tests for {@link FriendshipController}.
 * Targets 100% code coverage including all branches of every method.
 */
@ExtendWith(MockitoExtension.class)
class FriendshipControllerTest {

    @Mock
    private FriendshipService friendshipService;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private AthleteService athleteService;

    @Mock
    private ActivityService activityService;

    @Mock
    private WeatherReportService weatherReportService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private HttpSession session;

    @InjectMocks
    private FriendshipController controller;

    private Athlete currentAthlete;
    private Athlete otherAthlete;
    private Athlete thirdAthlete;

    @BeforeEach
    void setUp() {
        currentAthlete = createAthleteWithId(1, "current@test.com", "currentUser");
        otherAthlete = createAthleteWithId(2, "other@test.com", "otherUser");
        thirdAthlete = createAthleteWithId(3, "third@test.com", "thirdUser");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ======================== friendsPage tests ========================

    @Test
    void friendsPage_shouldRedirectToLogin_whenNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);
        // SecurityContextHolder has no authentication by default after clear

        String result = controller.friendsPage(session, null, null, model);

        assertEquals("redirect:/login", result);
    }

    @Test
    void friendsPage_shouldShowFriendsPage_whenAuthenticatedWithSession() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        when(friendshipService.getFriendsOfAthlete(1)).thenReturn(List.of(otherAthlete));
        when(friendshipService.getPendingRequestsForAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getSentPendingRequests(1)).thenReturn(Collections.emptyList());
        when(athleteService.getAllAthletes()).thenReturn(new ArrayList<>(List.of(currentAthlete, otherAthlete)));

        String result = controller.friendsPage(session, null, null, model);

        assertEquals("athlete/friend/friends", result);
        verify(model).addAttribute("friends", List.of(otherAthlete));
        verify(model).addAttribute("requests", Collections.emptyList());
        verify(model).addAttribute("sentRequests", Collections.emptyList());
        verify(model).addAttribute("activeTab", "friends");
        verify(model).addAttribute("currentAthlete", currentAthlete);
    }

    @Test
    void friendsPage_shouldShowFriendsPage_whenAuthenticatedWithSecurityContext() {
        when(session.getAttribute("athlete")).thenReturn(null);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("current@test.com");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(athleteRepository.findByEmail("current@test.com")).thenReturn(Optional.of(currentAthlete));
        when(friendshipService.getFriendsOfAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getPendingRequestsForAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getSentPendingRequests(1)).thenReturn(Collections.emptyList());
        when(athleteService.getAllAthletes()).thenReturn(new ArrayList<>(List.of(currentAthlete)));

        String result = controller.friendsPage(session, null, null, model);

        assertEquals("athlete/friend/friends", result);
        verify(session).setAttribute("athlete", currentAthlete);
    }

    @Test
    void friendsPage_shouldSearchAthletes_whenQueryProvided() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        when(friendshipService.getFriendsOfAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getPendingRequestsForAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getSentPendingRequests(1)).thenReturn(Collections.emptyList());
        when(athleteService.searchAthletesByName("other")).thenReturn(new ArrayList<>(List.of(otherAthlete)));

        String result = controller.friendsPage(session, "other", null, model);

        assertEquals("athlete/friend/friends", result);
        verify(model).addAttribute("query", "other");
        verify(model).addAttribute("athletes", List.of(otherAthlete));
    }

    @Test
    void friendsPage_shouldUseSpecifiedTab_whenTabProvided() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        when(friendshipService.getFriendsOfAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getPendingRequestsForAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getSentPendingRequests(1)).thenReturn(Collections.emptyList());
        when(athleteService.getAllAthletes()).thenReturn(new ArrayList<>());

        String result = controller.friendsPage(session, null, "requests", model);

        assertEquals("athlete/friend/friends", result);
        verify(model).addAttribute("activeTab", "requests");
    }

    @Test
    void friendsPage_shouldUseDefaultTab_whenTabIsNull() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        when(friendshipService.getFriendsOfAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getPendingRequestsForAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getSentPendingRequests(1)).thenReturn(Collections.emptyList());
        when(athleteService.getAllAthletes()).thenReturn(new ArrayList<>());

        String result = controller.friendsPage(session, null, null, model);

        assertEquals("athlete/friend/friends", result);
        verify(model).addAttribute("activeTab", "friends");
    }

    @Test
    void friendsPage_shouldRemoveCurrentUserFromAthleteList() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        when(friendshipService.getFriendsOfAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getPendingRequestsForAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getSentPendingRequests(1)).thenReturn(Collections.emptyList());
        // Return a mutable list containing the current user
        when(athleteService.getAllAthletes()).thenReturn(new ArrayList<>(List.of(currentAthlete, otherAthlete, thirdAthlete)));

        controller.friendsPage(session, null, null, model);

        // Verify the athletes attribute does not contain the current user
        verify(model).addAttribute(eq("athletes"), argThat(list -> {
            @SuppressWarnings("unchecked")
            List<Athlete> athletes = (List<Athlete>) list;
            return athletes.stream().noneMatch(a -> a.getId().equals(currentAthlete.getId()))
                    && athletes.size() == 2;
        }));
    }

    @Test
    void friendsPage_shouldNotSearch_whenQueryIsEmpty() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        when(friendshipService.getFriendsOfAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getPendingRequestsForAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getSentPendingRequests(1)).thenReturn(Collections.emptyList());
        when(athleteService.getAllAthletes()).thenReturn(new ArrayList<>());

        controller.friendsPage(session, "", null, model);

        verify(athleteService, never()).searchAthletesByName(anyString());
        verify(athleteService).getAllAthletes();
    }

    // ======================== friendProfile tests ========================

    @Test
    void friendProfile_shouldRedirectToLogin_whenNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);

        String result = controller.friendProfile(2, session, model);

        assertEquals("redirect:/login", result);
    }

    @Test
    void friendProfile_shouldRedirectToFriends_whenTargetNotFound() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        when(athleteRepository.findById(999)).thenReturn(Optional.empty());

        String result = controller.friendProfile(999, session, model);

        assertEquals("redirect:/friends", result);
    }

    @Test
    void friendProfile_shouldReturnSELF_whenViewingOwnProfile() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        when(athleteRepository.findById(1)).thenReturn(Optional.of(currentAthlete));
        when(activityService.findAllByAthleteIds(List.of(1))).thenReturn(Collections.emptyList());

        String result = controller.friendProfile(1, session, model);

        assertEquals("athlete/friend/profile", result);
        verify(model).addAttribute("relationshipStatus", "SELF");
        verify(model).addAttribute("profileAthlete", currentAthlete);
    }

    @Test
    void friendProfile_shouldReturnNONE_whenNoFriendshipExists() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        when(athleteRepository.findById(2)).thenReturn(Optional.of(otherAthlete));
        when(friendshipRepository.findBetweenAthletes(currentAthlete, otherAthlete)).thenReturn(Optional.empty());

        String result = controller.friendProfile(2, session, model);

        assertEquals("athlete/friend/profile", result);
        verify(model).addAttribute("relationshipStatus", "NONE");
        verify(model).addAttribute("friendship", (Object) null);
    }

    @Test
    void friendProfile_shouldReturnACCEPTED_whenFriends() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        when(athleteRepository.findById(2)).thenReturn(Optional.of(otherAthlete));

        Friendship friendship = createFriendship(10, currentAthlete, otherAthlete, FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findBetweenAthletes(currentAthlete, otherAthlete)).thenReturn(Optional.of(friendship));
        when(activityService.findAllByAthleteIds(List.of(2))).thenReturn(Collections.emptyList());

        String result = controller.friendProfile(2, session, model);

        assertEquals("athlete/friend/profile", result);
        verify(model).addAttribute("relationshipStatus", "ACCEPTED");
        verify(model).addAttribute("friendship", friendship);
    }

    @Test
    void friendProfile_shouldReturnPENDING_whenRequestPending() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        when(athleteRepository.findById(2)).thenReturn(Optional.of(otherAthlete));

        Friendship friendship = createFriendship(11, currentAthlete, otherAthlete, FriendshipStatus.PENDING);
        when(friendshipRepository.findBetweenAthletes(currentAthlete, otherAthlete)).thenReturn(Optional.of(friendship));

        String result = controller.friendProfile(2, session, model);

        assertEquals("athlete/friend/profile", result);
        verify(model).addAttribute("relationshipStatus", "PENDING");
    }

    @Test
    void friendProfile_shouldReturnNONE_whenFriendshipRejected() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        when(athleteRepository.findById(2)).thenReturn(Optional.of(otherAthlete));

        Friendship friendship = createFriendship(12, currentAthlete, otherAthlete, FriendshipStatus.REJECTED);
        when(friendshipRepository.findBetweenAthletes(currentAthlete, otherAthlete)).thenReturn(Optional.of(friendship));

        String result = controller.friendProfile(2, session, model);

        assertEquals("athlete/friend/profile", result);
        verify(model).addAttribute("relationshipStatus", "NONE");
    }

    @Test
    void friendsActivities_shouldRedirectToLogin_whenNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);

        String result = controller.friendsActivities(session, model);

        assertEquals("redirect:/login", result);
    }

    @Test
    void friendsActivities_shouldShowAggregatedFeed_whenAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        when(friendshipService.getFriendsOfAthlete(1)).thenReturn(List.of(otherAthlete, thirdAthlete));

        Activity firstActivity = new Activity();
        firstActivity.setId(101);
        Activity secondActivity = new Activity();
        secondActivity.setId(102);

        when(activityService.findAllByAthleteIds(List.of(2, 3))).thenReturn(List.of(firstActivity, secondActivity));

        String result = controller.friendsActivities(session, model);

        assertEquals("athlete/friend/activities", result);
        verify(model).addAttribute("friends", List.of(otherAthlete, thirdAthlete));
        verify(model).addAttribute("activities", List.of(firstActivity, secondActivity));
    }

    // ======================== sendFriendRequest tests ========================

    @Test
    void sendFriendRequest_shouldRedirectToLogin_whenNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);

        String result = controller.sendFriendRequest(session, 2, redirectAttributes);

        assertEquals("redirect:/login", result);
    }

    @Test
    void sendFriendRequest_shouldRedirectWithSuccess_whenRequestSent() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);

        String result = controller.sendFriendRequest(session, 2, redirectAttributes);

        assertEquals("redirect:/friends?tab=add", result);
        verify(friendshipService).sendFriendRequest(1, 2);
        verify(redirectAttributes).addFlashAttribute("success", "Demande d'ami envoyée avec succès !");
    }

    @Test
    void sendFriendRequest_shouldRedirectWithError_whenExceptionThrown() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        doThrow(new IllegalArgumentException("A friend request already exists and is pending"))
                .when(friendshipService).sendFriendRequest(1, 2);

        String result = controller.sendFriendRequest(session, 2, redirectAttributes);

        assertEquals("redirect:/friends?tab=add", result);
        verify(redirectAttributes).addFlashAttribute("error", "A friend request already exists and is pending");
    }

    @Test
    void sendFriendRequest_shouldRedirectWithError_whenSelfRequest() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        doThrow(new IllegalArgumentException("You cannot send a friend request to yourself"))
                .when(friendshipService).sendFriendRequest(1, 1);

        String result = controller.sendFriendRequest(session, 1, redirectAttributes);

        assertEquals("redirect:/friends?tab=add", result);
        verify(redirectAttributes).addFlashAttribute("error", "You cannot send a friend request to yourself");
    }

    // ======================== acceptFriendRequest tests ========================

    @Test
    void acceptFriendRequest_shouldRedirectToLogin_whenNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);

        String result = controller.acceptFriendRequest(10, session, redirectAttributes);

        assertEquals("redirect:/login", result);
    }

    @Test
    void acceptFriendRequest_shouldRedirectWithSuccess_whenAccepted() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);

        String result = controller.acceptFriendRequest(10, session, redirectAttributes);

        assertEquals("redirect:/friends?tab=requests", result);
        verify(friendshipService).acceptFriendRequest(10, 1);
        verify(redirectAttributes).addFlashAttribute("success", "Demande d'ami acceptée !");
    }

    @Test
    void acceptFriendRequest_shouldRedirectWithError_whenExceptionThrown() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        doThrow(new IllegalArgumentException("Only the recipient can accept a friend request"))
                .when(friendshipService).acceptFriendRequest(10, 1);

        String result = controller.acceptFriendRequest(10, session, redirectAttributes);

        assertEquals("redirect:/friends?tab=requests", result);
        verify(redirectAttributes).addFlashAttribute("error", "Only the recipient can accept a friend request");
    }

    @Test
    void acceptFriendRequest_shouldRedirectWithError_whenNotPending() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        doThrow(new IllegalStateException("Only pending friend requests can be accepted"))
                .when(friendshipService).acceptFriendRequest(10, 1);

        String result = controller.acceptFriendRequest(10, session, redirectAttributes);

        assertEquals("redirect:/friends?tab=requests", result);
        verify(redirectAttributes).addFlashAttribute("error", "Only pending friend requests can be accepted");
    }

    // ======================== rejectFriendRequest tests ========================

    @Test
    void rejectFriendRequest_shouldRedirectToLogin_whenNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);

        String result = controller.rejectFriendRequest(10, session, redirectAttributes);

        assertEquals("redirect:/login", result);
    }

    @Test
    void rejectFriendRequest_shouldRedirectWithSuccess_whenRejected() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);

        String result = controller.rejectFriendRequest(10, session, redirectAttributes);

        assertEquals("redirect:/friends?tab=requests", result);
        verify(friendshipService).rejectFriendRequest(10, 1);
        verify(redirectAttributes).addFlashAttribute("success", "Demande d'ami refusée.");
    }

    @Test
    void rejectFriendRequest_shouldRedirectWithError_whenExceptionThrown() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        doThrow(new IllegalArgumentException("Only the recipient can reject a friend request"))
                .when(friendshipService).rejectFriendRequest(10, 1);

        String result = controller.rejectFriendRequest(10, session, redirectAttributes);

        assertEquals("redirect:/friends?tab=requests", result);
        verify(redirectAttributes).addFlashAttribute("error", "Only the recipient can reject a friend request");
    }

    // ======================== removeFriend tests ========================

    @Test
    void removeFriend_shouldRedirectToLogin_whenNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);

        String result = controller.removeFriend(2, session, redirectAttributes);

        assertEquals("redirect:/login", result);
    }

    @Test
    void removeFriend_shouldRedirectWithSuccess_whenRemoved() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);

        String result = controller.removeFriend(2, session, redirectAttributes);

        assertEquals("redirect:/friends?tab=friends", result);
        verify(friendshipService).removeFriend(1, 2);
        verify(redirectAttributes).addFlashAttribute("success", "Ami supprimé avec succès.");
    }

    @Test
    void removeFriend_shouldRedirectWithError_whenExceptionThrown() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        doThrow(new IllegalArgumentException("Friendship does not exist"))
                .when(friendshipService).removeFriend(1, 2);

        String result = controller.removeFriend(2, session, redirectAttributes);

        assertEquals("redirect:/friends?tab=friends", result);
        verify(redirectAttributes).addFlashAttribute("error", "Friendship does not exist");
    }

    @Test
    void removeFriend_shouldRedirectWithError_whenNotAccepted() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        doThrow(new IllegalStateException("Only accepted friendships can be removed"))
                .when(friendshipService).removeFriend(1, 2);

        String result = controller.removeFriend(2, session, redirectAttributes);

        assertEquals("redirect:/friends?tab=friends", result);
        verify(redirectAttributes).addFlashAttribute("error", "Only accepted friendships can be removed");
    }

    // ======================== getAuthenticatedAthlete (via public methods) ========================

    @Test
    void getAuthenticatedAthlete_shouldReturnNull_whenAuthenticationIsNull() {
        when(session.getAttribute("athlete")).thenReturn(null);
        SecurityContextHolder.clearContext();

        String result = controller.friendsPage(session, null, null, model);

        assertEquals("redirect:/login", result);
    }

    @Test
    void getAuthenticatedAthlete_shouldReturnNull_whenNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(auth);

        String result = controller.friendsPage(session, null, null, model);

        assertEquals("redirect:/login", result);
    }

    @Test
    void getAuthenticatedAthlete_shouldReturnNull_whenAnonymousAuthentication() {
        when(session.getAttribute("athlete")).thenReturn(null);

        AnonymousAuthenticationToken anonymousToken = new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        SecurityContextHolder.getContext().setAuthentication(anonymousToken);

        String result = controller.friendsPage(session, null, null, model);

        assertEquals("redirect:/login", result);
    }

    @Test
    void getAuthenticatedAthlete_shouldReturnNull_whenAthleteNotFoundByEmail() {
        when(session.getAttribute("athlete")).thenReturn(null);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("unknown@test.com");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(athleteRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        String result = controller.friendsPage(session, null, null, model);

        assertEquals("redirect:/login", result);
    }

    @Test
    void getAuthenticatedAthlete_shouldReturnAthlete_whenFoundByEmail() {
        when(session.getAttribute("athlete")).thenReturn(null);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("current@test.com");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(athleteRepository.findByEmail("current@test.com")).thenReturn(Optional.of(currentAthlete));
        when(friendshipService.getFriendsOfAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getPendingRequestsForAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getSentPendingRequests(1)).thenReturn(Collections.emptyList());
        when(athleteService.getAllAthletes()).thenReturn(new ArrayList<>());

        String result = controller.friendsPage(session, null, null, model);

        assertEquals("athlete/friend/friends", result);
        verify(session).setAttribute("athlete", currentAthlete);
    }

    @Test
    void getAuthenticatedAthlete_shouldReturnFromSession_whenSessionHasAthlete() {
        when(session.getAttribute("athlete")).thenReturn(currentAthlete);
        when(friendshipService.getFriendsOfAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getPendingRequestsForAthlete(1)).thenReturn(Collections.emptyList());
        when(friendshipService.getSentPendingRequests(1)).thenReturn(Collections.emptyList());
        when(athleteService.getAllAthletes()).thenReturn(new ArrayList<>());

        String result = controller.friendsPage(session, null, null, model);

        assertEquals("athlete/friend/friends", result);
        // Should NOT query the repository since session already has the athlete
        verify(athleteRepository, never()).findByEmail(anyString());
    }

    // ======================== Helper methods ========================

    /**
     * Creates an Athlete instance with the given id, email, and username using reflection.
     */
    private Athlete createAthleteWithId(int id, String email, String username) {
        Athlete athlete = new Athlete();
        try {
            Field idField = athlete.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(athlete, id);

            Field emailField = athlete.getClass().getSuperclass().getDeclaredField("email");
            emailField.setAccessible(true);
            emailField.set(athlete, email);

            Field usernameField = athlete.getClass().getSuperclass().getDeclaredField("username");
            usernameField.setAccessible(true);
            usernameField.set(athlete, username);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set athlete fields via reflection", e);
        }
        return athlete;
    }

    /**
     * Creates a Friendship instance with the given id, initiator, recipient, and status using reflection.
     */
    private Friendship createFriendship(int id, Athlete initiator, Athlete recipient, FriendshipStatus status) {
        Friendship friendship = new Friendship();
        try {
            Field idField = friendship.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(friendship, id);

            Field initiatorField = friendship.getClass().getDeclaredField("initiator");
            initiatorField.setAccessible(true);
            initiatorField.set(friendship, initiator);

            Field recipientField = friendship.getClass().getDeclaredField("recipient");
            recipientField.setAccessible(true);
            recipientField.set(friendship, recipient);

            Field statusField = friendship.getClass().getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(friendship, status);

            Field createdAtField = friendship.getClass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(friendship, LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException("Failed to set friendship fields via reflection", e);
        }
        return friendship;
    }
}
