package fr.utc.miage.sporttrack.repository.User;

import fr.utc.miage.sporttrack.entity.User.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
}
