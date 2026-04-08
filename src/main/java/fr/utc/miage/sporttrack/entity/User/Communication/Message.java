package fr.utc.miage.sporttrack.entity.User.Communication;

import fr.utc.miage.sporttrack.entity.User.Athlete;

public class Message {

    private Athlete initiator;
    private Athlete recipient;
    private String content;
    private boolean seen;

    public Message() {}
}
