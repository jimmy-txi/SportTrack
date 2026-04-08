package fr.utc.miage.sporttrack.repository.Event;

import fr.utc.miage.sporttrack.entity.Event.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Integer> {
}
