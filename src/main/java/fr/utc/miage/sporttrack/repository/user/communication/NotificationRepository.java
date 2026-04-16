package fr.utc.miage.sporttrack.repository.user.communication;

import fr.utc.miage.sporttrack.entity.user.communication.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

	List<Notification> findByRecipientIdOrderByCreatedAtDesc(Integer recipientId);

	List<Notification> findTop10ByRecipientIdOrderByCreatedAtDesc(Integer recipientId);

	List<Notification> findByRecipientIdAndSeenFalseOrderByCreatedAtDesc(Integer recipientId);

	long countByRecipientIdAndSeenFalse(Integer recipientId);

	Optional<Notification> findByIdAndRecipientId(Integer id, Integer recipientId);

	@Modifying
	@Query("UPDATE Notification n SET n.seen = true WHERE n.recipient.id = :recipientId")
	int markAllAsSeen(@Param("recipientId") Integer recipientId);
}
