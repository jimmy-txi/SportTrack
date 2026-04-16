package fr.utc.miage.sporttrack.service.user;

import fr.utc.miage.sporttrack.entity.user.Admin;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.user.AdminRepository;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service layer component implementing Spring Security's {@link UserDetailsService}
 * for the SportTrack application.
 *
 * <p>This service resolves login credentials by looking up athletes first, then
 * administrators, using the email address as the principal username. It constructs
 * a {@link UserDetails} object with the appropriate role ("USER" or "ADMIN").</p>
 *
 * @author SportTrack Team
 */
@Service
public class UserService implements UserDetailsService {

    /** The repository used for looking up athlete accounts. */
    private final AthleteRepository athleteRepository;

    /** The repository used for looking up admin accounts. */
    private final AdminRepository adminRepository;

    /**
     * Constructs a new {@code UserService} with the required repositories.
     *
     * @param athleteRepository the repository for athlete data access
     * @param adminRepository   the repository for admin data access
     */
    public UserService(AthleteRepository athleteRepository, AdminRepository adminRepository) {
        this.athleteRepository = athleteRepository;
        this.adminRepository = adminRepository;
    }

    /**
     * Loads a user by their email address, searching athletes first then admins.
     *
     * @param email the email address used as the login identifier
     * @return a {@link UserDetails} instance with the appropriate role and credentials
     * @throws UsernameNotFoundException if no user is found with the given email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Try athlete first
        Optional<Athlete> athleteOpt = athleteRepository.findByEmail(email);
        if (athleteOpt.isPresent()) {
            Athlete athlete = athleteOpt.get();
            return User.builder()
                    .username(athlete.getEmail())
                    .password(athlete.getPassword())
                    .roles("USER")
                    .build();
        }

        // Try admin next
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            return User.builder()
                    .username(admin.getEmail())
                    .password(admin.getPassword())
                    .roles("ADMIN")
                    .build();
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}