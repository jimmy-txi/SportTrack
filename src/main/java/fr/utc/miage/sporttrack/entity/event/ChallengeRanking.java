package fr.utc.miage.sporttrack.entity.event;

import java.util.Locale;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * JPA entity representing a single ranking entry within a {@link Challenge}.
 *
 * <p>Each ranking associates an athlete with a rank position and a cumulative
 * score for the challenge's performance metric. Convenience methods are provided
 * to obtain a display name and a formatted score string for presentation in the
 * user interface.</p>
 *
 * @author SportTrack Team
 */
@Entity
public class ChallengeRanking {

    /** The unique database-generated identifier for this ranking entry. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** The position of the athlete in the challenge ranking (1 = first place). */
    private int rankPosition;

    /** The cumulative score achieved by the athlete for the challenge's metric. */
    private double score;

    /** The challenge to which this ranking entry belongs. */
    @ManyToOne
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    /** The athlete associated with this ranking entry. */
    @ManyToOne
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete;

    /**
     * Returns the unique identifier of this ranking entry.
     *
     * @return the database identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the rank position of the athlete in the challenge.
     *
     * @return the rank position (1-based)
     */
    public int getRankPosition() {
        return rankPosition;
    }

    /**
     * Sets the rank position of the athlete.
     *
     * @param rankPosition the rank position to assign (1-based)
     */
    public void setRankPosition(int rankPosition) {
        this.rankPosition = rankPosition;
    }

    /**
     * Returns the cumulative score achieved by the athlete.
     *
     * @return the score value
     */
    public double getScore() {
        return score;
    }

    /**
     * Sets the cumulative score achieved by the athlete.
     *
     * @param score the score value to assign
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * Returns the challenge to which this ranking belongs.
     *
     * @return the parent {@link Challenge}
     */
    public Challenge getChallenge() {
        return challenge;
    }

    /**
     * Sets the challenge to which this ranking belongs.
     *
     * @param challenge the parent {@link Challenge} to assign
     */
    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    /**
     * Returns the athlete associated with this ranking.
     *
     * @return the ranked {@link Athlete}
     */
    public Athlete getAthlete() {
        return athlete;
    }

    /**
     * Sets the athlete associated with this ranking.
     *
     * @param athlete the {@link Athlete} to assign
     */
    public void setAthlete(Athlete athlete) {
        this.athlete = athlete;
    }

    /**
     * Returns the display name of the ranked athlete.
     * Falls back to the username or email if the full name is unavailable,
     * and ultimately returns "Inconnu" if no athlete is associated.
     *
     * @return the athlete's display name
     */
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

    /**
     * Returns the score formatted for display in the French locale.
     * Integer scores are shown without decimal places; non-integer scores
     * are shown with two decimal places. Handles {@code NaN} and infinite
     * values by returning "0".
     *
     * @return the formatted score string
     */
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