package fr.utc.miage.sporttrack.service.Event;

import fr.utc.miage.sporttrack.entity.Event.Badge;
import fr.utc.miage.sporttrack.repository.Event.BadgeRepository;
import org.springframework.stereotype.Service;

@Service
public class BadgeService {

    private final BadgeRepository badgeRepository;

    public BadgeService(BadgeRepository badgeRepository) {
        this.badgeRepository = badgeRepository;
    }
}
