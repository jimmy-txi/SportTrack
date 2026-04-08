package fr.utc.miage.sporttrack.repository.User.Communication;

import fr.utc.miage.sporttrack.entity.User.Communication.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
}
