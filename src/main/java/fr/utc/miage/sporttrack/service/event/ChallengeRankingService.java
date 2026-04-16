package fr.utc.miage.sporttrack.service.event;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.entity.event.Challenge;
import fr.utc.miage.sporttrack.entity.event.ChallengeRanking;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.repository.activity.ActivityRepository;
import fr.utc.miage.sporttrack.repository.event.ChallengeRepository;

@Service
public class ChallengeRankingService {

    private final ChallengeRepository challengeRepository;
    private final ActivityRepository activityRepository;

    public ChallengeRankingService(ChallengeRepository challengeRepository, ActivityRepository activityRepository) {
        this.challengeRepository = challengeRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional
    public void recomputeRankingsForActivity(Integer athleteId, Integer sportId, LocalDate activityDate) {
        if (athleteId == null || sportId == null || activityDate == null) {
            return;
        }

        List<Challenge> impactedChallenges = challengeRepository.findChallengesImpactedByActivity(athleteId, sportId, activityDate);
        for (Challenge challenge : impactedChallenges) {
            recomputeRanking(challenge);
        }
    }

    @Transactional
    public void recomputeRankingByChallengeId(Integer challengeId) {
        if (challengeId == null) {
            return;
        }
        challengeRepository.findById(challengeId).ifPresent(this::recomputeRanking);
    }

    public void recomputeRanking(Challenge challenge) {
        if (challenge == null || challenge.getId() <= 0) {
            return;
        }

        List<ChallengeRanking> computedRanking = buildRanking(challenge);
        challenge.setRankings(computedRanking);
        challengeRepository.save(challenge);
    }

    private List<ChallengeRanking> buildRanking(Challenge challenge) {
        if (challenge.getParticipants() == null || challenge.getParticipants().isEmpty()) {
            return List.of();
        }
        if (challenge.getSport() == null || challenge.getStartDate() == null || challenge.getEndDate() == null || challenge.getMetric() == null) {
            return List.of();
        }

        List<Integer> participantIds = challenge.getParticipants().stream()
                .map(Athlete::getId)
                .filter(Objects::nonNull)
                .toList();

        if (participantIds.isEmpty()) {
            return List.of();
        }

        List<Activity> challengeActivities = activityRepository.findActivitiesForChallengeRanking(
                participantIds,
                challenge.getSport().getId(),
                challenge.getStartDate(),
                challenge.getEndDate()
        );

        List<ChallengeRanking> rankingEntries = new ArrayList<>();
        for (Athlete participant : challenge.getParticipants()) {
            if (participant == null || participant.getId() == null) {
                continue;
            }

            double score = computeScoreByMetric(challenge.getMetric(), participant.getId(), challengeActivities);
            ChallengeRanking ranking = new ChallengeRanking();
            ranking.setAthlete(participant);
            ranking.setScore(score);
            ranking.setChallenge(challenge);
            rankingEntries.add(ranking);
        }

        rankingEntries.sort(Comparator
                .comparingDouble(ChallengeRanking::getScore)
                .reversed()
                .thenComparing(ChallengeRanking::getDisplayName, String.CASE_INSENSITIVE_ORDER));

        for (int i = 0; i < rankingEntries.size(); i++) {
            rankingEntries.get(i).setRankPosition(i + 1);
        }

        return rankingEntries;
    }

    private double computeScoreByMetric(Metric metric, Integer participantId, List<Activity> activities) {
        List<Activity> participantActivities = activities.stream()
                .filter(activity -> activity.getCreatedBy() != null && participantId.equals(activity.getCreatedBy().getId()))
                .toList();

        return switch (metric) {
            case DURATION -> participantActivities.stream().mapToDouble(Activity::getDuration).sum();
            case REPETITION -> participantActivities.stream()
                    .mapToDouble(activity -> activity.getRepetition() != null ? activity.getRepetition() : 0)
                    .sum();
            case DISTANCE -> participantActivities.stream()
                    .mapToDouble(activity -> activity.getDistance() != null ? activity.getDistance() : 0d)
                    .sum();
            case MEAN_VELOCITY -> {
                double totalDistance = participantActivities.stream()
                        .mapToDouble(activity -> activity.getDistance() != null ? activity.getDistance() : 0d)
                        .sum();
                double totalDuration = participantActivities.stream().mapToDouble(Activity::getDuration).sum();
                yield totalDuration > 0 ? totalDistance / totalDuration : 0d;
            }
            case REPS_PER_MINUTE -> {
                double totalRepetitions = participantActivities.stream()
                        .mapToDouble(activity -> activity.getRepetition() != null ? activity.getRepetition() : 0)
                        .sum();
                double totalDurationMinutes = participantActivities.stream().mapToDouble(Activity::getDuration).sum() * 60d;
                yield totalDurationMinutes > 0 ? totalRepetitions / totalDurationMinutes : 0d;
            }
        };
    }
}
