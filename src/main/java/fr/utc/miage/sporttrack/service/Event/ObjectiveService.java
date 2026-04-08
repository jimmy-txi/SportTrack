package fr.utc.miage.sporttrack.service.Event;

import fr.utc.miage.sporttrack.entity.Event.Objective;
import fr.utc.miage.sporttrack.repository.Event.ObjectiveRepository;
import org.springframework.stereotype.Service;

@Service
public class ObjectiveService {

    private final ObjectiveRepository objectiveRepository;

    public ObjectiveService(ObjectiveRepository objectiveRepository) {
        this.objectiveRepository = objectiveRepository;
    }
}
