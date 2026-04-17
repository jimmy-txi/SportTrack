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

/**
 * Service layer component responsible for computing and persisting
 * {@link ChallengeRanking} entries for {@link Challenge} entities.
 *
 * <p>Rankings are recalculated whenever an activity is created, updated, or deleted
 * that impacts an active challenge. Scores are computed based on the challenge's
 * chosen {@link fr.utc.miage.sporttrack.entity.enumeration.Metric}.</p>
 *
 * @author SportTrack Team
 */
@Service
public class ChallengeRankingService {

    /** The repository for challenge data access. */
    private final ChallengeRepository challengeRepository;

    /** The repository for activity data access. */
    private final ActivityRepository activityRepository;

    /**
     * Constructs a new {@code ChallengeRankingService} with the required repositories.
     *
     * @param challengeRepository the challenge repository
     * @param activityRepository  the activity repository
     */
    public ChallengeRankingService(ChallengeRepository challengeRepository, ActivityRepository activityRepository) {
        this.challengeRepository = challengeRepository;
        this.activityRepository = activityRepository;
    }

    /**
     * Recomputes rankings for all challenges impacted by a new or modified activity.
     *
     * @param athleteId    the identifier of the athlete whose activity changed
     * @param sportId      the identifier of the sport associated with the activity
     * @param activityDate the date of the activity
     */
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

    /**
     * Recomputes and persists the full ranking for the given challenge.
     *
     * @param challenge the challenge whose rankings should be recomputed
     * @throws IllegalArgumentException if the challenge uses COUNT as a metric, which is not allowed for challenges
     */
    public void recomputeRanking(Challenge challenge) {
        if (challenge == null || challenge.getId() <= 0) {
            return;
        }

        if (challenge.getMetric() == Metric.COUNT) {
            throw new IllegalArgumentException("COUNT metric is not allowed for challenges. It is reserved for badge verification only.");
        }

        List<ChallengeRanking> computedRanking = buildRanking(challenge);
        if (!computedRanking.isEmpty()) {
            challenge.setRankings(computedRanking);
            challengeRepository.save(challenge);
        }
    }

    /**
     * Builds a sorted list of ranking entries for the given challenge
     * by aggregating participant activity data.
     *
     * @param challenge the challenge for which to build rankings
     * @return a sorted list of {@link ChallengeRanking} entries
     */
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

    /**
     * Computes a participant's score for a challenge based on the specified metric
     * and the given list of activities.
     *
     * @param metric        the metric to use for scoring
     * @param participantId the identifier of the participant
     * @param activities    the list of challenge-relevant activities
     * @return the computed score as a double
     */
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
            case COUNT -> {
                throw new IllegalArgumentException("COUNT metric is not supported for challenge rankings.");
            }
        };
    }
}
