package fr.utc.miage.sporttrack.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import fr.utc.miage.sporttrack.dto.BadgeFormDTO;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.entity.event.Badge;
import fr.utc.miage.sporttrack.repository.activity.SportRepository;
import fr.utc.miage.sporttrack.service.activity.SportService;
import fr.utc.miage.sporttrack.service.event.BadgeService;
import fr.utc.miage.sporttrack.service.user.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ExtendWith(MockitoExtension.class)
class AdminBadgeControllerTest {

    @Mock
    private BadgeService badgeService;

    @Mock
    private SportService sportService;

    @Mock
    private AdminService adminService;

    @Mock
    private SportRepository sportRepository;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminBadgeController controller;

    private Sport sport;
    private Badge badge;

    @BeforeEach
    void setUp() {
        sport = new Sport();
        sport.setId(1);
        sport.setName("Course");

        badge = new Badge();
        badge.setId(1);
        badge.setLabel("Courreur 50km");
        badge.setDescription("Cumulez 50 km");
        badge.setSport(sport);
        badge.setMetric(Metric.DISTANCE);
        badge.setThreshold(50.0);
        badge.setIcon("bi-trophy");
    }

    // ========== listBadges ==========

    @Test
    void testListBadgesAdminAuthenticated() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(badgeService.findAll()).thenReturn(List.of(badge));

        String viewName = controller.listBadges(model, authentication);

