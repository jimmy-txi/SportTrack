package fr.utc.miage.sporttrack.entity.enumeration;

/**
 * Enumeration representing the types of social interactions that an athlete
 * can perform on another athlete's activity within the SportTrack application.
 *
 * <p>Each constant carries a localized display value (in Chinese) used for
 * rendering the interaction label in the user interface.</p>
 *
 * @author SportTrack Team
 */
public enum InteractionType {

    /** Indicates a positive "like" reaction to an activity. */
    LIKE("赞"),

    /** Indicates an encouraging "cheer" reaction to an activity. */
    CHEER("加油"),

    /** Indicates a neutral or lukewarm response to an activity. */
    AVERAGE("一般般"),

    /** Indicates no interaction has been made on the activity. */
    NONE("无");

    /** The localized display label for this interaction type, intended for UI rendering. */
    private final String displayValue;

    /**
     * Constructs an {@code InteractionType} constant with the specified display value.
     *
     * @param displayValue the localized label to display in the user interface
     */
    InteractionType(String displayValue) {
        this.displayValue = displayValue;
    }

    /**
     * Returns the localized display label associated with this interaction type.
     *
     * @return the display string for this interaction type
     */
    public String getDisplayValue() {
        return displayValue;
    }
}