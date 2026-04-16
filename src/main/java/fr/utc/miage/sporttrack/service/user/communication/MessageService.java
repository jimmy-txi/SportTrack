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

@Service
public class MessageService {

	private final MessageRepository messageRepository;
	private final AthleteRepository athleteRepository;
	private final FriendshipRepository friendshipRepository;
	private final NotificationService notificationService;

	public MessageService(MessageRepository messageRepository,
						  AthleteRepository athleteRepository,
						  FriendshipRepository friendshipRepository) {
		this(messageRepository, athleteRepository, friendshipRepository, null);
	}

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

	@Transactional
	public List<Message> getConversation(Integer currentUserId, Integer friendId) {
		Athlete currentUser = findAthleteOrThrow(currentUserId, "Current user not found");
		Athlete friend = findAthleteOrThrow(friendId, "Friend not found");
		ensureUsersAreFriends(currentUser, friend);

		messageRepository.markConversationAsSeen(currentUserId, friendId);
		return messageRepository.findConversation(currentUserId, friendId);
	}

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

	public long countUnreadMessages(Integer athleteId) {
		return messageRepository.countByRecipientIdAndSeenFalse(athleteId);
	}

	private Athlete findAthleteOrThrow(Integer athleteId, String errorMessage) {
		return athleteRepository.findById(athleteId)
				.orElseThrow(() -> new IllegalArgumentException(errorMessage));
	}

	private void ensureUsersAreFriends(Athlete first, Athlete second) {
		Friendship friendship = friendshipRepository.findBetweenAthletes(first, second)
				.orElseThrow(() -> new IllegalStateException("You can only message your friends"));

		if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
			throw new IllegalStateException("You can only message your friends");
		}
	}
}
