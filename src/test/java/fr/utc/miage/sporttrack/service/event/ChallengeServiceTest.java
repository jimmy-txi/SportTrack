package fr.utc.miage.sporttrack.service.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.entity.event.Challenge;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.event.ChallengeRepository;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {
    
    private static final String NAME = "Défi de course à pied";
    private static final String DESCRIPTION = "Un défi pour courir la plus grande distance cumulée en une semaine.";
    private static final LocalDate START_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate END_DATE = LocalDate.of(2024, 1, 7);
    private static final Metric TYPE = Metric.DURATION;
    private static final Athlete ATHLETE = new Athlete();
    private static final Sport SPORT = new Sport();
    
    @Mock
    private ChallengeRepository challengeRepository;

    @InjectMocks
    private ChallengeService challengeService;

    @Test
    void shouldSaveChallengeSuccessfully() {     
        Challenge challenge = new Challenge(NAME, DESCRIPTION, START_DATE, END_DATE, TYPE);

        challenge.setOrganizer(ATHLETE);
        challenge.setSport(SPORT);

        challengeService.saveChallenge(challenge, ATHLETE, SPORT);
        verify(challengeRepository).save(challenge);
    }

    @Test
    void shouldNotSaveChallengeWhenNull() {
        challengeService.saveChallenge(null, ATHLETE, SPORT);
        verify(challengeRepository, never()).save(any());
    }

    @Test
    void shouldNotSaveWhenAthleteIsNull() {
        Challenge challenge = new Challenge();
        challengeService.saveChallenge(challenge, null, SPORT);
        verify(challengeRepository, never()).save(any());
    }

    @Test
    void shouldNotSaveWhenSportIsNull() {
        Challenge challenge = new Challenge();
        challengeService.saveChallenge(challenge, ATHLETE, null);
        verify(challengeRepository, never()).save(any());
    }
}
