package fr.utc.miage.sporttrack.repository.Activity;

import fr.utc.miage.sporttrack.entity.Activity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Integer> {
}
