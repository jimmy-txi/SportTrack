package fr.utc.miage.sporttrack.util;

/**
 * Utility class for common validation constants and thresholds.
 * Defines magic numbers and boundary values used throughout the application.
 *
 * @author SportTrack Team
 */
public final class ValidationConstants {

    // ID validation
    /** Minimum valid identifier value. */
    public static final int MIN_ID = 1;

    /** Default/invalid identifier value (zero). */
    public static final int INVALID_ID = 0;

    // Percentage
    /** Percentage factor for calculations. */
    public static final double PERCENTAGE_FACTOR = 100.0;

    // Pagination and limits
    /** Default page size for paginated results. */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** Maximum recent items to display. */
    public static final int RECENT_ITEMS_LIMIT = 5;

    /** Limit for recent badges. */
    public static final int RECENT_BADGES_LIMIT = 3;

    // Thresholds
    /** No precipitation threshold in millimeters. */
    public static final double NO_PRECIPITATION = 0d;

    /** Maximum body text length. */
    public static final int MAX_TEXT_LENGTH = 1000;

    /** Maximum comment length. */
    public static final int MAX_COMMENT_LENGTH = 500;

    // String validation
    /** Minimum password length. */
    public static final int MIN_PASSWORD_LENGTH = 6;

    /** Maximum username length. */
    public static final int MAX_USERNAME_LENGTH = 50;

    /** Maximum email length. */
    public static final int MAX_EMAIL_LENGTH = 100;

    // Private constructor to prevent instantiation
    private ValidationConstants() {
        throw new AssertionError("Cannot instantiate ValidationConstants");
    }
}
