package fr.utc.miage.sporttrack.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDate;
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

import fr.utc.miage.sporttrack.dto.ChallengeFormDTO;
import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
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

    // =====================================================================
    // showCreateChallengeForm
    // =====================================================================

    @Test
    void shouldRedirectToLoginOnShowFormWhenNotAuthenticated() {
        MockHttpSession session = new MockHttpSession();
        String view = controller.showCreateChallengeForm(session, model);
        assertEquals("redirect:/login", view);
    }

    @Test
    void shouldShowCreateChallengeFormWhenAuthenticated() throws Exception {
        Athlete athlete = buildAthlete(1, "alice", "alice@mail.com");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("athlete", athlete);

        when(sportService.findAllActive()).thenReturn(List.of());

        String view = controller.showCreateChallengeForm(session, model);

        assertEquals("challenge/challenge_form", view);
        verify(model).addAttribute("athlete", athlete);
        verify(model).addAttribute(any(String.class), any(ChallengeFormDTO.class));
    }

    // =====================================================================
    // createChallenge
    // =====================================================================

    @Test
    void shouldRedirectToLoginOnCreateWhenNotAuthenticated() {
        MockHttpSession session = new MockHttpSession();
        ChallengeFormDTO dto = buildValidDto();

        String view = controller.createChallenge(dto, 1, session, model);
        assertEquals("redirect:/login", view);
    }

    @Test
    void shouldReturnFormWhenSportIdIsNull() throws Exception {
        Athlete athlete = buildAthlete(1, "alice", "alice@mail.com");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("athlete", athlete);

        when(sportService.findAllActive()).thenReturn(List.of());

        ChallengeFormDTO dto = buildValidDto();
        String view = controller.createChallenge(dto, null, session, model);

        assertEquals("challenge/challenge_form", view);
        verify(model).addAttribute("error", "Veuillez sélectionner une discipline sportive valide.");
    }

    @Test
    void shouldReturnFormWhenSportIdIsZeroOrNegative() throws Exception {
        Athlete athlete = buildAthlete(1, "alice", "alice@mail.com");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("athlete", athlete);

        when(sportService.findAllActive()).thenReturn(List.of());

        ChallengeFormDTO dto = buildValidDto();
        String view = controller.createChallenge(dto, 0, session, model);

        assertEquals("challenge/challenge_form", view);
        verify(model).addAttribute("error", "Veuillez sélectionner une discipline sportive valide.");
    }

    @Test
    void shouldReturnFormWhenSportNotFound() throws Exception {
        Athlete athlete = buildAthlete(1, "alice", "alice@mail.com");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("athlete", athlete);

        when(sportRepository.findById(99)).thenReturn(Optional.empty());
        when(sportService.findAllActive()).thenReturn(List.of());

        ChallengeFormDTO dto = buildValidDto();
        String view = controller.createChallenge(dto, 99, session, model);

        assertEquals("challenge/challenge_form", view);
        verify(model).addAttribute("error", "La discipline sportive sélectionnée est introuvable.");
    }

    @Test
    void shouldReturnFormWhenDatesAreInvalid() throws Exception {
        Athlete athlete = buildAthlete(1, "alice", "alice@mail.com");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("athlete", athlete);

        Sport sport = new Sport();
        when(sportRepository.findById(1)).thenReturn(Optional.of(sport));
        when(sportService.findAllActive()).thenReturn(List.of());

        // startDate after endDate
        ChallengeFormDTO dto = new ChallengeFormDTO();
        dto.setName("Test");
        dto.setDescription("Desc");
        dto.setMetric(Metric.DISTANCE);
        dto.setStartDate(LocalDate.now().plusDays(5));
        dto.setEndDate(LocalDate.now().plusDays(1));

        String view = controller.createChallenge(dto, 1, session, model);

        assertEquals("challenge/challenge_form", view);
        verify(model).addAttribute(any(String.class), any(String.class));
    }

    @Test
    void shouldReturnFormWhenStartDateIsInPast() throws Exception {
        Athlete athlete = buildAthlete(1, "alice", "alice@mail.com");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("athlete", athlete);

        Sport sport = new Sport();
        when(sportRepository.findById(1)).thenReturn(Optional.of(sport));
        when(sportService.findAllActive()).thenReturn(List.of());

        ChallengeFormDTO dto = new ChallengeFormDTO();
        dto.setName("Test");
        dto.setDescription("Desc");
        dto.setMetric(Metric.DISTANCE);
        dto.setStartDate(LocalDate.now().minusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(5));

        String view = controller.createChallenge(dto, 1, session, model);

        assertEquals("challenge/challenge_form", view);
    }

    @Test
    void shouldCreateChallengeSuccessfully() throws Exception {
        Athlete athlete = buildAthlete(1, "alice", "alice@mail.com");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("athlete", athlete);

        Sport sport = new Sport();
        when(sportRepository.findById(1)).thenReturn(Optional.of(sport));
        when(challengeRepository.save(any(Challenge.class))).thenAnswer(inv -> inv.getArgument(0));

        ChallengeFormDTO dto = buildValidDto();
        String view = controller.createChallenge(dto, 1, session, model);

        assertEquals("redirect:/challenges", view);
        verify(challengeRepository).save(any(Challenge.class));
        verify(challengeRankingService).recomputeRanking(any(Challenge.class));
    }

    // =====================================================================
    // listChallenges
    // =====================================================================

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
    void shouldRedirectToLoginOnListChallengesWhenNotAuthenticated() {
        MockHttpSession session = new MockHttpSession();
        String view = controller.listChallenges(session, model);
        assertEquals("redirect:/login", view);
    }

    // =====================================================================
    // participateInChallenge
    // =====================================================================

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
    void shouldNotAddDuplicateParticipant() throws Exception {
        Athlete athlete = buildAthlete(2, "bob", "bob@mail.com");
        Challenge challenge = new Challenge();
        setPrivateField(Challenge.class, challenge, "id", 42);
        challenge.setParticipants(new ArrayList<>(List.of(athlete)));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("athlete", athlete);

        when(challengeRepository.findById(42)).thenReturn(Optional.of(challenge));

        String view = controller.participateInChallenge(42, session);

        assertEquals("redirect:/challenges", view);
        // should NOT save since athlete is already a participant
        verifyNoInteractions(challengeRankingService);
    }

    @Test
    void shouldRedirectToLoginOnParticipateWhenNotAuthenticated() {
        MockHttpSession session = new MockHttpSession();
        String view = controller.participateInChallenge(1, session);
        assertEquals("redirect:/login", view);
    }

    @Test
    void shouldHandleMissingChallengeOnParticipate() throws Exception {
        Athlete athlete = buildAthlete(2, "bob", "bob@mail.com");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("athlete", athlete);

        when(challengeRepository.findById(999)).thenReturn(Optional.empty());

        String view = controller.participateInChallenge(999, session);
        assertEquals("redirect:/challenges", view);
    }

    // =====================================================================
    // listAllChallenges
    // =====================================================================

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

    // =====================================================================
    // Helpers
    // =====================================================================

    private ChallengeFormDTO buildValidDto() {
        ChallengeFormDTO dto = new ChallengeFormDTO();
        dto.setName("TestChallenge");
        dto.setDescription("A test challenge");
        dto.setMetric(Metric.DISTANCE);
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(10));
        return dto;
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
