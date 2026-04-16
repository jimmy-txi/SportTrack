package fr.utc.miage.sporttrack.util;

/**
 * Shared text normalization helpers to keep setters consistent across DTOs/entities.
 */
public final class TextNormalizer {

    private TextNormalizer() {
        // Utility class.
    }

    public static String trimNullable(String value) {
        return value == null ? null : value.trim();
    }
}
