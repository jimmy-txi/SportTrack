package fr.utc.miage.sporttrack.service.Activity;

import fr.utc.miage.sporttrack.entity.Activity.WeatherReport;
import fr.utc.miage.sporttrack.repository.Activity.WeatherReportRepository;
import org.springframework.stereotype.Service;

@Service
public class WeatherReportService {

    private final WeatherReportRepository weatherReportRepository;

    public WeatherReportService(WeatherReportRepository weatherReportRepository) {
        this.weatherReportRepository = weatherReportRepository;
    }
}
