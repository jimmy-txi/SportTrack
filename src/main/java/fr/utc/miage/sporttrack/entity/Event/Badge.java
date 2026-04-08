package fr.utc.miage.sporttrack.entity;

public class Badge {

    private int id;
    private String label;
    private String description;
    private String verificationLambda;
    private boolean makeItBoolean;

    public Badge() {}

    public Badge(String label, String description, Sport sport) {}
}
