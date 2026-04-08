package fr.utc.miage.sporttrack.entity;

import java.util.List;

public class Athlete extends User {

    private List<Badge> badges;
    private List<Challenge> historyChallenges;
    private List<Athlete> friends;

    public Athlete() {}

    public List<Badge> getAcquiredBadges() {
        return badges;
    }

    public void addFriend(Athlete athlete) {}

    public List<Athlete> getFriends() {
        return friends;
    }
}
