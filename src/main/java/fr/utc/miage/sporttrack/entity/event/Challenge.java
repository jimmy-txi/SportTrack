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

@Entity
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String description;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "Metric")
    private Metric metric;

    @ManyToOne
    @JoinColumn(name = "sport_id")
    private Sport sport;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private Athlete organizer;

    @ManyToMany
    @JoinTable(
        name = "challenge_participants",
        joinColumns = @JoinColumn(name = "challenge_id"),
        inverseJoinColumns = @JoinColumn(name = "athlete_id")
    )private List<Athlete> participants;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rankPosition ASC")
    private List<ChallengeRanking> rankings = new ArrayList<>();

    @Column(name = "ended_notified_at")
    private LocalDateTime endedNotifiedAt;

    public Challenge() {}

    public Challenge(String name, String description, LocalDate startDate, LocalDate endDate, Metric metric) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.metric = metric;
    }

    public Athlete getOrganizer() {
        return organizer;
    }
 
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<Athlete> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Athlete> participants) {
        this.participants = participants;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public Sport getSport() {
        return sport;
    }

    public void setOrganizer(Athlete organizer) {
        this.organizer = organizer;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
    }

    public List<ChallengeRanking> getRankings() {
        return rankings;
    }

    public LocalDateTime getEndedNotifiedAt() {
        return endedNotifiedAt;
    }

    public void setRankings(List<ChallengeRanking> rankings) {
        this.rankings.clear();
        if (rankings == null) {
            return;
        }
        for (ChallengeRanking ranking : rankings) {
            addRanking(ranking);
        }
    }

    public void addRanking(ChallengeRanking ranking) {
        if (ranking == null) {
            return;
        }
        ranking.setChallenge(this);
        this.rankings.add(ranking);
    }

    public void setEndedNotifiedAt(LocalDateTime endedNotifiedAt) {
        this.endedNotifiedAt = endedNotifiedAt;
    }

    public int getId() {
        return id;
    }
}
