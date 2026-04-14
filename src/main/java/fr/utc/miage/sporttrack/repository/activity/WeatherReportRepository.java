package fr.utc.miage.sporttrack.repository.activity;

import fr.utc.miage.sporttrack.entity.activity.WeatherReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeatherReportRepository extends JpaRepository<WeatherReport, Integer> {

    Optional<WeatherReport> findByActivity_Id(int activityId);

    void deleteByActivity_Id(int activityId);
}