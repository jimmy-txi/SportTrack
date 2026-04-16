package fr.utc.miage.sporttrack.service.event;

import fr.utc.miage.sporttrack.entity.event.Challenge;
import fr.utc.miage.sporttrack.repository.event.ChallengeRepository;
import fr.utc.miage.sporttrack.service.user.communication.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChallengeNotificationSchedulerTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ChallengeNotificationScheduler scheduler;

    @Test
    void notifyFinishedChallenges_shouldNotifyAndPersistTimestamp() {
        Challenge challenge = new Challenge();
        challenge.setName("Challenge test");

        when(challengeRepository.findByEndDateLessThanEqualAndEndedNotifiedAtIsNull(any(LocalDate.class)))
                .thenReturn(List.of(challenge));
        when(challengeRepository.save(any(Challenge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        scheduler.notifyFinishedChallenges();

        verify(notificationService).notifyChallengeEnded(challenge);
        verify(challengeRepository).save(challenge);
        assertNotNull(challenge.getEndedNotifiedAt());
    }
}
