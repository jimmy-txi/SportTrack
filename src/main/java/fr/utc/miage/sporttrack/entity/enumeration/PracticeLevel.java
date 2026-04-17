package fr.utc.miage.sporttrack.entity.enumeration;

/**
 * Enumeration representing the sport practice levels available for an athlete
 * within the SportTrack application.
 *
 * <p>Each constant carries a localized display value (in French) for rendering
 * in the user interface. The practice level helps categorize athletes by their
 * experience and proficiency in a given sport.</p>
 *
 * @author SportTrack Team
 */
public enum PracticeLevel {

    /** Represents an athlete who is new to the sport with little or no prior experience. */
    BEGINNER("Débutant"),

    /** Represents an athlete with a moderate level of experience in the sport. */
    INTERMEDIATE("Intermédiaire"),

    /** Represents an athlete with a high level of experience and proficiency in the sport. */
    ADVANCED("Avancé");

    /** The localized display label for this practice level, intended for UI rendering. */
    private final String displayValue;

    /**
     * Constructs a {@code PracticeLevel} constant with the specified display value.
     *
     * @param displayValue the localized French label to display in the user interface
     */
    PracticeLevel(String displayValue) {
        this.displayValue = displayValue;
    }

    /**
     * Returns the localized display label associated with this practice level.
     *
     * @return the French display string for this practice level
     */
    public String getDisplayValue() {
        return displayValue;
    }
}