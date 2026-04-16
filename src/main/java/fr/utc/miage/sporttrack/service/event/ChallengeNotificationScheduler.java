package fr.utc.miage.sporttrack.service.event;

import fr.utc.miage.sporttrack.entity.event.Challenge;
import fr.utc.miage.sporttrack.repository.event.ChallengeRepository;
import fr.utc.miage.sporttrack.service.user.communication.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChallengeNotificationScheduler {

    private final ChallengeRepository challengeRepository;
    private final NotificationService notificationService;

    public ChallengeNotificationScheduler(ChallengeRepository challengeRepository,
                                          NotificationService notificationService) {
        this.challengeRepository = challengeRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelayString = "${sporttrack.notifications.challenge-check-ms:3600000}")
    @Transactional
    public void notifyFinishedChallenges() {
        List<Challenge> finishedChallenges = challengeRepository.findByEndDateLessThanEqualAndEndedNotifiedAtIsNull(LocalDate.now());
        for (Challenge challenge : finishedChallenges) {
            notificationService.notifyChallengeEnded(challenge);
            challenge.setEndedNotifiedAt(LocalDateTime.now());
            challengeRepository.save(challenge);
        }
    }
}
