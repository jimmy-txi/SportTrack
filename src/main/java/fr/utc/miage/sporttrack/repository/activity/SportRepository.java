package fr.utc.miage.sporttrack.repository.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.utc.miage.sporttrack.entity.activity.Sport;

import java.util.List;

@Repository
public interface SportRepository extends JpaRepository<Sport, Integer> {
    List<Sport> findAllByActive(boolean active);
}
