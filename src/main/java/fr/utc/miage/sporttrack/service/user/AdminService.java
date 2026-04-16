package fr.utc.miage.sporttrack.service.user;

import fr.utc.miage.sporttrack.entity.user.Admin;
import fr.utc.miage.sporttrack.repository.user.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service layer component responsible for managing {@link Admin} entities
 * within the SportTrack application.
 *
 * <p>Provides business logic for looking up administrators and verifying
 * admin authentication status. A default admin account is automatically
 * seeded on first application startup if no admin exists in the database.</p>
 *
 * @author SportTrack Team
 */
@Service
public class AdminService {

    /** The default email address for the seeded admin account. */
    private static final String MAIL = "admin@mail.com";

    /** The default username for the seeded admin account. */
    private static final String USERNAME = "admin";

    /** The repository used for persisting and retrieving admin entities. */
    private final AdminRepository adminRepository;

    /**
     * Constructs a new {@code AdminService}, seeding a default admin account
     * if the database contains no administrators.
     *
     * @param adminRepository the repository for admin data access
     * @param passwordEncoder the encoder used to hash the default password
     * @param defaultPassword the plaintext default password injected from configuration
     */
    public AdminService(AdminRepository adminRepository,
                        PasswordEncoder passwordEncoder,
                        @Value("${admin.default.password}") String defaultPassword) {
        this.adminRepository = adminRepository;

        if (adminRepository.count() == 0) {
            Admin defaultAdmin = new Admin();
            defaultAdmin.setEmail(MAIL);
            defaultAdmin.setUsername(USERNAME);
            String encodedPassword = passwordEncoder.encode(defaultPassword);
            defaultAdmin.setPassword(encodedPassword);

            adminRepository.save(defaultAdmin);
        }
    }

    /**
     * Finds an administrator by their email address.
     *
     * @param email the email address to search for
     * @return the matching {@link Admin}
     * @throws IllegalArgumentException if no admin is found with the given email
     */
    public Admin findByEmail(String email) {
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
    }

    /**
     * Checks whether the given authentication object represents a logged-in admin.
     *
     * @param auth the Spring Security authentication object, may be {@code null}
     * @return {@code true} if the authentication corresponds to a valid admin, {@code false} otherwise
     */
    public boolean checkAdminLoggedIn(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return false;
        }
        Admin admin;
        try {
            admin = findByEmail(auth.getName());
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }
}