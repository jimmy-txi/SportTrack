package fr.utc.miage.sporttrack.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

import fr.utc.miage.sporttrack.dto.SportFormDTO;
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

    // =====================================================================
    // listSports
    // =====================================================================

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
     * Test : listSports() redirige si non authentifié
     */
    @Test
    void shouldRedirectToLoginOnListSportsWhenNotAdmin() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(false);

        String viewName = sportController.listSports(model, authentication);

        assertEquals("redirect:/login", viewName);
        verifyNoInteractions(sportService);
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

    // =====================================================================
    // showCreateForm
    // =====================================================================

    /**
     * Test : showCreateForm() affiche le formulaire de création
     */
    @Test
    void shouldShowCreateForm() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);

        String viewName = sportController.showCreateForm(model, authentication);

        assertEquals("admin/sport/create", viewName);
        verify(adminService, times(1)).checkAdminLoggedIn(authentication);
        verify(model, times(1)).addAttribute(eq("sport"), any(SportFormDTO.class));
    }

    /**
     * Test : showCreateForm() redirige si non authentifié
     */
    @Test
    void shouldRedirectToLoginOnShowCreateFormWhenNotAdmin() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(false);

        String viewName = sportController.showCreateForm(model, authentication);

        assertEquals("redirect:/login", viewName);
        verifyNoInteractions(sportService);
    }

    // =====================================================================
    // showEditForm
    // =====================================================================

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
        verify(model, times(1)).addAttribute(eq("sport"), any(SportFormDTO.class));
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
     * Test : showEditForm() redirige si non authentifié
     */
    @Test
    void shouldRedirectToLoginOnShowEditFormWhenNotAdmin() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(false);

        String viewName = sportController.showEditForm(SPORT_ID, model, redirectAttributes, authentication);

        assertEquals("redirect:/login", viewName);
        verifyNoInteractions(sportService);
    }

    // =====================================================================
    // saveSport
    // =====================================================================

    /**
     * Test : saveSport() crée un nouveau sport avec succès
     */
    @Test
    void shouldCreateSportSuccessfully() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        SportFormDTO dto = createSportFormDTO(0, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        Sport savedSport = createSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);

        when(sportService.createSport(SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE))
                .thenReturn(savedSport);

        String viewName = sportController.saveSport(dto, redirectAttributes, authentication);

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
        SportFormDTO dto = createSportFormDTO(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);
        Sport updatedSport = createSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);

        when(sportService.updateSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE))
                .thenReturn(updatedSport);

        String viewName = sportController.saveSport(dto, redirectAttributes, authentication);

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
        SportFormDTO dto = createSportFormDTO(0, "", SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);

        when(sportService.createSport(eq(""), eq(SPORT_DESCRIPTION), eq(CALORIES_PER_HOUR), eq(SPORT_TYPE)))
                .thenThrow(new IllegalArgumentException("Sport name cannot be null or empty"));

        String viewName = sportController.saveSport(dto, redirectAttributes, authentication);

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
        SportFormDTO dto = createSportFormDTO(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, -100, SPORT_TYPE);

        when(sportService.updateSport(SPORT_ID, SPORT_NAME, SPORT_DESCRIPTION, -100, SPORT_TYPE))
                .thenThrow(new IllegalArgumentException("Calories per hour must be greater than zero"));

        String viewName = sportController.saveSport(dto, redirectAttributes, authentication);

        assertEquals("redirect:/admin/sports/create", viewName);
        verify(adminService, times(1)).checkAdminLoggedIn(authentication);
        verify(redirectAttributes, times(1)).addAttribute("error", "Calories per hour must be greater than zero");
    }

    /**
     * Test : saveSport() redirige si non authentifié
     */
    @Test
    void shouldRedirectToLoginOnSaveSportWhenNotAdmin() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(false);
        SportFormDTO dto = createSportFormDTO(0, SPORT_NAME, SPORT_DESCRIPTION, CALORIES_PER_HOUR, SPORT_TYPE);

        String viewName = sportController.saveSport(dto, redirectAttributes, authentication);

        assertEquals("redirect:/login", viewName);
        verifyNoInteractions(sportService);
    }

    // =====================================================================
    // enable / disable
    // =====================================================================

    /**
     * Test : enable() active un sport avec succès
     */
    @Test
    void shouldEnableSportSuccessfully() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        doNothing().when(sportService).enableSport(SPORT_ID);

        String viewName = sportController.enable(SPORT_ID, authentication);

        assertEquals("redirect:/admin/sports", viewName);
        verify(adminService, times(1)).checkAdminLoggedIn(authentication);
        verify(sportService, times(1)).enableSport(SPORT_ID);
    }

    /**
     * Test : enable() redirige vers login si non authentifié
     */
    @Test
    void shouldRedirectToLoginWhenEnablingWithoutAuth() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(false);

        String viewName = sportController.enable(SPORT_ID, authentication);

        assertEquals("redirect:/login", viewName);
        verify(sportService, never()).enableSport(SPORT_ID);
    }

    /**
     * Test : disable() désactive un sport avec succès
     */
    @Test
    void shouldDisableSportSuccessfully() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        doNothing().when(sportService).disableSport(SPORT_ID);

        String viewName = sportController.disable(SPORT_ID, authentication);

        assertEquals("redirect:/admin/sports", viewName);
        verify(adminService, times(1)).checkAdminLoggedIn(authentication);
        verify(sportService, times(1)).disableSport(SPORT_ID);
    }

    /**
     * Test : disable() redirige vers login si non authentifié
     */
    @Test
    void shouldRedirectToLoginWhenDisablingWithoutAuth() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(false);

        String viewName = sportController.disable(SPORT_ID, authentication);

        assertEquals("redirect:/login", viewName);
        verify(sportService, never()).disableSport(SPORT_ID);
    }

    /**
     * Test : enable() gère les exceptions lors de l'activation
     */
    @Test
    void shouldHandleExceptionWhenEnablingSport() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        doThrow(new IllegalArgumentException("Sport not found with id: " + SPORT_ID))
                .when(sportService).enableSport(SPORT_ID);

        assertThrows(IllegalArgumentException.class, () -> {
            sportController.enable(SPORT_ID, authentication);
        });
        verify(adminService, times(1)).checkAdminLoggedIn(authentication);
        verify(sportService, times(1)).enableSport(SPORT_ID);
    }

    /**
     * Test : disable() gère les exceptions lors de la désactivation
     */
    @Test
    void shouldHandleExceptionWhenDisablingSport() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        doThrow(new IllegalArgumentException("Sport not found with id: " + SPORT_ID))
                .when(sportService).disableSport(SPORT_ID);

        assertThrows(IllegalArgumentException.class, () -> {
            sportController.disable(SPORT_ID, authentication);
        });
        verify(adminService, times(1)).checkAdminLoggedIn(authentication);
        verify(sportService, times(1)).disableSport(SPORT_ID);
    }

    // =====================================================================
    // Helpers
    // =====================================================================

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

    /**
     * Méthode helper pour créer un SportFormDTO de test
     */
    private SportFormDTO createSportFormDTO(int id, String name, String description, double caloriesPerHour, SportType type) {
        SportFormDTO dto = new SportFormDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription(description);
        dto.setCaloriesPerHour(caloriesPerHour);
        dto.setType(type);
        return dto;
    }
}
