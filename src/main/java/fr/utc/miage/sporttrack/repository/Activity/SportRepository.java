package fr.utc.miage.sporttrack.repository.Activity;

import fr.utc.miage.sporttrack.entity.Activity.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SportRepository extends JpaRepository<Sport, Integer> {
}
