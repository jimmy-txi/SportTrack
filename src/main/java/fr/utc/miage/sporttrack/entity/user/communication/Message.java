package fr.utc.miage.sporttrack.entity.user.communication;

import fr.utc.miage.sporttrack.entity.user.Athlete;

public class Message {

    private Athlete initiator;
    private Athlete recipient;
    private String content;
    private boolean seen;

    public Message() {}
}
