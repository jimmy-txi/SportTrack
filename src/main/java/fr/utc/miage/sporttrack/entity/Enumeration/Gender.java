package fr.utc.miage.sporttrack.entity.Enumeration;

public enum Gender {
    MALE("Homme"),
    FEMALE("Femme"),
    OTHER("Autre");

    private final String displayValue;

    Gender(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
