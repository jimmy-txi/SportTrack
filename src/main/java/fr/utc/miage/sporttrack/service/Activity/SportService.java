package fr.utc.miage.sporttrack.service.Activity;

import fr.utc.miage.sporttrack.entity.Activity.Sport;
import fr.utc.miage.sporttrack.repository.Activity.SportRepository;
import org.springframework.stereotype.Service;

@Service
public class SportService {

    private final SportRepository sportRepository;

    public SportService(SportRepository sportRepository) {
        this.sportRepository = sportRepository;
    }
}
