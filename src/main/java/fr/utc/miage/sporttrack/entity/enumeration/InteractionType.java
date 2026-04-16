package fr.utc.miage.sporttrack.entity.enumeration;

public enum InteractionType {
    LIKE("赞"),
    CHEER("加油"),
    AVERAGE("一般般"),
    NONE("无");

    private final String displayValue;

    InteractionType(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
