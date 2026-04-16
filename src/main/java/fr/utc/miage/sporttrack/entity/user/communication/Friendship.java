package fr.utc.miage.sporttrack.entity.user.communication;

import fr.utc.miage.sporttrack.entity.enumeration.FriendshipStatus;
import fr.utc.miage.sporttrack.entity.user.Athlete;
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

/**
 * JPA entity representing a friendship relationship between two athletes
 * within the SportTrack application.
 *
 * <p>A friendship has an initiator (the athlete who sent the request) and a
 * recipient (the athlete who received it). The relationship progresses through
 * statuses defined by {@link FriendshipStatus}, from {@code PENDING} to
 * {@code ACCEPTED}, {@code REJECTED}, or {@code BLOCKED}.</p>
 *
 * @author SportTrack Team
 */
@Entity
@Table(name = "friendships")
public class Friendship {

    /** The unique database-generated identifier for this friendship. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The athlete who initiated the friendship request. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "initiator_id", nullable = false)
    private Athlete initiator;

    /** The athlete who received the friendship request. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Athlete recipient;

    /** The current status of the friendship (e.g., PENDING, ACCEPTED). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;

    /** The timestamp at which this friendship record was created. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * No-argument constructor required by JPA.
     */
    public Friendship() {
    }

    /**
     * Constructs a new friendship with the specified initiator and recipient.
     * The status is set to {@link FriendshipStatus#PENDING} and the creation
     * timestamp is set to the current time.
     *
     * @param initiator the athlete who sent the friendship request
     * @param recipient the athlete who received the friendship request
     */
    public Friendship(Athlete initiator, Athlete recipient) {
        this.initiator = initiator;
        this.recipient = recipient;
        this.status = FriendshipStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructs a new friendship with the specified initiator, recipient,
     * and initial status. The creation timestamp is set to the current time.
     *
     * @param initiator the athlete who sent the friendship request
     * @param recipient the athlete who received the friendship request
     * @param status    the initial {@link FriendshipStatus} of the relationship
     */
    public Friendship(Athlete initiator, Athlete recipient, FriendshipStatus status) {
        this.initiator = initiator;
        this.recipient = recipient;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }


    /**
     * Returns the unique identifier of this friendship.
     *
     * @return the friendship's database identifier
     */
    public Integer getId() {
        return id;
    }

    /**
     * Returns the athlete who initiated the friendship request.
     *
     * @return the initiator {@link Athlete}
     */
    public Athlete getInitiator() {
        return initiator;
    }

    /**
     * Returns the athlete who received the friendship request.
     *
     * @return the recipient {@link Athlete}
     */
    public Athlete getRecipient() {
        return recipient;
    }

    /**
     * Returns the current status of this friendship.
     *
     * @return the {@link FriendshipStatus}
     */
    public FriendshipStatus getStatus() {
        return status;
    }

    /**
     * Returns the creation timestamp of this friendship record.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the status of this friendship.
     *
     * @param status the {@link FriendshipStatus} to assign
     */
    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }

    /**
     * Sets the creation timestamp of this friendship record.
     *
     * @param createdAt the creation timestamp to assign
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Sets the athlete who initiated the friendship request.
     *
     * @param initiator the initiator {@link Athlete} to assign
     */
    public void setInitiator(Athlete initiator) {
        this.initiator = initiator;
    }

    /**
     * Sets the athlete who received the friendship request.
     *
     * @param recipient the recipient {@link Athlete} to assign
     */
    public void setRecipient(Athlete recipient) {
        this.recipient = recipient;
    }

    /**
     * Compares this friendship to another object for equality based on the
     * database identifier.
     *
     * @param o the object to compare with
     * @return {@code true} if the objects are considered equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friendship that)) return false;
        return Objects.equals(id, that.id);
    }

    /**
     * Returns the hash code for this friendship, computed from the identifier.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of this friendship, including the
     * identifier, initiator, recipient, status, and creation timestamp.
     *
     * @return a descriptive string representation
     */
    @Override
    public String toString() {
        return "Friendship{" + "id=" + id + ", initiator=" + (initiator != null ? initiator.getId() : null) + ", recipient=" + (recipient != null ? recipient.getId() : null) + ", status=" + status + ", createdAt=" + createdAt + '}';
    }
}