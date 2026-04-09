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

@Service
public class UserService implements UserDetailsService {

    private final AthleteRepository athleteRepository;
    private final AdminRepository adminRepository;

    public UserService(AthleteRepository athleteRepository, AdminRepository adminRepository) {
        this.athleteRepository = athleteRepository;
        this.adminRepository = adminRepository;
    }

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
