package fr.utc.miage.sporttrack.service.user.communication;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.enumeration.NotificationType;
import fr.utc.miage.sporttrack.entity.event.Badge;
import fr.utc.miage.sporttrack.entity.event.Challenge;
import fr.utc.miage.sporttrack.entity.event.Objective;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.user.communication.Friendship;
import fr.utc.miage.sporttrack.entity.user.communication.Message;
import fr.utc.miage.sporttrack.entity.user.communication.Notification;
import fr.utc.miage.sporttrack.repository.user.communication.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service layer component responsible for managing {@link Notification} entities
 * within the SportTrack application.
 *
 * <p>Provides business logic for creating, querying, marking as read, and
 * dispatching typed notifications for various application events such as
 * messages, friend requests, badge awards, objectives, and challenge endings.</p>
 *
 * @author SportTrack Team
 */
@Service
public class NotificationService {

	/** The repository for notification data access. */
	private final NotificationRepository notificationRepository;

	/**
	 * Constructs a new {@code NotificationService} with the given repository.
	 *
	 * @param notificationRepository the notification repository
	 */
	public NotificationService(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	/**
	 * Creates and persists a new notification.
	 *
	 * @param recipient the athlete who will receive the notification
	 * @param actor     the athlete who triggered the notification (may be {@code null})
	 * @param type      the type of notification
	 * @param title     the notification title
	 * @param content   the notification body text
	 * @param targetUrl the URL to navigate to when the notification is clicked
	 * @return the persisted {@link Notification}, or {@code null} if the recipient is invalid
	 */
	public Notification createNotification(Athlete recipient,
										   Athlete actor,
										   NotificationType type,
										   String title,
										   String content,
										   String targetUrl) {
		if (recipient == null || recipient.getId() == null) {
			return null;
		}

		Notification notification = new Notification();
		notification.setRecipient(recipient);
		notification.setActor(actor);
		notification.setType(type);
		notification.setTitle(title);
		notification.setContent(content);
		notification.setTargetUrl(targetUrl);
		notification.setSeen(false);
		return notificationRepository.save(notification);
	}

	/**
	 * Returns all notifications for the specified athlete, ordered newest first.
	 *
	 * @param athleteId the identifier of the athlete
	 * @return a list of notifications, or an empty list if the identifier is null
	 */
	@Transactional(readOnly = true)
	public List<Notification> getNotificationsForAthlete(Integer athleteId) {
		if (athleteId == null) {
			return List.of();
		}
		return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(athleteId);
	}

	/**
	 * Returns a specific notification if it belongs to the specified recipient.
	 *
	 * @param notificationId the notification identifier
	 * @param recipientId    the recipient identifier
	 * @return the matching notification, or {@code null} if not found or parameters are null
	 */
	@Transactional(readOnly = true)
	public Notification getNotificationForRecipient(Integer notificationId, Integer recipientId) {
		if (notificationId == null || recipientId == null) {
			return null;
		}
		return notificationRepository.findByIdAndRecipientId(notificationId, recipientId).orElse(null);
	}

	/**
	 * Returns the ten most recent notifications for the specified athlete.
	 *
	 * @param athleteId the identifier of the athlete
	 * @return a list of up to ten notifications, newest first
	 */
	@Transactional(readOnly = true)
	public List<Notification> getRecentNotifications(Integer athleteId) {
		if (athleteId == null) {
			return List.of();
		}
		return notificationRepository.findTop10ByRecipientIdOrderByCreatedAtDesc(athleteId);
	}

	/**
	 * Counts the number of unread notifications for the specified athlete.
	 *
	 * @param athleteId the identifier of the athlete
	 * @return the count of unread notifications, or {@code 0} if the identifier is null
	 */
	@Transactional(readOnly = true)
	public long countUnreadNotifications(Integer athleteId) {
		if (athleteId == null) {
			return 0L;
		}
		return notificationRepository.countByRecipientIdAndSeenFalse(athleteId);
	}

	/**
	 * Marks a single notification as read for the specified recipient.
	 *
	 * @param notificationId the notification identifier
	 * @param recipientId    the recipient identifier
	 * @throws IllegalArgumentException if the notification is not found
	 */
	@Transactional
	public void markAsRead(Integer notificationId, Integer recipientId) {
		if (notificationId == null || recipientId == null) {
			return;
		}

		Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, recipientId)
				.orElseThrow(() -> new IllegalArgumentException("Notification not found"));
		notification.setSeen(true);
		notificationRepository.save(notification);
	}

	/**
	 * Marks all notifications as read for the specified recipient.
	 *
	 * @param recipientId the recipient identifier
	 */
	@Transactional
	public void markAllAsRead(Integer recipientId) {
		if (recipientId == null) {
			return;
		}
		notificationRepository.markAllAsSeen(recipientId);
	}

	/**
	 * Sends a notification to the message recipient that a new message has been received.
	 *
	 * @param message the message that was received
	 */
	@Transactional
	public void notifyMessageReceived(Message message) {
		if (message == null || message.getRecipient() == null) {
			return;
		}

		createNotification(
				message.getRecipient(),
				message.getInitiator(),
				NotificationType.MESSAGE_RECEIVED,
				"Nouveau message",
				buildActorName(message.getInitiator()) + " vous a envoyé un message.",
				"/messages"
		);
	}

