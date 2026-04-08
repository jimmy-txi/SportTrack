package fr.utc.miage.sporttrack.repository.User;

import fr.utc.miage.sporttrack.entity.User.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AthleteRepository extends JpaRepository<Athlete, Integer> {
}
