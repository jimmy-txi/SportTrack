package fr.utc.miage.sporttrack.controller;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.activity.WeatherReport;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.service.activity.ActivityService;
import fr.utc.miage.sporttrack.service.activity.SportService;
import fr.utc.miage.sporttrack.service.activity.WeatherReportService;
import fr.utc.miage.sporttrack.service.user.AthleteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AthleteActivityControllerTest {

    @Mock
    private ActivityService activityService;

    @Mock
    private SportService sportService;

    @Mock
    private WeatherReportService weatherReportService;

    @Mock
    private AthleteService athleteService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AthleteActivityController controller;

    @Test
    void shouldDisplayAllAthleteActivities_Test36() {
        Athlete athlete = buildAthlete("athlete@mail.com", 5);
        Activity activity = new Activity();
        activity.setId(1);
        List<Activity> activities = new ArrayList<>();
        activities.add(activity);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("athlete@mail.com");
        when(athleteService.getCurrentAthlete("athlete@mail.com")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(activities);
        when(weatherReportService.findByActivityId(1)).thenReturn(Optional.empty());

        String view = controller.listMyActivities(model, authentication);

        assertEquals("athlete/activity/list", view);
        verify(model).addAttribute("activities", activities);
    }

    @Test
    void shouldDisplayMessageContextWhenNoActivityExists_Test37() {
        Athlete athlete = buildAthlete("athlete@mail.com", 5);
        List<Activity> emptyActivities = List.of();

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("athlete@mail.com");
        when(athleteService.getCurrentAthlete("athlete@mail.com")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(emptyActivities);

        String view = controller.listMyActivities(model, authentication);

        assertEquals("athlete/activity/list", view);
        verify(model).addAttribute("activities", emptyActivities);
    }

    @Test
    void shouldAttachWeatherReportToActivity_Test62() {
        Athlete athlete = buildAthlete("athlete@mail.com", 7);
        Activity activity = new Activity();
        activity.setId(12);
        List<Activity> activities = new ArrayList<>();
        activities.add(activity);

        WeatherReport weatherReport = new WeatherReport();
        weatherReport.setWeatherCode(0);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("athlete@mail.com");
        when(athleteService.getCurrentAthlete("athlete@mail.com")).thenReturn(athlete);
        when(activityService.findAllByAthlete(athlete)).thenReturn(activities);
        when(weatherReportService.findByActivityId(12)).thenReturn(Optional.of(weatherReport));

        String view = controller.listMyActivities(model, authentication);

        assertEquals("athlete/activity/list", view);
        assertNotNull(activity.getWeatherReport());
        assertEquals(0, activity.getWeatherReport().getWeatherCode());

        ArgumentCaptor<List<Activity>> captor = ArgumentCaptor.forClass(List.class);
        verify(model).addAttribute(org.mockito.ArgumentMatchers.eq("activities"), captor.capture());
        assertEquals(1, captor.getValue().size());
    }

    private Athlete buildAthlete(String email, int id) {
        Athlete athlete = new Athlete();
        athlete.setUsername("athlete");
        athlete.setPassword("pwd");
        athlete.setEmail(email);
        try {
            java.lang.reflect.Field idField = athlete.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(athlete, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return athlete;
    }
}