	/**
	 * Sends a notification to the friendship recipient that a friend request has been received.
	 *
	 * @param friendship the friendship representing the request
	 */
	@Transactional
	public void notifyFriendRequest(Friendship friendship) {
		if (friendship == null || friendship.getRecipient() == null) {
			return;
		}

		createNotification(
				friendship.getRecipient(),
				friendship.getInitiator(),
				NotificationType.FRIEND_REQUEST,
				"Nouvelle demande d'ami",
				buildActorName(friendship.getInitiator()) + " vous a envoyé une demande d'ami.",
				"/friends?tab=requests"
		);
	}

	/**
	 * Sends a notification to the friendship initiator that their request has been accepted.
	 *
	 * @param friendship the accepted friendship
	 */
	@Transactional
	public void notifyFriendRequestAccepted(Friendship friendship) {
		if (friendship == null || friendship.getInitiator() == null) {
			return;
		}

		createNotification(
				friendship.getInitiator(),
				friendship.getRecipient(),
				NotificationType.FRIEND_REQUEST_ACCEPTED,
				"Demande d'ami acceptée",
				buildActorName(friendship.getRecipient()) + " a accepté votre demande d'ami.",
				"/friends?tab=friends"
		);
	}

	/**
	 * Sends a notification to multiple recipients that a friend has published a new activity.
	 *
	 * @param actor      the athlete who published the activity
	 * @param activity   the newly published activity
	 * @param recipients the list of friends to notify
	 */
	@Transactional
	public void notifyActivityPublished(Athlete actor, Activity activity, List<Athlete> recipients) {
		if (actor == null || activity == null || recipients == null || recipients.isEmpty()) {
			return;
		}

		String message = buildActorName(actor) + " a enregistré une nouvelle activité : " + activity.getTitle() + ".";
		for (Athlete recipient : recipients) {
			if (recipient != null && recipient.getId() != null && !recipient.getId().equals(actor.getId())) {
				createNotification(
						recipient,
						actor,
						NotificationType.FRIEND_ACTIVITY,
						"Nouvelle activité d'un ami",
						message,
						"/friends/activities"
				);
			}
		}
	}

	/**
	 * Sends a notification to the athlete that they have earned a badge.
	 *
	 * @param athlete the athlete who earned the badge
	 * @param badge   the badge that was earned
	 */
	@Transactional
	public void notifyBadgeEarned(Athlete athlete, Badge badge) {
		if (athlete == null || badge == null) {
			return;
		}

		createNotification(
				athlete,
				null,
				NotificationType.BADGE_EARNED,
				"Badge obtenu",
				"Vous avez gagné le badge " + badge.getLabel() + ".",
				"/badges"
		);
	}

	/**
	 * Sends a notification to the athlete that an objective has been completed.
	 *
	 * @param athlete   the athlete who completed the objective
	 * @param objective the completed objective
	 */
	@Transactional
	public void notifyObjectiveCompleted(Athlete athlete, Objective objective) {
		if (athlete == null || objective == null) {
			return;
		}

		createNotification(
				athlete,
				null,
				NotificationType.OBJECTIVE_COMPLETED,
				"Objectif atteint",
				"Vous avez validé l'objectif " + objective.getName() + ".",
				"/objectives"
		);
	}

	/**
	 * Sends notifications to all participants and the organiser that a challenge has ended.
	 *
	 * @param challenge the challenge that has ended
	 */
	@Transactional
	public void notifyChallengeEnded(Challenge challenge) {
		if (challenge == null) {
			return;
		}

		List<Athlete> recipients = new ArrayList<>();
		if (challenge.getOrganizer() != null) {
			recipients.add(challenge.getOrganizer());
		}
		if (challenge.getParticipants() != null) {
			recipients.addAll(challenge.getParticipants());
		}

		String message = "Le challenge " + challenge.getName() + " est terminé.";
		for (Athlete recipient : recipients) {
			createNotification(
					recipient,
					null,
					NotificationType.CHALLENGE_ENDED,
					"Challenge terminé",
					message,
					"/challenges"
			);
		}
	}

	/**
	 * Builds a display name for the given athlete, preferring first and last name,
	 * falling back to username, then email.
	 *
	 * @param athlete the athlete whose name should be resolved
	 * @return the best available display name
	 */
	private String buildActorName(Athlete athlete) {
		if (athlete == null) {
			return "Un utilisateur";
		}

		String firstName = athlete.getFirstName() != null ? athlete.getFirstName().trim() : "";
		String lastName = athlete.getLastName() != null ? athlete.getLastName().trim() : "";
		String fullName = (firstName + " " + lastName).trim();
		if (!fullName.isEmpty()) {
			return fullName;
		}
		if (athlete.getUsername() != null && !athlete.getUsername().isBlank()) {
			return athlete.getUsername();
		}
		return athlete.getEmail() != null ? athlete.getEmail() : "Un utilisateur";
	}
}
