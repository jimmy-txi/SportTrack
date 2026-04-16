package fr.utc.miage.sporttrack.entity.event;

import java.util.Locale;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ChallengeRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int rankPosition;

    private double score;

    @ManyToOne
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @ManyToOne
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete;

    public int getId() {
        return id;
    }

    public int getRankPosition() {
        return rankPosition;
    }

    public void setRankPosition(int rankPosition) {
        this.rankPosition = rankPosition;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public Athlete getAthlete() {
        return athlete;
    }

    public void setAthlete(Athlete athlete) {
        this.athlete = athlete;
    }

    public String getDisplayName() {
        if (athlete == null) {
            return "Inconnu";
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

        return athlete.getEmail() != null ? athlete.getEmail() : "Inconnu";
    }

    public String getFormattedScore() {
        if (Double.isNaN(score) || Double.isInfinite(score)) {
            return "0";
        }

        if (Math.rint(score) == score) {
            return String.format(Locale.FRANCE, "%.0f", score);
        }

        return String.format(Locale.FRANCE, "%.2f", score);
    }
}
