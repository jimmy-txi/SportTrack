package fr.utc.miage.sporttrack.entity.enumeration;

public enum PracticeLevel {
    BEGINNER("Débutant"),
    INTERMEDIATE("Intermédiaire"),
    ADVANCED("Avancé");

    private final String displayValue;

    PracticeLevel(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
