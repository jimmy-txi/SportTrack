package fr.utc.miage.sporttrack.service.User;

import fr.utc.miage.sporttrack.entity.User.Admin;
import fr.utc.miage.sporttrack.repository.User.AdminRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
}
