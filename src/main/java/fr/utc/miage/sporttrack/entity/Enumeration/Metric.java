package fr.utc.miage.sporttrack.entity.enumeration;

public enum Metric {
    DURATION("Durée"),
    REPETITION("Répétition"),
    DISTANCE("Distance"),
    MEAN_VELOCITY("Vitesse moyenne"),
    REPS_PER_MINUTE("Répétitions par minute");

    private final String displayValue;

    Metric(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
