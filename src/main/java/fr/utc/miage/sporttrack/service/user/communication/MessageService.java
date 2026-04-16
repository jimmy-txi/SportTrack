package fr.utc.miage.sporttrack.service.user.communication;

import fr.utc.miage.sporttrack.entity.enumeration.FriendshipStatus;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.user.communication.Friendship;
import fr.utc.miage.sporttrack.entity.user.communication.Message;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.repository.user.communication.FriendshipRepository;
import fr.utc.miage.sporttrack.repository.user.communication.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer component responsible for managing {@link Message} entities
 * within the SportTrack application.
 *
 * <p>Provides business logic for sending messages, retrieving conversations,
 * and tracking unread message counts between athlete friends.</p>
 *
 * @author SportTrack Team
 */
@Service
public class MessageService {

	/** The repository for message data access. */
	private final MessageRepository messageRepository;

	/** The repository for athlete data access. */
	private final AthleteRepository athleteRepository;

	/** The repository for friendship data access, used to verify friend status. */
	private final FriendshipRepository friendshipRepository;

	/** The notification service for message-received events (optional). */
	private final NotificationService notificationService;

	/**
	 * Constructs a {@code MessageService} without notification support.
	 *
	 * @param messageRepository      the message repository
	 * @param athleteRepository      the athlete repository
	 * @param friendshipRepository   the friendship repository
	 */
	public MessageService(MessageRepository messageRepository,
						  AthleteRepository athleteRepository,
						  FriendshipRepository friendshipRepository) {
		this(messageRepository, athleteRepository, friendshipRepository, null);
	}

	/**
	 * Constructs a {@code MessageService} with full notification support.
	 *
	 * @param messageRepository      the message repository
	 * @param athleteRepository      the athlete repository
	 * @param friendshipRepository   the friendship repository
	 * @param notificationService    the notification service for message events
	 */
	@Autowired
	public MessageService(MessageRepository messageRepository,
						  AthleteRepository athleteRepository,
						  FriendshipRepository friendshipRepository,
						  NotificationService notificationService) {
		this.messageRepository = messageRepository;
		this.athleteRepository = athleteRepository;
		this.friendshipRepository = friendshipRepository;
		this.notificationService = notificationService;
	}

	/**
	 * Sends a message from one athlete to another. Both athletes must be friends.
	 *
	 * @param senderId    the identifier of the sending athlete
	 * @param recipientId the identifier of the receiving athlete
	 * @param content     the textual content of the message
	 * @return the persisted {@link Message}
	 * @throws IllegalArgumentException if sender equals recipient, content is empty, or a user is not found
	 * @throws IllegalStateException    if the two athletes are not friends
	 */
	@Transactional
	public Message sendMessage(Integer senderId, Integer recipientId, String content) {
		if (senderId.equals(recipientId)) {
			throw new IllegalArgumentException("You cannot send a message to yourself");
		}

		if (content == null || content.trim().isEmpty()) {
			throw new IllegalArgumentException("Message content cannot be empty");
		}

		Athlete sender = findAthleteOrThrow(senderId, "Sender not found");
		Athlete recipient = findAthleteOrThrow(recipientId, "Recipient not found");
		ensureUsersAreFriends(sender, recipient);

		Message message = new Message();
		message.setInitiator(sender);
		message.setRecipient(recipient);
		message.setContent(content.trim());
		message.setSentAt(LocalDateTime.now());
		message.setSeen(false);

		Message savedMessage = messageRepository.save(message);
		if (notificationService != null) {
			notificationService.notifyMessageReceived(savedMessage);
		}
		return savedMessage;
	}

	/**
	 * Retrieves the full conversation between two friends and marks all messages as seen.
	 *
	 * @param currentUserId the identifier of the current user
	 * @param friendId      the identifier of the friend
	 * @return a list of messages in the conversation, ordered by send time ascending
	 * @throws IllegalStateException if the two athletes are not friends
	 */
	@Transactional
	public List<Message> getConversation(Integer currentUserId, Integer friendId) {
		Athlete currentUser = findAthleteOrThrow(currentUserId, "Current user not found");
		Athlete friend = findAthleteOrThrow(friendId, "Friend not found");
		ensureUsersAreFriends(currentUser, friend);

		messageRepository.markConversationAsSeen(currentUserId, friendId);
		return messageRepository.findConversation(currentUserId, friendId);
	}

	/**
	 * Returns a map of friend identifiers to their most recent message with the given athlete.
	 *
	 * @param athleteId the identifier of the athlete
	 * @return a map where each key is a friend's identifier and the value is the latest message
	 */
	public Map<Integer, Message> getLatestMessageByFriend(Integer athleteId) {
		List<Message> allMessages = messageRepository.findByInitiatorIdOrRecipientIdOrderBySentAtDesc(athleteId, athleteId);
		if (allMessages.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<Integer, Message> latestByFriend = new LinkedHashMap<>();
		for (Message message : allMessages) {
			Integer friendId = message.getInitiator().getId().equals(athleteId)
					? message.getRecipient().getId()
					: message.getInitiator().getId();

			latestByFriend.putIfAbsent(friendId, message);
		}
		return latestByFriend;
	}

	/**
	 * Returns a map of friend identifiers to the count of unread messages from that friend.
	 *
	 * @param athleteId the identifier of the athlete
	 * @return a map where each key is a friend's identifier and the value is the unread count
	 */
	public Map<Integer, Integer> getUnreadCountByFriend(Integer athleteId) {
		List<Message> allMessages = messageRepository.findByInitiatorIdOrRecipientIdOrderBySentAtDesc(athleteId, athleteId);
		Map<Integer, Integer> unreadByFriend = new HashMap<>();

		for (Message message : allMessages) {
			if (message.getRecipient().getId().equals(athleteId) && !message.isSeen()) {
				Integer friendId = message.getInitiator().getId();
				unreadByFriend.merge(friendId, 1, Integer::sum);
			}
		}

		return unreadByFriend;
	}

	/**
	 * Counts the total number of unread messages for the specified athlete.
	 *
	 * @param athleteId the identifier of the athlete
	 * @return the total count of unread messages
	 */
	public long countUnreadMessages(Integer athleteId) {
		return messageRepository.countByRecipientIdAndSeenFalse(athleteId);
	}

	/**
	 * Finds an athlete by identifier or throws an exception if not found.
	 *
	 * @param athleteId    the athlete identifier
	 * @param errorMessage the error message for the exception
	 * @return the resolved athlete
	 * @throws IllegalArgumentException if no athlete is found
	 */
	private Athlete findAthleteOrThrow(Integer athleteId, String errorMessage) {
		return athleteRepository.findById(athleteId)
				.orElseThrow(() -> new IllegalArgumentException(errorMessage));
	}

	/**
	 * Verifies that two athletes have an accepted friendship.
	 *
	 * @param first  the first athlete
	 * @param second the second athlete
	 * @throws IllegalStateException if no accepted friendship exists
	 */
	private void ensureUsersAreFriends(Athlete first, Athlete second) {
		Friendship friendship = friendshipRepository.findBetweenAthletes(first, second)
				.orElseThrow(() -> new IllegalStateException("You can only message your friends"));

		if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
			throw new IllegalStateException("You can only message your friends");
		}
	}
}
