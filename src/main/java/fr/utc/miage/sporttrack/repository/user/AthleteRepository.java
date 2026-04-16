package fr.utc.miage.sporttrack.repository.user;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Athlete} entities.
 *
 * <p>Provides standard CRUD operations as well as custom query methods
 * for retrieving athletes by email, username, and identifier.</p>
 *
 * @author SportTrack Team
 */
@Repository
public interface AthleteRepository extends JpaRepository<Athlete, Integer> {

    /**
     * Checks whether an athlete with the specified email address already exists.
     *
     * @param email the email address to check
     * @return {@code true} if an athlete with the email exists, {@code false} otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds all athletes whose username contains the given search string, ignoring case.
     *
     * @param q the search query to match against usernames
     * @return a list of athletes whose usernames contain the query string
     */
    List<Athlete> findByUsernameContainingIgnoreCase(String q);

    /**
     * Finds an athlete by their email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the athlete if found, empty otherwise
     */
    Optional<Athlete> findByEmail(String email);

    /**
     * Finds an athlete by their unique identifier.
     *
     * @param id the unique identifier of the athlete
     * @return an {@link Optional} containing the athlete if found, empty otherwise
     */
    Optional<Athlete> findById(Integer id);

}