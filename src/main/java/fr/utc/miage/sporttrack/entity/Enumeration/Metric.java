package fr.utc.miage.sporttrack.entity.enumeration;

public enum Metric {
    Duration("Durée"),
    Repetition("Répétition"),
    Distance("Distance"),
    MeanVelocity("Vitesse moyenne"),
    RepsPerMinute("Répétitions par minute");

    private final String displayValue;

    Metric(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
