package fr.utc.miage.sporttrack.service.user;

import fr.utc.miage.sporttrack.entity.user.Admin;
import fr.utc.miage.sporttrack.repository.user.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private static final String MAIL = "admin@mail.com";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";



    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;

        if (adminRepository.count() == 0) {
            Admin defaultAdmin = new Admin();
            defaultAdmin.setEmail(MAIL);
            defaultAdmin.setUsername(USERNAME);
            String encodedPassword = passwordEncoder.encode(PASSWORD);
            defaultAdmin.setPassword(encodedPassword);

            adminRepository.save(defaultAdmin);
        }
    }

    public Admin findByEmail(String email) {
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
    }
}
