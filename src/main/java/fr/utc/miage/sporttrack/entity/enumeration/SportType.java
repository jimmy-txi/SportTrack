package fr.utc.miage.sporttrack.entity.enumeration;

/**
 * Enumeration representing the categorization types for sports within the
 * SportTrack application.
 *
 * <p>Each constant defines how a sport's activity is measured — either by
 * elapsed time, by repetition count, or by distance traveled. The display value
 * is a localized French label used in the user interface.</p>
 *
 * @author SportTrack Team
 */
public enum SportType {

    /** The sport is measured by the total duration of the activity session. */
    DURATION("Durée"),

    /** The sport is measured by the number of repetitions performed. */
    REPETITION("Répétition"),

    /** The sport is measured by the total distance covered during the activity. */
    DISTANCE("Distance");

    /** The localized display label for this sport type, intended for UI rendering. */
    private final String displayValue;

    /**
     * Constructs a {@code SportType} constant with the specified display value.
     *
     * @param displayValue the localized French label to display in the user interface
     */
    SportType(String displayValue) {
        this.displayValue = displayValue;
    }

    /**
     * Returns the localized display label associated with this sport type.
     *
     * @return the French display string for this sport type
     */
    public String getDisplayValue() {
        return displayValue;
    }
}