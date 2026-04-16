package fr.utc.miage.sporttrack.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.SportType;
import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.service.activity.ActivityService;
import fr.utc.miage.sporttrack.service.activity.SportService;
import fr.utc.miage.sporttrack.service.event.ObjectiveService;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private ActivityService activityService;

    @Mock
    private ObjectiveService objectiveService;

    @Mock
    private SportService sportService;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private Model model;

    @Mock
    private HttpSession session;

    @InjectMocks
    private DashboardController dashboardController;

    private Athlete athlete;
    private Sport sport;
    private Activity activity;
    private Objective objective;

    @BeforeEach
    void setUp() {
        // Setup athlete
        athlete = new Athlete();
        athlete.setUsername("testuser");
        athlete.setPassword("pwd");
        athlete.setEmail("test@example.com");
        setAthleteId(athlete, 1);

        // Setup sport
        sport = new Sport();
        sport.setId(1);
        sport.setName("Course à pied");
        sport.setType(SportType.DURATION);
        sport.setCaloriesPerHour(500.0);

        // Setup activity
        activity = new Activity();
        activity.setId(1);
        activity.setTitle("Morning Run");
        activity.setDuration(1.5);
        activity.setDateA(LocalDate.now());
        activity.setStartTime(LocalTime.of(7, 0));
        activity.setLocationCity("Paris");
        activity.setSportAndType(sport);
        activity.setCreatedBy(athlete);
        activity.setDistance(10.0);

        // Setup objective
        objective = new Objective("Run 50km", "");
        objective.setSport(sport);
        setObjectiveId(objective, 1);
    }

    // ==================== showDashboard Tests ====================

    /**
     * Test: showDashboard should redirect to login when athlete is not authenticated
     */
    @Test
    void shouldRedirectToLoginWhenNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);

        String result = dashboardController.showDashboard(session, model, null, null, null);

        assertEquals("redirect:/login", result);
        verify(session, times(1)).getAttribute("athlete");
        verify(model, never()).addAttribute(anyString(), any());
    }

    /**
     * Test: showDashboard should display dashboard with all sports when no sport is selected
     */
    @Test
    void shouldShowDashboardWithAllSports() {
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(activity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(objectiveService.getObjectivesByUser(athlete)).thenReturn(List.of(objective));
        when(activityService.filterBySport(any(), isNull())).thenReturn(true);
        when(activityService.filterByDate(any(), isNull(), isNull())).thenReturn(true);
        when(objectiveService.isObjectiveCompleted(any(), any())).thenReturn(false);
        when(sportService.safeSportName(any())).thenReturn("Course à pied");

        String result = dashboardController.showDashboard(session, model, null, null, null);

        assertEquals("dashboard/compare", result);
        verify(model).addAttribute(eq("athlete"), eq(athlete));
        verify(model).addAttribute(eq("totalActivities"), eq(1));
        verify(model).addAttribute(eq("totalDuration"), eq(1.5));
        verify(model).addAttribute(eq("selectedSport"), isNull());
    }

    /**
     * Test: showDashboard should filter by selected sport
     */
    @Test
    void shouldFilterDashboardBySport() {
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(activity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(sportService.findById(1)).thenReturn(Optional.of(sport));
        when(objectiveService.getObjectivesByUser(athlete)).thenReturn(List.of(objective));
        when(activityService.filterBySport(any(), any(Sport.class))).thenReturn(true);
        when(activityService.filterByDate(any(), isNull(), isNull())).thenReturn(true);
        when(objectiveService.isObjectiveCompleted(any(), any())).thenReturn(false);
        when(sportService.safeSportName(any())).thenReturn("Course à pied");

        String result = dashboardController.showDashboard(session, model, 1, null, null);

        assertEquals("dashboard/compare", result);
        verify(sportService).findById(1);
        verify(model).addAttribute("selectedSport", sport);
        verify(model).addAttribute(eq("selectedSportId"), eq(1));
    }

    /**
     * Test: showDashboard should filter by date range
     */
    @Test
    void shouldFilterDashboardByDateRange() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(activity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(objectiveService.getObjectivesByUser(athlete)).thenReturn(List.of(objective));
        when(activityService.filterBySport(any(), isNull())).thenReturn(true);
        when(activityService.filterByDate(any(), eq(startDate), eq(endDate))).thenReturn(true);
        when(objectiveService.isObjectiveCompleted(any(), any())).thenReturn(false);
        when(sportService.safeSportName(any())).thenReturn("Course à pied");

        String result = dashboardController.showDashboard(session, model, null, startDate, endDate);

        assertEquals("dashboard/compare", result);
        verify(activityService).filterByDate(any(), eq(startDate), eq(endDate));
        verify(model).addAttribute("startDate", startDate);
        verify(model).addAttribute("endDate", endDate);
    }

    /**
     * Test: showDashboard should calculate KPIs correctly
     */
    @Test
    void shouldCalculateKPIsCorrectly() {
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(activity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(objectiveService.getObjectivesByUser(athlete)).thenReturn(List.of(objective));
        when(activityService.filterBySport(any(), isNull())).thenReturn(true);
        when(activityService.filterByDate(any(), isNull(), isNull())).thenReturn(true);
        when(objectiveService.isObjectiveCompleted(any(), any())).thenReturn(true);
        when(sportService.safeSportName(any())).thenReturn("Course à pied");

        String result = dashboardController.showDashboard(session, model, null, null, null);

        assertEquals("dashboard/compare", result);
        verify(model).addAttribute("totalActivities", 1);
        verify(model).addAttribute("totalDuration", 1.5);
        verify(model).addAttribute("totalDistance", 10.0);
        verify(model).addAttribute("objectivesCompleted", 1L);
    }

    /**
     * Test: showDashboard with empty activities
     */
    @Test
    void shouldHandleEmptyActivities() {
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of());
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(objectiveService.getObjectivesByUser(athlete)).thenReturn(List.of());

        String result = dashboardController.showDashboard(session, model, null, null, null);

        assertEquals("dashboard/compare", result);
        verify(model).addAttribute("totalActivities", 0);
        verify(model).addAttribute("totalDuration", 0.0);
    }

    // ==================== showGrowth Tests ====================

    /**
     * Test: showGrowth should redirect to login when athlete is not authenticated
     */
    @Test
    void shouldRedirectToLoginForGrowthWhenNotAuthenticated() {
        when(session.getAttribute("athlete")).thenReturn(null);

        String result = dashboardController.showGrowth(session, model, null);

        assertEquals("redirect:/login", result);
        verify(session, times(1)).getAttribute("athlete");
    }

    /**
     * Test: showGrowth should display growth page with all sports when no sport is selected
     */
    @Test
    void shouldShowGrowthWithAllSports() {
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(activity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(activityService.filterBySport(any(), isNull())).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, null);

        assertEquals("dashboard/growth", result);
        verify(model).addAttribute(eq("athlete"), eq(athlete));
        verify(model).addAttribute(eq("sports"), eq(List.of(sport)));
        verify(model).addAttribute(eq("selectedSport"), isNull());
    }

    /**
     * Test: showGrowth should filter by selected sport
     */
    @Test
    void shouldFilterGrowthBySport() {
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(activity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(sportService.findById(1)).thenReturn(Optional.of(sport));
        when(activityService.filterBySport(any(), any(Sport.class))).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, 1);

        assertEquals("dashboard/growth", result);
        verify(sportService).findById(1);
        verify(model).addAttribute("selectedSport", sport);
        verify(model).addAttribute(eq("selectedSportId"), eq(1));
    }

    /**
     * Test: showGrowth should set distance flag for distance sports
     */
    @Test
    void shouldSetDistanceFlagForDistanceSports() {
        Sport distanceSport = new Sport();
        distanceSport.setId(2);
        distanceSport.setName("Natation");
        distanceSport.setType(SportType.DISTANCE);

        Activity distanceActivity = new Activity();
        distanceActivity.setId(2);
        distanceActivity.setTitle("Swimming");
        distanceActivity.setDuration(1.0);
        distanceActivity.setDateA(LocalDate.now());
        distanceActivity.setStartTime(LocalTime.of(8, 0));
        distanceActivity.setLocationCity("Paris");
        distanceActivity.setSportAndType(distanceSport);
        distanceActivity.setCreatedBy(athlete);
        distanceActivity.setDistance(2.5);

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(distanceActivity));
        when(sportService.findAllActive()).thenReturn(List.of(distanceSport));
        when(sportService.findById(2)).thenReturn(Optional.of(distanceSport));
        when(activityService.filterBySport(distanceActivity, distanceSport)).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, 2);

        assertEquals("dashboard/growth", result);
        verify(model).addAttribute("hasDistance", true);
        verify(model).addAttribute("hasRepetition", false);
    }

    /**
     * Test: showGrowth should set repetition flag for repetition sports
     */
    @Test
    void shouldSetRepetitionFlagForRepetitionSports() {
        Sport repetitionSport = new Sport();
        repetitionSport.setId(3);
        repetitionSport.setName("Musculation");
        repetitionSport.setType(SportType.REPETITION);

        Activity repetitionActivity = new Activity();
        repetitionActivity.setId(3);
        repetitionActivity.setTitle("Weight training");
        repetitionActivity.setDuration(1.5);
        repetitionActivity.setDateA(LocalDate.now());
        repetitionActivity.setStartTime(LocalTime.of(9, 0));
        repetitionActivity.setLocationCity("Paris");
        repetitionActivity.setSportAndType(repetitionSport);
        repetitionActivity.setCreatedBy(athlete);
        repetitionActivity.setRepetition(20);

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(repetitionActivity));
        when(sportService.findAllActive()).thenReturn(List.of(repetitionSport));
        when(sportService.findById(3)).thenReturn(Optional.of(repetitionSport));
        when(activityService.filterBySport(repetitionActivity, repetitionSport)).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, 3);

        assertEquals("dashboard/growth", result);
        verify(model).addAttribute("hasRepetition", true);
        verify(model).addAttribute("hasDistance", false);
    }

    /**
     * Test: showGrowth should calculate consecutive active weeks
     */
    @Test
    void shouldCalculateConsecutiveActiveWeeks() {
        Activity activity1 = new Activity();
        activity1.setDateA(LocalDate.now());
        activity1.setDuration(1.0);
        activity1.setSportAndType(sport);
        activity1.setCreatedBy(athlete);

        Activity activity2 = new Activity();
        activity2.setDateA(LocalDate.now().minusWeeks(1));
        activity2.setDuration(1.0);
        activity2.setSportAndType(sport);
        activity2.setCreatedBy(athlete);

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(activity1, activity2));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(activityService.filterBySport(any(Activity.class), isNull())).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, null);

        assertEquals("dashboard/growth", result);
        verify(model).addAttribute(eq("consecutiveActiveWeeks"), anyInt());
    }

    /**
     * Test: showGrowth should calculate current month hours
     */
    @Test
    void shouldCalculateCurrentMonthHours() {
        Activity currentMonthActivity = new Activity();
        currentMonthActivity.setDateA(LocalDate.now());
        currentMonthActivity.setDuration(2.5);
        currentMonthActivity.setSportAndType(sport);
        currentMonthActivity.setCreatedBy(athlete);

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(currentMonthActivity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(activityService.filterBySport(currentMonthActivity, null)).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, null);

        assertEquals("dashboard/growth", result);
        verify(model).addAttribute("hoursCurrentMonth", 2.5);
    }

    /**
     * Test: showGrowth should build weekly data with activity counts
     */
    @Test
    void shouldBuildWeeklyDataWithActivityCounts() {
        Activity weekActivity = new Activity();
        weekActivity.setDateA(LocalDate.now());
        weekActivity.setDuration(1.5);
        weekActivity.setSportAndType(sport);
        weekActivity.setCreatedBy(athlete);

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(weekActivity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(activityService.filterBySport(weekActivity, null)).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, null);

        assertEquals("dashboard/growth", result);
        verify(model).addAttribute(eq("weeklyData"), any(List.class));
        verify(model).addAttribute(eq("weekLabels"), any(List.class));
        verify(model).addAttribute(eq("weekDurations"), any(List.class));
        verify(model).addAttribute(eq("weekActivityCounts"), any(List.class));
    }

    /**
     * Test: showGrowth with empty activities
     */
    @Test
    void shouldHandleEmptyActivitiesForGrowth() {
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of());
        when(sportService.findAllActive()).thenReturn(List.of(sport));

        String result = dashboardController.showGrowth(session, model, null);

        assertEquals("dashboard/growth", result);
        verify(model).addAttribute("consecutiveActiveWeeks", 0);
    }

    /**
     * Test: showGrowth should not include distance data when sport type is not DISTANCE
     */
    @Test
    void shouldNotIncludeDistanceDataForNonDistanceSports() {
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(activity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(sportService.findById(1)).thenReturn(Optional.of(sport));
        when(activityService.filterBySport(activity, sport)).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, 1);

        assertEquals("dashboard/growth", result);
        verify(model).addAttribute("hasDistance", false);
    }

    /**
     * Test: showGrowth should not include repetition data when sport type is not REPETITION
     */
    @Test
    void shouldNotIncludeRepetitionDataForNonRepetitionSports() {
        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(activity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(sportService.findById(1)).thenReturn(Optional.of(sport));
        when(activityService.filterBySport(activity, sport)).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, 1);

        assertEquals("dashboard/growth", result);
        verify(model).addAttribute("hasRepetition", false);
    }

    /**
     * Test: showGrowth with multiple weeks of data
     */
    @Test
    void shouldHandleMultipleWeeksOfData() {
        Activity weekOne = new Activity();
        weekOne.setDateA(LocalDate.now());
        weekOne.setDuration(2.0);
        weekOne.setSportAndType(sport);
        weekOne.setCreatedBy(athlete);

        Activity weekTwo = new Activity();
        weekTwo.setDateA(LocalDate.now().minusWeeks(1));
        weekTwo.setDuration(1.5);
        weekTwo.setSportAndType(sport);
        weekTwo.setCreatedBy(athlete);

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(weekOne, weekTwo));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(activityService.filterBySport(any(Activity.class), isNull())).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, null);

        assertEquals("dashboard/growth", result);
        verify(model).addAttribute(eq("weeklyData"), any(List.class));
    }

    // ==================== Helper Methods ====================

    /**
     * Helper method to create a Sport object
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
     * Helper method to set athlete id using reflection
     */
    private void setAthleteId(Athlete athlete, int id) {
        try {
            Field field = athlete.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(athlete, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method to set objective id using reflection
     */
    private void setObjectiveId(Objective objective, int id) {
        try {
            Field field = objective.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(objective, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}












