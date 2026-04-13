package fr.utc.miage.sporttrack.entity.User.Communication;

import fr.utc.miage.sporttrack.entity.Enumeration.FriendshipStatus;
import fr.utc.miage.sporttrack.entity.User.Athlete;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "friendships")
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "initiator_id", nullable = false)
    private Athlete initiator;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Athlete recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Friendship() {
    }

    public Friendship(Athlete initiator, Athlete recipient) {
        this.initiator = initiator;
        this.recipient = recipient;
        this.status = FriendshipStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }


    public Integer getId() {
        return id;
    }

    public Athlete getInitiator() {
        return initiator;
    }

    public Athlete getRecipient() {
        return recipient;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setInitiator(Athlete initiator) {
        this.initiator = initiator;
    }

    public void setRecipient(Athlete recipient) {
        this.recipient = recipient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friendship that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Friendship{" + "id=" + id + ", initiator=" + (initiator != null ? initiator.getIdU() : null) + ", recipient=" + (recipient != null ? recipient.getIdU() : null) + ", status=" + status + ", createdAt=" + createdAt + '}';
    }
}
