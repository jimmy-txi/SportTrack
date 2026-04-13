package fr.utc.miage.sporttrack.repository.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.utc.miage.sporttrack.entity.activity.Sport;

@Repository
public interface SportRepository extends JpaRepository<Sport, Integer> {
}
