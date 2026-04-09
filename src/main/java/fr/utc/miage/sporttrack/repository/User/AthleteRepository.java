package fr.utc.miage.sporttrack.repository.User;


import org.springframework.data.jpa.repository.JpaRepository;

import fr.utc.miage.sporttrack.entity.User.Athlete;


public interface AthleteRepository extends JpaRepository<Athlete, Long> {
    
}