        assertEquals("admin/badge/list", viewName);
        verify(model).addAttribute("badges", List.of(badge));
    }

    @Test
    void testListBadgesAdminNotAuthenticated() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(false);

        String viewName = controller.listBadges(model, authentication);

        assertEquals("redirect:/login", viewName);
        verifyNoInteractions(badgeService);
    }

    // ========== showCreateForm ==========

    @Test
    void testShowCreateFormAdminAuthenticated() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(sportService.findAllActive()).thenReturn(Collections.emptyList());

        String viewName = controller.showCreateForm(model, authentication);

        assertEquals("admin/badge/create", viewName);
        verify(model).addAttribute(eq("badge"), any(BadgeFormDTO.class));
        verify(model).addAttribute("sports", Collections.emptyList());
        verify(model).addAttribute(eq("metrics"), any(Metric[].class));
        verify(model).addAttribute(eq("icons"), any(List.class));
    }

    @Test
    void testShowCreateFormAdminNotAuthenticated() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(false);

        String viewName = controller.showCreateForm(model, authentication);

        assertEquals("redirect:/login", viewName);
        verifyNoInteractions(sportService);
    }

    // ========== showEditForm ==========

    @Test
    void testShowEditFormBadgeExists() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(badgeService.findById(1)).thenReturn(badge);
        when(sportService.findAllActive()).thenReturn(Collections.emptyList());

        String viewName = controller.showEditForm(1, model, redirectAttributes, authentication);

        assertEquals("admin/badge/create", viewName);
        verify(model).addAttribute(eq("badge"), any(BadgeFormDTO.class));
        verify(model).addAttribute("sports", Collections.emptyList());
    }

    @Test
    void testShowEditFormBadgeWithNullSport() {
        badge.setSport(null);

        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(badgeService.findById(1)).thenReturn(badge);
        when(sportService.findAllActive()).thenReturn(Collections.emptyList());

        String viewName = controller.showEditForm(1, model, redirectAttributes, authentication);

        assertEquals("admin/badge/create", viewName);
        verify(model).addAttribute(eq("badge"), any(BadgeFormDTO.class));
    }

    @Test
    void testShowEditFormBadgeNotFound() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(badgeService.findById(99)).thenThrow(new IllegalArgumentException("Badge not found with id: 99"));

        String viewName = controller.showEditForm(99, model, redirectAttributes, authentication);

        assertEquals("redirect:/admin/badges", viewName);
        verify(redirectAttributes).addAttribute(eq("error"), eq("Badge not found with id: 99"));
    }

    @Test
    void testShowEditFormAdminNotAuthenticated() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(false);

        String viewName = controller.showEditForm(1, model, redirectAttributes, authentication);

        assertEquals("redirect:/login", viewName);
        verifyNoInteractions(badgeService);
    }

    // ========== saveBadge ==========

    @Test
    void testSaveBadgeValidData() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(sportRepository.findById(1)).thenReturn(Optional.of(sport));

        BadgeFormDTO dto = new BadgeFormDTO();
        dto.setId(0);
        dto.setLabel("Test Badge");
        dto.setDescription("Description");
        dto.setSportId(1);
        dto.setMetric(Metric.DISTANCE);
        dto.setThreshold(50.0);
        dto.setIcon("bi-trophy");

        String viewName = controller.saveBadge(dto, redirectAttributes, authentication);

        assertEquals("redirect:/admin/badges", viewName);
        verify(badgeService).saveBadge(any(Badge.class), eq(sport));
        verify(redirectAttributes).addAttribute("saved", true);
    }

    @Test
    void testSaveBadgeNullLabel() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(sportRepository.findById(1)).thenReturn(Optional.of(sport));

        BadgeFormDTO dto = new BadgeFormDTO();
        dto.setLabel(null);
        dto.setSportId(1);
        dto.setIcon("bi-trophy");
        dto.setMetric(Metric.DISTANCE);
        dto.setThreshold(50.0);

        String viewName = controller.saveBadge(dto, redirectAttributes, authentication);

        assertEquals("redirect:/admin/badges/create", viewName);
        verify(redirectAttributes).addAttribute(eq("error"), eq("Badge label is required"));
        verify(badgeService, never()).saveBadge(any(), any());
    }

    @Test
    void testSaveBadgeBlankLabel() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(sportRepository.findById(1)).thenReturn(Optional.of(sport));

        BadgeFormDTO dto = new BadgeFormDTO();
        dto.setLabel("   ");
        dto.setSportId(1);
        dto.setIcon("bi-trophy");
        dto.setMetric(Metric.DISTANCE);
        dto.setThreshold(50.0);

        String viewName = controller.saveBadge(dto, redirectAttributes, authentication);

        assertEquals("redirect:/admin/badges/create", viewName);
        verify(redirectAttributes).addAttribute(eq("error"), eq("Badge label is required"));
    }

    @Test
    void testSaveBadgeNullIcon() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(sportRepository.findById(1)).thenReturn(Optional.of(sport));

        BadgeFormDTO dto = new BadgeFormDTO();
        dto.setLabel("Badge");
        dto.setSportId(1);
        dto.setIcon(null);
        dto.setMetric(Metric.DISTANCE);
        dto.setThreshold(50.0);

        String viewName = controller.saveBadge(dto, redirectAttributes, authentication);

        assertEquals("redirect:/admin/badges/create", viewName);
        verify(redirectAttributes).addAttribute(eq("error"), eq("Badge icon is required"));
    }

    @Test
    void testSaveBadgeBlankIcon() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(sportRepository.findById(1)).thenReturn(Optional.of(sport));

        BadgeFormDTO dto = new BadgeFormDTO();
        dto.setLabel("Badge");
        dto.setSportId(1);
        dto.setIcon("   ");
        dto.setMetric(Metric.DISTANCE);
        dto.setThreshold(50.0);

        String viewName = controller.saveBadge(dto, redirectAttributes, authentication);

        assertEquals("redirect:/admin/badges/create", viewName);
        verify(redirectAttributes).addAttribute(eq("error"), eq("Badge icon is required"));
    }

    @Test
    void testSaveBadgeNullMetric() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(sportRepository.findById(1)).thenReturn(Optional.of(sport));

        BadgeFormDTO dto = new BadgeFormDTO();
        dto.setLabel("Badge");
        dto.setSportId(1);
        dto.setIcon("bi-trophy");
        dto.setMetric(null);
        dto.setThreshold(50.0);

        String viewName = controller.saveBadge(dto, redirectAttributes, authentication);

        assertEquals("redirect:/admin/badges/create", viewName);
        verify(redirectAttributes).addAttribute(eq("error"), eq("Badge metric is required"));
    }

    @Test
    void testSaveBadgeZeroThreshold() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(sportRepository.findById(1)).thenReturn(Optional.of(sport));

        BadgeFormDTO dto = new BadgeFormDTO();
        dto.setLabel("Badge");
        dto.setSportId(1);
        dto.setIcon("bi-trophy");
        dto.setMetric(Metric.DISTANCE);
        dto.setThreshold(0.0);

        String viewName = controller.saveBadge(dto, redirectAttributes, authentication);

        assertEquals("redirect:/admin/badges/create", viewName);
        verify(redirectAttributes).addAttribute(eq("error"), eq("Threshold must be greater than zero"));
    }

    @Test
    void testSaveBadgeNegativeThreshold() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(sportRepository.findById(1)).thenReturn(Optional.of(sport));

        BadgeFormDTO dto = new BadgeFormDTO();
        dto.setLabel("Badge");
        dto.setSportId(1);
        dto.setIcon("bi-trophy");
        dto.setMetric(Metric.DISTANCE);
        dto.setThreshold(-5.0);

        String viewName = controller.saveBadge(dto, redirectAttributes, authentication);

        assertEquals("redirect:/admin/badges/create", viewName);
        verify(redirectAttributes).addAttribute(eq("error"), eq("Threshold must be greater than zero"));
    }

    @Test
    void testSaveBadgeSportNotFound() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(sportRepository.findById(99)).thenReturn(Optional.empty());

        BadgeFormDTO dto = new BadgeFormDTO();
        dto.setLabel("Badge");
        dto.setSportId(99);
        dto.setIcon("bi-trophy");
        dto.setMetric(Metric.DISTANCE);
        dto.setThreshold(50.0);

        String viewName = controller.saveBadge(dto, redirectAttributes, authentication);

        assertEquals("redirect:/admin/badges/create", viewName);
        verify(redirectAttributes).addAttribute(eq("error"), eq("Sport not found with id: 99"));
    }

    @Test
    void testSaveBadgeAdminNotAuthenticated() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(false);

        BadgeFormDTO dto = new BadgeFormDTO();
        String viewName = controller.saveBadge(dto, redirectAttributes, authentication);

        assertEquals("redirect:/login", viewName);
        verifyNoInteractions(badgeService);
        verifyNoInteractions(sportRepository);
    }

    @Test
    void testSaveBadgeWithNullId() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        when(sportRepository.findById(1)).thenReturn(Optional.of(sport));

        BadgeFormDTO dto = new BadgeFormDTO();
        dto.setId(null);
        dto.setLabel("New Badge");
        dto.setSportId(1);
        dto.setIcon("bi-star-fill");
        dto.setMetric(Metric.DURATION);
        dto.setThreshold(100.0);

        String viewName = controller.saveBadge(dto, redirectAttributes, authentication);

        assertEquals("redirect:/admin/badges", viewName);
        verify(badgeService).saveBadge(any(Badge.class), eq(sport));
    }

    // ========== deleteBadge ==========

    @Test
    void testDeleteBadgeExists() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);

        String viewName = controller.deleteBadge(1, redirectAttributes, authentication);

        assertEquals("redirect:/admin/badges", viewName);
        verify(badgeService).deleteById(1);
        verify(redirectAttributes).addAttribute("deleted", true);
    }

    @Test
    void testDeleteBadgeNotExists() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(true);
        doThrow(new IllegalArgumentException("Badge not found with id: 99"))
                .when(badgeService).deleteById(99);

        String viewName = controller.deleteBadge(99, redirectAttributes, authentication);

        assertEquals("redirect:/admin/badges", viewName);
        verify(redirectAttributes).addAttribute(eq("error"), eq("Badge not found with id: 99"));
    }

    @Test
    void testDeleteBadgeAdminNotAuthenticated() {
        when(adminService.checkAdminLoggedIn(authentication)).thenReturn(false);

        String viewName = controller.deleteBadge(1, redirectAttributes, authentication);

        assertEquals("redirect:/login", viewName);
        verifyNoInteractions(badgeService);
    }
}