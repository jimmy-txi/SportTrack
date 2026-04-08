package fr.utc.miage.sporttrack.service.User.Communication;

import fr.utc.miage.sporttrack.entity.User.Communication.Notification;
import fr.utc.miage.sporttrack.repository.User.Communication.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
}
