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

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;

	public NotificationService(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

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

	@Transactional(readOnly = true)
	public List<Notification> getNotificationsForAthlete(Integer athleteId) {
		if (athleteId == null) {
			return List.of();
		}
		return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(athleteId);
	}

	@Transactional(readOnly = true)
	public Notification getNotificationForRecipient(Integer notificationId, Integer recipientId) {
		if (notificationId == null || recipientId == null) {
			return null;
		}
		return notificationRepository.findByIdAndRecipientId(notificationId, recipientId).orElse(null);
	}

	@Transactional(readOnly = true)
	public List<Notification> getRecentNotifications(Integer athleteId) {
		if (athleteId == null) {
			return List.of();
		}
		return notificationRepository.findTop10ByRecipientIdOrderByCreatedAtDesc(athleteId);
	}

	@Transactional(readOnly = true)
	public long countUnreadNotifications(Integer athleteId) {
		if (athleteId == null) {
			return 0L;
		}
		return notificationRepository.countByRecipientIdAndSeenFalse(athleteId);
	}

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

	@Transactional
	public void markAllAsRead(Integer recipientId) {
		if (recipientId == null) {
			return;
		}
		notificationRepository.markAllAsSeen(recipientId);
	}

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
