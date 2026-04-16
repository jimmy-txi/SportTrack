package fr.utc.miage.sporttrack.entity.enumeration;

/**
 * Enumeration representing the gender options available for a user within the SportTrack application.
 *
 * <p>Each constant holds a localized display value (in French) used for presentation
 * purposes in the user interface.</p>
 *
 * @author SportTrack Team
 */
public enum Gender {

    /** Represents the male gender. */
    MALE("Homme"),

    /** Represents the female gender. */
    FEMALE("Femme"),

    /** Represents a gender identity that does not fall into the male or female categories. */
    OTHER("Autre");

    /** The localized display label for this gender option, intended for UI rendering. */
    private final String displayValue;

    /**
     * Constructs a {@code Gender} constant with the specified display value.
     *
     * @param displayValue the localized French label to display in the user interface
     */
    Gender(String displayValue) {
        this.displayValue = displayValue;
    }

    /**
     * Returns the localized display label associated with this gender constant.
     *
     * @return the French display string for this gender
     */
    public String getDisplayValue() {
        return displayValue;
    }
}