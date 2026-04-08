package fr.utc.miage.sporttrack.service.Activity;

import fr.utc.miage.sporttrack.entity.Activity.Activity;
import fr.utc.miage.sporttrack.repository.Activity.ActivityRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }
}
