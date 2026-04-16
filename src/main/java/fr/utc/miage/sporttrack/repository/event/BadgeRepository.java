package fr.utc.miage.sporttrack.repository.event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.utc.miage.sporttrack.entity.event.Badge;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Integer> {

    List<Badge> findBySportId(Integer sportId);

    List<Badge> findByEarnedBy_Id(Integer athleteId);

    List<Badge> findByIdNotIn(List<Integer> ids);
}