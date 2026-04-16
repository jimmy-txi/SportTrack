package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.activity.SportRepository;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.service.activity.SportService;
import fr.utc.miage.sporttrack.service.event.ObjectiveService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObjectiveControllerTest {

    @Mock
    private ObjectiveService objectiveService;

    @Mock
    private SportRepository sportRepository;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private SportService sportService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ObjectiveController controller;

    private Athlete athlete;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        athlete = createAthlete(1, "runner", "runner@mail.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getObjectives_shouldRedirectWhenNoAuthenticatedAthlete() {
        when(session.getAttribute("athlete")).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(null);

        String result = controller.getObjectives(session, model);

        assertEquals("redirect:/login", result);
    }

    @Test
    void getObjectives_shouldResolveAthleteFromSecurityContext() {
        List<Objective> objectives = List.of(new Objective("Obj", "Desc"));
        when(session.getAttribute("athlete")).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("runner@mail.com");
        when(athleteRepository.findByEmail("runner@mail.com")).thenReturn(Optional.of(athlete));
        when(objectiveService.getObjectivesByUser(athlete)).thenReturn(objectives);

        String result = controller.getObjectives(session, model);

        assertEquals("objective/objectives", result);
        verify(session).setAttribute("athlete", athlete);
        verify(model).addAttribute("athlete", athlete);
        verify(model).addAttribute("objectives", objectives);
    }

    @Test
    void createObjective_shouldRedirectWhenNoAthlete() {
        when(session.getAttribute("athlete")).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(null);

        String result = controller.createObjective(session, "objectif", "desc", 1);

        assertEquals("redirect:/login", result);
        verify(objectiveService, never()).saveObjective(any(), any(), any());
    }

    @Test
    void createObjective_shouldRedirectToAddWhenSportNotFound() {
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(sportRepository.findById(999)).thenReturn(Optional.empty());

        String result = controller.createObjective(session, "objectif", "desc", 999);

        assertEquals("redirect:/objectives/add", result);
        verify(objectiveService, never()).saveObjective(any(), any(), any());
    }

    @Test
    void createObjective_shouldSaveAndRedirect() {
        Sport sport = new Sport();
        sport.setId(7);

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(sportRepository.findById(7)).thenReturn(Optional.of(sport));

        String result = controller.createObjective(session, "Objectif test", "Description test", 7);

        assertEquals("redirect:/objectives", result);
        ArgumentCaptor<Objective> objectiveCaptor = ArgumentCaptor.forClass(Objective.class);
        verify(objectiveService).saveObjective(objectiveCaptor.capture(), eq(athlete), eq(sport));
        assertEquals("Objectif test", objectiveCaptor.getValue().getName());
        assertEquals("Description test", objectiveCaptor.getValue().getDescription());
    }

    @Test
    void deleteObjective_shouldDelegateToService() {
        String result = controller.deleteObjective(42);

        assertEquals("redirect:/objectives", result);
        verify(objectiveService).deleteById(42);
    }

    @Test
    void completeObjective_shouldRedirectWhenNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(null);

        String result = controller.completeObjective(5, session, redirectAttributes);

        assertEquals("redirect:/login", result);
    }

    @Test
    void completeObjective_shouldAddSuccessFlashOnSuccess() {
        when(session.getAttribute("athlete")).thenReturn(athlete);

        String result = controller.completeObjective(5, session, redirectAttributes);

        assertEquals("redirect:/objectives", result);
        verify(objectiveService).markAsCompleted(5, athlete);
        verify(redirectAttributes).addFlashAttribute("success", "Objectif marqué comme atteint !");
    }

    @Test
    void completeObjective_shouldAddErrorFlashOnFailure() {
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(objectiveService.markAsCompleted(8, athlete)).thenThrow(new IllegalArgumentException("Introuvable"));

        String result = controller.completeObjective(8, session, redirectAttributes);

        assertEquals("redirect:/objectives", result);
        verify(redirectAttributes).addFlashAttribute("error", "Introuvable");
    }

    @Test
    void showObjectivesForm_shouldRedirectWhenNoAthlete() {
        when(session.getAttribute("athlete")).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(null);

        String result = controller.showObjectivesForm(session, model);

        assertEquals("redirect:/login", result);
    }

    @Test
    void showObjectivesForm_shouldPopulateModelAndReturnView() {
        Sport sport = new Sport();
        sport.setId(1);

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(sportService.findAllActive()).thenReturn(List.of(sport));

        String result = controller.showObjectivesForm(session, model);

        assertEquals("objective/objective_form", result);
        verify(model).addAttribute("athlete", athlete);
        verify(model).addAttribute("sports", List.of(sport));
    }

    @Test
    void getObjectives_shouldRedirectWhenAuthenticationIsAnonymous() {
        when(session.getAttribute("athlete")).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(new AnonymousAuthenticationToken("key", "anon",
                List.of(() -> "ROLE_ANONYMOUS")));

        String result = controller.getObjectives(session, model);

        assertEquals("redirect:/login", result);
    }

    private Athlete createAthlete(int id, String username, String email) {
        Athlete created = new Athlete();
        created.setUsername(username);
        created.setPassword("pwd");
        created.setEmail(email);
        try {
            Field field = created.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(created, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return created;
    }

}
