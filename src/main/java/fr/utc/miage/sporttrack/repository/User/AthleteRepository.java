package fr.utc.miage.sporttrack.repository.User;

import fr.utc.miage.sporttrack.entity.User.Athlete;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AthleteRepository extends JpaRepository<Athlete, Integer> {

    boolean existsByEmail(String email);
    List<Athlete> findByUsernameContainingIgnoreCase(String q);

    Optional<Athlete> findByEmail(String email);

    Optional<Athlete> findByIdU(Integer id);

}
