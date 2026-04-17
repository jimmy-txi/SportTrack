package fr.utc.miage.sporttrack.entity.enumeration;

/**
 * Enumeration representing the various performance metrics that can be used
 * to measure and track an athlete's activity within the SportTrack application.
 *
 * <p>Each constant carries a localized display value (in French) for rendering
 * in the user interface. Metrics range from simple duration and repetition counts
 * to computed values such as mean velocity and reps per minute.</p>
 *
 * @author SportTrack Team
 */
public enum Metric {

    /** Measures the total elapsed time of an activity. */
    DURATION("Durée"),

    /** Counts the number of repetitions performed during an activity. */
    REPETITION("Répétition"),

    /** Measures the total distance covered during an activity. */
    DISTANCE("Distance"),

    /** Represents the average velocity maintained throughout an activity. */
    MEAN_VELOCITY("Vitesse moyenne"),

    /** Represents the number of repetitions performed per minute, indicating pace. */
    REPS_PER_MINUTE("Répétitions par minute"),

    /** Counts the number of different days on which activities were performed. */
    COUNT("Nombre de jours");

    /** The localized display label for this metric, intended for UI rendering. */
    private final String displayValue;

    /**
     * Constructs a {@code Metric} constant with the specified display value.
     *
     * @param displayValue the localized French label to display in the user interface
     */
    Metric(String displayValue) {
        this.displayValue = displayValue;
    }

    /**
     * Returns the localized display label associated with this metric.
     *
     * @return the French display string for this metric
     */
    public String getDisplayValue() {
        return displayValue;
    }

    /**
     * Returns all metrics except COUNT, suitable for challenge creation.
     * COUNT is excluded because it is intended only for badge verification.
     *
     * @return an array of metrics excluding COUNT
     */
    public static Metric[] valuesForChallenges() {
        return new Metric[]{DURATION, REPETITION, DISTANCE, MEAN_VELOCITY, REPS_PER_MINUTE};
    }
}