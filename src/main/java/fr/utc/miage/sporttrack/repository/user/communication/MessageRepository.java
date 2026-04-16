package fr.utc.miage.sporttrack.repository.user.communication;

import fr.utc.miage.sporttrack.entity.user.communication.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Message} entities.
 *
 * <p>Provides standard CRUD operations as well as custom query methods
 * for retrieving conversations, counting unread messages, and marking
 * messages as seen.</p>
 *
 * @author SportTrack Team
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    /**
     * Retrieves the full conversation between two athletes, ordered by send time ascending.
     *
     * @param firstId  the identifier of the first athlete
     * @param secondId the identifier of the second athlete
     * @return a list of messages exchanged between the two athletes, oldest first
     */
    @Query("SELECT m FROM Message m WHERE " +
	    "(m.initiator.id = :firstId AND m.recipient.id = :secondId) OR " +
	    "(m.initiator.id = :secondId AND m.recipient.id = :firstId) " +
	    "ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("firstId") Integer firstId,
				   @Param("secondId") Integer secondId);

    /**
     * Finds all messages in which the specified athlete is either the sender or the recipient,
     * ordered by send time descending.
     *
     * @param initiatorId the identifier to match against the sender field
     * @param recipientId the identifier to match against the recipient field
     * @return a list of messages involving the athlete, newest first
     */
    List<Message> findByInitiatorIdOrRecipientIdOrderBySentAtDesc(Integer initiatorId, Integer recipientId);

    /**
     * Marks all unseen messages sent by the specified friend to the specified recipient as seen.
     *
     * @param recipientId the identifier of the message recipient (the current user)
     * @param friendId    the identifier of the message sender (the friend)
     * @return the number of messages that were updated
     */
    @Modifying
    @Query("UPDATE Message m SET m.seen = true WHERE m.recipient.id = :recipientId " +
	    "AND m.initiator.id = :friendId AND m.seen = false")
    int markConversationAsSeen(@Param("recipientId") Integer recipientId,
			       @Param("friendId") Integer friendId);

    /**
     * Counts the number of unseen (unread) messages for the specified recipient.
     *
     * @param recipientId the identifier of the message recipient
     * @return the count of unseen messages
     */
    long countByRecipientIdAndSeenFalse(Integer recipientId);
}