package fr.utc.miage.sporttrack.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

import fr.utc.miage.sporttrack.entity.event.Challenge;
import fr.utc.miage.sporttrack.entity.event.ChallengeRanking;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.activity.SportRepository;
import fr.utc.miage.sporttrack.repository.event.ChallengeRepository;
import fr.utc.miage.sporttrack.repository.user.AthleteRepository;
import fr.utc.miage.sporttrack.service.activity.SportService;
import fr.utc.miage.sporttrack.service.event.ChallengeRankingService;

@ExtendWith(MockitoExtension.class)
class ChallengeControllerTest {

    @Mock
    private SportRepository sportRepository;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private SportService sportService;

    @Mock
    private ChallengeRankingService challengeRankingService;

    @Mock
    private Model model;

    @InjectMocks
    private ChallengeController controller;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldListChallengesWithoutTransientRankingMap() throws Exception {
        Athlete athlete = buildAthlete(1, "alice", "alice@mail.com");
        Challenge challenge = new Challenge();
        setPrivateField(Challenge.class, challenge, "id", 12);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("athlete", athlete);

        when(challengeRepository.findDistinctByOrganizer_IdOrParticipants_Id(1, 1)).thenReturn(List.of(challenge));

        String view = controller.listChallenges(session, model);

        assertEquals("challenge/challenge_list", view);
        verify(model).addAttribute("athlete", athlete);
        verify(model).addAttribute("challenges", List.of(challenge));
    }

    @Test
    void shouldRecomputeRankingWhenParticipatingChallenge() throws Exception {
        Athlete athlete = buildAthlete(2, "bob", "bob@mail.com");
        Challenge challenge = new Challenge();
        setPrivateField(Challenge.class, challenge, "id", 42);
        challenge.setParticipants(new ArrayList<>());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("athlete", athlete);

        when(challengeRepository.findById(42)).thenReturn(Optional.of(challenge));
        when(challengeRepository.save(any(Challenge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String view = controller.participateInChallenge(42, session);

        assertEquals("redirect:/challenges", view);
        verify(challengeRepository).save(challenge);
        verify(challengeRankingService).recomputeRanking(challenge);
    }

    @Test
    void shouldRedirectToLoginWhenNoAuthenticatedAthlete() {
        MockHttpSession session = new MockHttpSession();

        String view = controller.listAllChallenges(session, model);

        assertEquals("redirect:/login", view);
        verifyNoInteractions(challengeRepository);
    }

    @Test
    void shouldDisplayChallengeRanking_Test58() throws Exception {
        Athlete athlete = buildAthlete(10, "viewer", "viewer@mail.com");
        Athlete participant = buildAthlete(11, "runner", "runner@mail.com");

        ChallengeRanking ranking = new ChallengeRanking();
        ranking.setAthlete(participant);
        ranking.setRankPosition(1);
        ranking.setScore(42d);

        Challenge challenge = new Challenge();
        setPrivateField(Challenge.class, challenge, "id", 99);
        challenge.setParticipants(List.of(participant));
        challenge.setRankings(List.of(ranking));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("athlete", athlete);

        when(challengeRepository.findAll()).thenReturn(List.of(challenge));

        String view = controller.listAllChallenges(session, model);

        assertEquals("challenge/challenges", view);
        assertEquals(1, challenge.getRankings().size());
        assertEquals("runner", challenge.getRankings().get(0).getDisplayName());
        assertEquals(42d, challenge.getRankings().get(0).getScore());
        verify(model).addAttribute("challenges", List.of(challenge));
    }

    private Athlete buildAthlete(int id, String username, String email) throws Exception {
        Athlete athlete = new Athlete();
        athlete.setUsername(username);
        athlete.setPassword("pwd");
        athlete.setEmail(email);
        setPrivateField(athlete.getClass().getSuperclass(), athlete, "id", id);
        return athlete;
    }

    private void setPrivateField(Class<?> ownerClass, Object target, String fieldName, Object value) throws Exception {
        Field field = ownerClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
