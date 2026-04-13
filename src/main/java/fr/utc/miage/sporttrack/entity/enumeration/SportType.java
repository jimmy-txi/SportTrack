package fr.utc.miage.sporttrack.entity.enumeration;

public enum SportType {
    DURATION("Durée"),
    REPETITION("Répétition"),
    DISTANCE("Distance");

    private final String displayValue;

    SportType(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
