package fr.utc.miage.sporttrack.repository.user.communication;

import fr.utc.miage.sporttrack.entity.user.communication.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("SELECT m FROM Message m WHERE " +
	    "(m.initiator.id = :firstId AND m.recipient.id = :secondId) OR " +
	    "(m.initiator.id = :secondId AND m.recipient.id = :firstId) " +
	    "ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("firstId") Integer firstId,
				   @Param("secondId") Integer secondId);

    List<Message> findByInitiatorIdOrRecipientIdOrderBySentAtDesc(Integer initiatorId, Integer recipientId);

    @Modifying
    @Query("UPDATE Message m SET m.seen = true WHERE m.recipient.id = :recipientId " +
	    "AND m.initiator.id = :friendId AND m.seen = false")
    int markConversationAsSeen(@Param("recipientId") Integer recipientId,
			       @Param("friendId") Integer friendId);

    long countByRecipientIdAndSeenFalse(Integer recipientId);
}
