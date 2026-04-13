package fr.utc.miage.sporttrack.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import fr.utc.miage.sporttrack.service.activity.SportService;
import fr.utc.miage.sporttrack.service.user.AdminService;

@ExtendWith(MockitoExtension.class)
class SportControllerTest {

    private static final int SPORT_ID = 1;
    private static final String SPORT_NAME = "Course à pied";
    private static final String SPORT_DESCRIPTION = "Sport de course";
    private static final double CALORIES_PER_HOUR = 500.0;
    private static final SportType SPORT_TYPE = SportType.DURATION;

    @Mock
    private SportService sportService;

    @Mock
    private AdminService adminService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SportController sportController;

    /**
     * Test : listSports() affiche la liste de tous les sports
     */
    @Test
    void shouldListAllSports() {
        Sport sport1 = createSport(1, "Course", "Running", 500, SportType.DURATION);
        Sport sport2 = createSport(2, "Natation", "Swimming", 400, SportType.DISTANCE);
        List<Sport> sports = List.of(sport1, sport2);

        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(sportService.findAll()).thenReturn(sports);

        String viewName = sportController.listSports(model, authentication);

        assertEquals("admin/sport/list", viewName);
        verify(adminService, times(1)).checkAdminLoggedIn(authentication);
        verify(sportService, times(1)).findAll();
        verify(model, times(1)).addAttribute("sports", sports);
    }

    /**
     * Test : listSports() affiche une liste vide
     */
    @Test
    void shouldListEmptySports() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(sportService.findAll()).thenReturn(List.of());

        String viewName = sportController.listSports(model, authentication);

        assertEquals("admin/sport/list", viewName);
        verify(adminService, times(1)).checkAdminLoggedIn(authentication);
        verify(sportService, times(1)).findAll();
        verify(model, times(1)).addAttribute("sports", List.of());
    }

    /**
     * Test : showCreateForm() affiche le formulaire de création
     */
    @Test
    void shouldShowCreateForm() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);

        String viewName = sportController.showCreateForm(model, authentication);

        assertEquals("admin/sport/create", viewName);
        verify(adminService, times(1)).checkAdminLoggedIn(authentication);
        verify(model, times(1)).addAttribute(eq("sport"), any(Sport.class));
    }

    /**
     * Test : showEditForm() affiche le formulaire d'édition d'un sport existant
     */
    @Test
    void shouldShowEditFormForExistingSport() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        Sport sport = createSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        when(sportService.findById(SPORT_ID)).thenReturn(Optional.of(sport));

        String viewName = sportController.showEditForm(SPORT_ID, model, redirectAttributes, authentication);

        assertEquals("admin/sport/create", viewName);
        verify(adminService, times(1)).checkAdminLoggedIn(authentication);
        verify(sportService, times(1)).findById(SPORT_ID);
        verify(model, times(1)).addAttribute("sport", sport);
        verify(redirectAttributes, never()).addAttribute(anyString(), any());
    }

    /**
     * Test : showEditForm() redirige si le sport n'existe pas
     */
    @Test
    void shouldRedirectWhenEditingSportNotFound() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(sportService.findById(SPORT_ID)).thenReturn(Optional.empty());

        String viewName = sportController.showEditForm(SPORT_ID, model, redirectAttributes, authentication);

        assertEquals("redirect:/admin/sports", viewName);
        verify(adminService, times(1)).checkAdminLoggedIn(authentication);
        verify(sportService, times(1)).findById(SPORT_ID);
        verify(redirectAttributes, times(1)).addAttribute("error", "Sport not found");
        verify(model, never()).addAttribute(anyString(), any());
    }

    /**
     * Test : saveSport() crée un nouveau sport avec succès
     */
    @Test
    void shouldCreateSportSuccessfully() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        Sport sport = createSport(0, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        Sport savedSport = createSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);

        when(sportService.createSport(SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE))
                .thenReturn(savedSport);

        String viewName = sportController.saveSport(sport, redirectAttributes, authentication);

        assertEquals("redirect:/admin/sports", viewName);
        verify(adminService, times(1)).checkAdminLoggedIn(authentication);
        verify(sportService, times(1)).createSport(SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        verify(redirectAttributes, times(1)).addAttribute("created", true);
    }

    /**
     * Test : saveSport() met à jour un sport existant
     */
    @Test
    void shouldUpdateSportSuccessfully() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        Sport sport = createSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        Sport updatedSport = createSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);

        when(sportService.updateSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE))
                .thenReturn(updatedSport);

        String viewName = sportController.saveSport(sport, redirectAttributes, authentication);

        assertEquals("redirect:/admin/sports", viewName);
        verify(adminService, times(1)).checkAdminLoggedIn(authentication);
        verify(sportService, times(1)).updateSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        verify(redirectAttributes, times(1)).addAttribute("updated", true);
    }

    /**
     * Test : saveSport() gère les exceptions lors de la création
     */
    @Test
    void shouldHandleExceptionWhenCreatingSport() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        Sport sport = createSport(0, "", SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);

        when(sportService.createSport(eq(""), eq(SPORT_DESCRIPTION), eq(CALORIES_PER_HOUR), eq(SPORT_TYPE)))
                .thenThrow(new IllegalArgumentException("Sport name cannot be null or empty"));

        String viewName = sportController.saveSport(sport, redirectAttributes, authentication);

        assertEquals("redirect:/admin/sports/create", viewName);
        verify(adminService, times(1)).checkAdminLoggedIn(authentication);
        verify(redirectAttributes, times(1)).addAttribute("error", "Sport name cannot be null or empty");
    }

    /**
     * Test : saveSport() gère les exceptions lors de la mise à jour
     */
    @Test
    void shouldHandleExceptionWhenUpdatingSport() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        Sport sport = createSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, -100, SPORT_TYPE);

        when(sportService.updateSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, -100, SPORT_TYPE))
                .thenThrow(new IllegalArgumentException("Calories per hour must be greater than zero"));

        String viewName = sportController.saveSport(sport, redirectAttributes, authentication);

        assertEquals("redirect:/admin/sports/create", viewName);
        verify(adminService, times(1)).checkAdminLoggedIn(authentication);
        verify(redirectAttributes, times(1)).addAttribute("error", "Calories per hour must be greater than zero");
    }

    /**
     * Méthode helper pour créer un sport de test
     */
    private Sport createSport(int id, String name, String description, double caloriesPerHour, SportType type) {
        Sport sport = new Sport();
        sport.setId(id);
        sport.setName(name);
        sport.setDescription(description);
        sport.setCaloriesPerHour(caloriesPerHour);
        sport.setType(type);
        return sport;
    }
}
