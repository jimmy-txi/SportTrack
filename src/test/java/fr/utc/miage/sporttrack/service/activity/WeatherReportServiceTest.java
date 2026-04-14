package fr.utc.miage.sporttrack.service.activity;

import fr.utc.miage.sporttrack.dto.MeteoDTO;
import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.activity.WeatherReport;
import fr.utc.miage.sporttrack.repository.activity.WeatherReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherReportServiceTest {

    @Mock
    private WeatherReportRepository weatherReportRepository;

    @Spy
    @InjectMocks
    private WeatherReportService weatherReportService;

    @Test
    void shouldSaveWeatherReportForActivity_Test62() {
        Activity activity = new Activity();
        activity.setId(10);
        activity.setLocationCity("Compiegne");
        activity.setDateA(LocalDate.now());

        MeteoDTO dto = new MeteoDTO();
        dto.setValid(true);
        dto.setCity("Compiegne");
        dto.setCountry("France");
        dto.setWeatherCode(3);
        dto.setPrecipitationSum(1.2);

        doReturn(dto).when(weatherReportService).getWeatherForActivity("Compiegne", activity.getDateA());
        when(weatherReportRepository.findByActivity_Id(10)).thenReturn(Optional.empty());
        when(weatherReportRepository.save(any(WeatherReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WeatherReport result = weatherReportService.refreshWeatherReport(activity);

        assertEquals("Compiegne", result.getCity());
        assertEquals("France", result.getCountry());
        assertEquals(3, result.getWeatherCode());
        assertEquals(1.2, result.getPrecipitationSum());
        verify(weatherReportRepository).save(any(WeatherReport.class));
    }

    @Test
    void shouldFailWeatherRetrievalWhenLocationMissing_Test63() {
        Activity activity = new Activity();
        activity.setId(11);
        activity.setLocationCity("");
        activity.setDateA(LocalDate.now());

        MeteoDTO dto = new MeteoDTO();
        dto.setValid(false);
        dto.setMessage("City not found");

        doReturn(dto).when(weatherReportService).getWeatherForActivity("", activity.getDateA());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> weatherReportService.refreshWeatherReport(activity)
        );

        assertEquals("City not found", exception.getMessage());
    }

    @Test
    void shouldUpdateExistingWeatherReportWhenAlreadyPresent() {
        Activity activity = new Activity();
        activity.setId(15);
        activity.setLocationCity("Paris");
        activity.setDateA(LocalDate.now().minusDays(1));

        WeatherReport existing = new WeatherReport();
        existing.setId(99);
        existing.setCity("Old city");

        MeteoDTO dto = new MeteoDTO();
        dto.setValid(true);
        dto.setCity("Paris");
        dto.setCountry("France");
        dto.setWeatherCode(1);
        dto.setPrecipitationSum(0.0);

        doReturn(dto).when(weatherReportService).getWeatherForActivity("Paris", activity.getDateA());
        when(weatherReportRepository.findByActivity_Id(15)).thenReturn(Optional.of(existing));
        when(weatherReportRepository.save(any(WeatherReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WeatherReport saved = weatherReportService.refreshWeatherReport(activity);

        assertEquals(99, saved.getId());
        assertEquals("Paris", saved.getCity());
        assertEquals("France", saved.getCountry());
        assertEquals(1, saved.getWeatherCode());
        assertEquals(activity, saved.getActivity());
    }

    @Test
    void shouldUseZeroWhenPrecipitationIsNull() {
        Activity activity = new Activity();
        activity.setId(16);
        activity.setLocationCity("Lyon");
        activity.setDateA(LocalDate.now().minusDays(1));

        MeteoDTO dto = new MeteoDTO();
        dto.setValid(true);
        dto.setCity("Lyon");
        dto.setCountry("France");
        dto.setWeatherCode(45);
        dto.setPrecipitationSum(null);

        doReturn(dto).when(weatherReportService).getWeatherForActivity("Lyon", activity.getDateA());
        when(weatherReportRepository.findByActivity_Id(16)).thenReturn(Optional.empty());
        when(weatherReportRepository.save(any(WeatherReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WeatherReport saved = weatherReportService.refreshWeatherReport(activity);

        assertEquals(0.0, saved.getPrecipitationSum());
        assertEquals(45, saved.getWeatherCode());
    }

    @Test
    void shouldFindWeatherByActivityId() {
        WeatherReport report = new WeatherReport();
        report.setId(40);
        when(weatherReportRepository.findByActivity_Id(40)).thenReturn(Optional.of(report));

        Optional<WeatherReport> result = weatherReportService.findByActivityId(40);

        assertTrue(result.isPresent());
        assertEquals(40, result.get().getId());
    }

    @Test
    void shouldReturnInvalidDtoWhenGeocodingHasNoResults() {
        WeatherReportService realService = new WeatherReportService(weatherReportRepository);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(realService, "restTemplate", restTemplate);

        when(restTemplate.getForObject(contains("geocoding-api.open-meteo.com"), any(Class.class)))
                .thenReturn("{}");

        MeteoDTO result = realService.getWeatherForActivity("UnknownCity", LocalDate.now().minusDays(1));

        assertFalse(result.isValid());
        assertEquals("City not found", result.getMessage());
    }

    @Test
    void shouldReturnValidDtoWhenApiReturnsParsablePayload() {
        WeatherReportService realService = new WeatherReportService(weatherReportRepository);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(realService, "restTemplate", restTemplate);

        String geocodingPayload = "{\"results\":[{\"name\":\"Compiegne\",\"country\":\"France\",\"latitude\":49.4,\"longitude\":2.8}]}";
        String archivePayload = "{\"hourly\":{\"weather_code\":[3,3],\"precipitation\":[0.4,0.6]}}";

        when(restTemplate.getForObject(contains("geocoding-api.open-meteo.com"), any(Class.class)))
                .thenReturn(geocodingPayload);
        when(restTemplate.getForObject(contains("archive-api.open-meteo.com"), any(Class.class)))
                .thenReturn(archivePayload);

        MeteoDTO result = realService.getWeatherForActivity("Compiegne", LocalDate.now().minusDays(1));

        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals("Compiegne", result.getCity());
        assertEquals("France", result.getCountry());
        assertEquals(3, result.getWeatherCode());
        assertEquals(1.0, result.getPrecipitationSum());
    }
}
