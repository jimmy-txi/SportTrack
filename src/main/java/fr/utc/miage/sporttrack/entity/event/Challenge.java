package fr.utc.miage.sporttrack.entity.event;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA entity representing a competitive challenge within the SportTrack application.
 *
 * <p>A challenge is organised by an athlete and allows other athletes to compete
 * against one another based on a specific sport and performance metric (e.g.,
 * total distance run, total duration). Rankings are maintained as an ordered
 * list of {@link ChallengeRanking} entries. The challenge has defined start and
 * end dates, after which the final standings are determined.</p>
 *
 * @author SportTrack Team
 */
@Entity
public class Challenge {

    /** The unique database-generated identifier for this challenge. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** The display name of the challenge. */
    private String name;

    /** A textual description outlining the rules or objectives of the challenge. */
    private String description;

    /** The date on which the challenge ends and final results are computed. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    /** The date on which the challenge begins. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** The performance metric used to rank participants in this challenge. */
    @Enumerated(EnumType.STRING)
    @Column(name = "Metric")
    private Metric metric;

    /** The sport discipline targeted by this challenge. */
    @ManyToOne
    @JoinColumn(name = "sport_id")
    private Sport sport;

    /** The athlete who organised (created) this challenge. */
    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private Athlete organizer;

    /** The list of athletes participating in this challenge. */
    @ManyToMany
    @JoinTable(
        name = "challenge_participants",
        joinColumns = @JoinColumn(name = "challenge_id"),
        inverseJoinColumns = @JoinColumn(name = "athlete_id")
    )private List<Athlete> participants;

    /** The ordered list of rankings for this challenge, sorted by rank position. */
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rankPosition ASC")
    private List<ChallengeRanking> rankings = new ArrayList<>();

    /** The timestamp at which the end-of-challenge notification was sent to participants. */
    @Column(name = "ended_notified_at")
    private LocalDateTime endedNotifiedAt;

    /**
     * No-argument constructor required by JPA.
     */
    public Challenge() {}

    /**
     * Constructs a new challenge with the specified name, description, date range, and metric.
     *
     * @param name        the display name of the challenge
     * @param description the textual description of the challenge
     * @param startDate   the start date of the challenge
     * @param endDate     the end date of the challenge
     * @param metric      the performance metric used for ranking
     */
    public Challenge(String name, String description, LocalDate startDate, LocalDate endDate, Metric metric) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.metric = metric;
    }

    /**
     * Returns the athlete who organised this challenge.
     *
     * @return the organiser {@link Athlete}
     */
    public Athlete getOrganizer() {
        return organizer;
    }

    /**
     * Returns the display name of this challenge.
     *
     * @return the challenge name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this challenge.
     *
     * @param name the challenge name to assign
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the textual description of this challenge.
     *
     * @return the challenge description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the textual description of this challenge.
     *
     * @param description the description to assign
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the start date of this challenge.
     *
     * @return the start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date of this challenge.
     *
     * @param startDate the start date to assign
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the end date of this challenge.
     *
     * @return the end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date of this challenge.
     *
     * @param endDate the end date to assign
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns the list of athletes participating in this challenge.
     *
     * @return the list of participant {@link Athlete}s
     */
    public List<Athlete> getParticipants() {
        return participants;
    }

    /**
     * Sets the list of athletes participating in this challenge.
     *
     * @param participants the list of participant {@link Athlete}s to assign
     */
    public void setParticipants(List<Athlete> participants) {
        this.participants = participants;
    }

    /**
     * Returns the performance metric used for ranking in this challenge.
     *
     * @return the {@link Metric}
     */
    public Metric getMetric() {
        return metric;
    }

    /**
     * Sets the performance metric used for ranking in this challenge.
     *
     * @param metric the {@link Metric} to assign
     */
    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    /**
     * Returns the sport associated with this challenge.
     *
     * @return the associated {@link Sport}
     */
    public Sport getSport() {
        return sport;
    }

    /**
     * Sets the athlete who organised this challenge.
     *
     * @param organizer the organiser {@link Athlete} to assign
     */
    public void setOrganizer(Athlete organizer) {
        this.organizer = organizer;
    }

    /**
     * Sets the sport associated with this challenge.
     *
     * @param sport the {@link Sport} to associate
     */
    public void setSport(Sport sport) {
        this.sport = sport;
    }

    /**
     * Returns the ordered list of rankings for this challenge.
     *
     * @return the list of {@link ChallengeRanking} entries, sorted by rank position
     */
    public List<ChallengeRanking> getRankings() {
        return rankings;
    }

    /**
     * Returns the timestamp at which the end-of-challenge notification was sent.
     *
     * @return the notification timestamp, or {@code null} if not yet notified
     */
    public LocalDateTime getEndedNotifiedAt() {
        return endedNotifiedAt;
    }

    /**
     * Replaces the entire ranking list with the provided entries, maintaining
     * the parent-child relationship on each ranking.
     *
     * @param rankings the new list of {@link ChallengeRanking} entries to assign;
     *                 if {@code null}, the list is simply cleared
     */
    public void setRankings(List<ChallengeRanking> rankings) {
        this.rankings.clear();
        if (rankings == null) {
            return;
        }
        for (ChallengeRanking ranking : rankings) {
            addRanking(ranking);
        }
    }

    /**
     * Adds a single ranking entry to this challenge and establishes the
     * bidirectional relationship.
     *
     * @param ranking the {@link ChallengeRanking} to add; ignored if {@code null}
     */
    public void addRanking(ChallengeRanking ranking) {
        if (ranking == null) {
            return;
        }
        ranking.setChallenge(this);
        this.rankings.add(ranking);
    }

    /**
     * Sets the timestamp at which the end-of-challenge notification was sent.
     *
     * @param endedNotifiedAt the notification timestamp to assign
     */
    public void setEndedNotifiedAt(LocalDateTime endedNotifiedAt) {
        this.endedNotifiedAt = endedNotifiedAt;
    }

    /**
     * Returns the unique identifier of this challenge.
     *
     * @return the challenge's database identifier
     */
    public int getId() {
        return id;
    }
}