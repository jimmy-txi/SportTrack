package fr.utc.miage.sporttrack.repository.user;

import fr.utc.miage.sporttrack.entity.user.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Admin} entities.
 *
 * <p>Provides standard CRUD operations as well as custom query methods
 * for retrieving administrators by their email address.</p>
 *
 * @author SportTrack Team
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

    /**
     * Finds an administrator by their email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the administrator if found, empty otherwise
     */
    Optional<Admin> findByEmail(String email);
}