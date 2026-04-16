package fr.utc.miage.sporttrack.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
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
        Athlete athlete = createAthlete(1, "testuser", "test@example.com");
        Sport sport = createSport(1, "Course à pied", "Running", 500.0, SportType.DURATION);
        Activity activity = createActivity(1, "Morning Run", 1.5, LocalDate.now(), "Paris", sport, athlete, 10.0);
        Objective objective = createObjective(1, "Run 50km", sport);

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
        verify(model).addAttribute("athlete", athlete);
        verify(model).addAttribute("totalActivities", 1);
        verify(model).addAttribute("totalDuration", 1.5);
        verify(model).addAttribute("selectedSport", null);
    }

    /**
     * Test: showDashboard should filter by selected sport
     */
    @Test
    void shouldFilterDashboardBySport() {
        Athlete athlete = createAthlete(1, "testuser", "test@example.com");
        Sport sport = createSport(1, "Course à pied", "Running", 500.0, SportType.DURATION);
        Activity activity = createActivity(1, "Morning Run", 1.5, LocalDate.now(), "Paris", sport, athlete, 10.0);
        Objective objective = createObjective(1, "Run 50km", sport);

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
        verify(model).addAttribute("selectedSportId", 1);
    }

    /**
     * Test: showDashboard should filter by date range
     */
    @Test
    void shouldFilterDashboardByDateRange() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        Athlete athlete = createAthlete(1, "testuser", "test@example.com");
        Sport sport = createSport(1, "Course à pied", "Running", 500.0, SportType.DURATION);
        Activity activity = createActivity(1, "Morning Run", 1.5, LocalDate.now(), "Paris", sport, athlete, 10.0);
        Objective objective = createObjective(1, "Run 50km", sport);

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(activity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(objectiveService.getObjectivesByUser(athlete)).thenReturn(List.of(objective));
        when(activityService.filterBySport(any(), isNull())).thenReturn(true);
        when(activityService.filterByDate(any(), any(), any())).thenReturn(true);
        when(objectiveService.isObjectiveCompleted(any(), any())).thenReturn(false);
        when(sportService.safeSportName(any())).thenReturn("Course à pied");

        String result = dashboardController.showDashboard(session, model, null, startDate, endDate);

        assertEquals("dashboard/compare", result);
        verify(model).addAttribute("startDate", startDate);
        verify(model).addAttribute("endDate", endDate);
    }

    /**
     * Test: showDashboard should calculate KPIs correctly
     */
    @Test
    void shouldCalculateKPIsCorrectly() {
        Athlete athlete = createAthlete(1, "testuser", "test@example.com");
        Sport sport = createSport(1, "Course à pied", "Running", 500.0, SportType.DURATION);
        Activity activity = createActivity(1, "Morning Run", 1.5, LocalDate.now(), "Paris", sport, athlete, 10.0);
        Objective objective = createObjective(1, "Run 50km", sport);

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
        Athlete athlete = createAthlete(1, "testuser", "test@example.com");
        Sport sport = createSport(1, "Course à pied", "Running", 500.0, SportType.DURATION);

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
        Athlete athlete = createAthlete(1, "testuser", "test@example.com");
        Sport sport = createSport(1, "Course à pied", "Running", 500.0, SportType.DURATION);
        Activity activity = createActivity(1, "Morning Run", 1.5, LocalDate.now(), "Paris", sport, athlete, 10.0);

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(activity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(activityService.filterBySport(any(), isNull())).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, null);

        assertEquals("dashboard/growth", result);
        verify(model).addAttribute("athlete", athlete);
        verify(model).addAttribute("sports", List.of(sport));
        verify(model).addAttribute("selectedSport", null);
    }

    /**
     * Test: showGrowth should filter by selected sport
     */
    @Test
    void shouldFilterGrowthBySport() {
        Athlete athlete = createAthlete(1, "testuser", "test@example.com");
        Sport sport = createSport(1, "Course à pied", "Running", 500.0, SportType.DURATION);
        Activity activity = createActivity(1, "Morning Run", 1.5, LocalDate.now(), "Paris", sport, athlete, 10.0);

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(activity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(sportService.findById(1)).thenReturn(Optional.of(sport));
        when(activityService.filterBySport(any(), any(Sport.class))).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, 1);

        assertEquals("dashboard/growth", result);
        verify(sportService).findById(1);
        verify(model).addAttribute("selectedSport", sport);
        verify(model).addAttribute("selectedSportId", 1);
    }

    /**
     * Test: showGrowth should set distance flag for distance sports
     */
    @Test
    void shouldSetDistanceFlagForDistanceSports() {
        Athlete athlete = createAthlete(1, "testuser", "test@example.com");
        Sport distanceSport = createSport(2, "Natation", "Swimming", 400.0, SportType.DISTANCE);
        Activity distanceActivity = createActivity(2, "Swimming", 1.0, LocalDate.now(), "Paris", distanceSport, athlete, 2.5);

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(distanceActivity));
        when(sportService.findAllActive()).thenReturn(List.of(distanceSport));
        when(sportService.findById(2)).thenReturn(Optional.of(distanceSport));
        when(activityService.filterBySport(any(), any(Sport.class))).thenReturn(true);

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
        Athlete athlete = createAthlete(1, "testuser", "test@example.com");
        Sport repetitionSport = createSport(3, "Musculation", "Weight training", 600.0, SportType.REPETITION);
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
        when(activityService.filterBySport(any(), any(Sport.class))).thenReturn(true);

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
        Athlete athlete = createAthlete(1, "testuser", "test@example.com");
        Sport sport = createSport(1, "Course à pied", "Running", 500.0, SportType.DURATION);
        
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
        when(activityService.filterBySport(any(), isNull())).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, null);

        assertEquals("dashboard/growth", result);
        verify(model).addAttribute(eq("consecutiveActiveWeeks"), anyInt());
    }

    /**
     * Test: showGrowth should calculate current month hours
     */
    @Test
    void shouldCalculateCurrentMonthHours() {
        Athlete athlete = createAthlete(1, "testuser", "test@example.com");
        Sport sport = createSport(1, "Course à pied", "Running", 500.0, SportType.DURATION);
        Activity currentMonthActivity = createActivity(1, "Morning Run", 2.5, LocalDate.now(), "Paris", sport, athlete, 0.0);

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(currentMonthActivity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(activityService.filterBySport(any(), isNull())).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, null);

        assertEquals("dashboard/growth", result);
        verify(model).addAttribute("hoursCurrentMonth", 2.5);
    }

    /**
     * Test: showGrowth should build weekly data with activity counts
     */
    @Test
    void shouldBuildWeeklyDataWithActivityCounts() {
        Athlete athlete = createAthlete(1, "testuser", "test@example.com");
        Sport sport = createSport(1, "Course à pied", "Running", 500.0, SportType.DURATION);
        Activity weekActivity = createActivity(1, "Morning Run", 1.5, LocalDate.now(), "Paris", sport, athlete, 10.0);

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(weekActivity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(activityService.filterBySport(any(), isNull())).thenReturn(true);

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
        Athlete athlete = createAthlete(1, "testuser", "test@example.com");
        Sport sport = createSport(1, "Course à pied", "Running", 500.0, SportType.DURATION);

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
        Athlete athlete = createAthlete(1, "testuser", "test@example.com");
        Sport sport = createSport(1, "Course à pied", "Running", 500.0, SportType.DURATION);
        Activity activity = createActivity(1, "Morning Run", 1.5, LocalDate.now(), "Paris", sport, athlete, 10.0);

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(activity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(sportService.findById(1)).thenReturn(Optional.of(sport));
        when(activityService.filterBySport(any(), any(Sport.class))).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, 1);

        assertEquals("dashboard/growth", result);
        verify(model).addAttribute("hasDistance", false);
    }

    /**
     * Test: showGrowth should not include repetition data when sport type is not REPETITION
     */
    @Test
    void shouldNotIncludeRepetitionDataForNonRepetitionSports() {
        Athlete athlete = createAthlete(1, "testuser", "test@example.com");
        Sport sport = createSport(1, "Course à pied", "Running", 500.0, SportType.DURATION);
        Activity activity = createActivity(1, "Morning Run", 1.5, LocalDate.now(), "Paris", sport, athlete, 10.0);

        when(session.getAttribute("athlete")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(List.of(activity));
        when(sportService.findAllActive()).thenReturn(List.of(sport));
        when(sportService.findById(1)).thenReturn(Optional.of(sport));
        when(activityService.filterBySport(any(), any(Sport.class))).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, 1);

        assertEquals("dashboard/growth", result);
        verify(model).addAttribute("hasRepetition", false);
    }

    /**
     * Test: showGrowth with multiple weeks of data
     */
    @Test
    void shouldHandleMultipleWeeksOfData() {
        Athlete athlete = createAthlete(1, "testuser", "test@example.com");
        Sport sport = createSport(1, "Course à pied", "Running", 500.0, SportType.DURATION);

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
        when(activityService.filterBySport(any(), isNull())).thenReturn(true);

        String result = dashboardController.showGrowth(session, model, null);

        assertEquals("dashboard/growth", result);
        verify(model).addAttribute(eq("weeklyData"), any(List.class));
    }

    // ==================== Helper Methods ====================

    private Athlete createAthlete(int id, String username, String email) {
        Athlete athlete = new Athlete();
        athlete.setUsername(username);
        athlete.setPassword("pwd");
        athlete.setEmail(email);
        setAthleteId(athlete, id);
        return athlete;
    }

    private Sport createSport(int id, String name, String description, double caloriesPerHour, SportType type) {
        Sport sport = new Sport();
        sport.setId(id);
        sport.setName(name);
        sport.setDescription(description);
        sport.setCaloriesPerHour(caloriesPerHour);
        sport.setType(type);
        return sport;
    }

    private Activity createActivity(int id, String title, double duration, LocalDate dateA, String locationCity,
                                    Sport sport, Athlete athlete, double distance) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setTitle(title);
        activity.setDuration(duration);
        activity.setDateA(dateA);
        activity.setStartTime(LocalTime.of(7, 0));
        activity.setLocationCity(locationCity);
        activity.setSportAndType(sport);
        activity.setCreatedBy(athlete);
        activity.setDistance(distance);
        return activity;
    }

    private Objective createObjective(int id, String name, Sport sport) {
        Objective objective = new Objective(name, "");
        objective.setSport(sport);
        setObjectiveId(objective, id);
        return objective;
    }

    private void setAthleteId(Athlete athlete, int id) {
        try {
            Field field = athlete.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(athlete, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
