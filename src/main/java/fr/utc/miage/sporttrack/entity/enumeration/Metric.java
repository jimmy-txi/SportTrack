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
    REPS_PER_MINUTE("Répétitions par minute");

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
}