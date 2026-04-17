package fr.utc.miage.sporttrack.repository.user.communication;

import fr.utc.miage.sporttrack.entity.user.communication.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Notification} entities.
 *
 * <p>Provides standard CRUD operations as well as custom query methods
 * for retrieving, counting, and marking notifications for a specific recipient.</p>
 *
 * @author SportTrack Team
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    /**
     * Finds all notifications for the specified recipient, ordered by creation time descending.
     *
     * @param recipientId the unique identifier of the recipient
     * @return a list of notifications, newest first
     */
	List<Notification> findByRecipientIdOrderByCreatedAtDesc(Integer recipientId);

    /**
     * Finds the ten most recent notifications for the specified recipient.
     *
     * @param recipientId the unique identifier of the recipient
     * @return a list of up to ten notifications, newest first
     */
	List<Notification> findTop10ByRecipientIdOrderByCreatedAtDesc(Integer recipientId);

    /**
     * Finds all unseen notifications for the specified recipient, ordered by creation time descending.
     *
     * @param recipientId the unique identifier of the recipient
     * @return a list of unseen notifications, newest first
     */
	List<Notification> findByRecipientIdAndSeenFalseOrderByCreatedAtDesc(Integer recipientId);

    /**
     * Counts the number of unseen notifications for the specified recipient.
     *
     * @param recipientId the unique identifier of the recipient
     * @return the count of unseen notifications
     */
	long countByRecipientIdAndSeenFalse(Integer recipientId);

    /**
     * Finds a notification by its identifier and the identifier of its recipient.
     *
     * @param id          the unique identifier of the notification
     * @param recipientId the unique identifier of the recipient
     * @return an {@link Optional} containing the notification if found, empty otherwise
     */
	Optional<Notification> findByIdAndRecipientId(Integer id, Integer recipientId);

    /**
     * Marks all notifications for the specified recipient as seen.
     *
     * @param recipientId the unique identifier of the recipient whose notifications should be marked
     * @return the number of notifications that were updated
     */
	@Modifying
	@Query("UPDATE Notification n SET n.seen = true WHERE n.recipient.id = :recipientId")
	int markAllAsSeen(@Param("recipientId") Integer recipientId);
}