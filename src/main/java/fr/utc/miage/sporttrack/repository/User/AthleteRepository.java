package fr.utc.miage.sporttrack.repository.user;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AthleteRepository extends JpaRepository<Athlete, Integer> {

    boolean existsByEmail(String email);

    Optional<Athlete> findByEmail(String email);

    Optional<Athlete> findByIdU(Integer id);

}
