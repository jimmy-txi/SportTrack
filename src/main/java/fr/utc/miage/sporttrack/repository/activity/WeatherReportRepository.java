package fr.utc.miage.sporttrack.repository.activity;

import fr.utc.miage.sporttrack.entity.activity.WeatherReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link WeatherReport} entities.
 *
 * <p>Provides standard CRUD operations as well as custom query methods
 * for retrieving and deleting weather reports associated with a specific activity.</p>
 *
 * @author SportTrack Team
 */
@Repository
public interface WeatherReportRepository extends JpaRepository<WeatherReport, Integer> {

    /**
     * Finds the weather report associated with the specified activity.
     *
     * @param activityId the unique identifier of the activity
     * @return an {@link Optional} containing the weather report if found, empty otherwise
     */
    Optional<WeatherReport> findByActivity_Id(int activityId);

    /**
     * Deletes the weather report associated with the specified activity.
     *
     * @param activityId the unique identifier of the activity whose weather report should be deleted
     */
    void deleteByActivity_Id(int activityId);
}