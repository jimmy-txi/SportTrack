package fr.utc.miage.sporttrack.entity.user.communication;

import fr.utc.miage.sporttrack.entity.user.Athlete;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * JPA entity representing a private message exchanged between two athletes
 * within the SportTrack application.
 *
 * <p>Each message has an initiator (sender) and a recipient. The message
 * tracks whether it has been seen by the recipient. The send timestamp is
 * automatically set upon first persistence if not already provided.</p>
 *
 * @author SportTrack Team
 */
@Entity
@Table(name = "messages")
public class Message {

    /** The unique database-generated identifier for this message. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The athlete who sent the message. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "initiator_id", nullable = false)
    private Athlete initiator;

    /** The athlete who received the message. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Athlete recipient;

    /** The textual content of the message. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** The timestamp at which the message was sent. */
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    /** Indicates whether the recipient has viewed (seen) this message. */
    @Column(nullable = false)
    private boolean seen;

    /**
     * No-argument constructor required by JPA.
     */
    public Message() {
        // Required by JPA
    }

    /**
     * Returns the unique identifier of this message.
     *
     * @return the message's database identifier
     */
    public Integer getId() {
        return id;
    }

    /**
     * Returns the athlete who sent this message.
     *
     * @return the sender {@link Athlete}
     */
    public Athlete getInitiator() {
        return initiator;
    }

    /**
     * Returns the athlete who received this message.
     *
     * @return the recipient {@link Athlete}
     */
    public Athlete getRecipient() {
        return recipient;
    }

    /**
     * Returns the textual content of this message.
     *
     * @return the message content
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the timestamp at which this message was sent.
     *
     * @return the send timestamp
     */
    public LocalDateTime getSentAt() {
        return sentAt;
    }

    /**
     * Returns whether this message has been seen by the recipient.
     *
     * @return {@code true} if the message has been viewed, {@code false} otherwise
     */
    public boolean isSeen() {
        return seen;
    }

    /**
     * Sets the unique identifier of this message.
     *
     * @param id the database identifier to assign
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Sets the athlete who sent this message.
     *
     * @param initiator the sender {@link Athlete} to assign
     */
    public void setInitiator(Athlete initiator) {
        this.initiator = initiator;
    }

    /**
     * Sets the athlete who received this message.
     *
     * @param recipient the recipient {@link Athlete} to assign
     */
    public void setRecipient(Athlete recipient) {
        this.recipient = recipient;
    }

    /**
     * Sets the textual content of this message.
     *
     * @param content the message text to assign
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Sets the timestamp at which this message was sent.
     *
     * @param sentAt the send timestamp to assign
     */
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    /**
     * Sets whether this message has been seen by the recipient.
     *
     * @param seen {@code true} to mark as seen, {@code false} otherwise
     */
    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    /**
     * Lifecycle callback that sets the send timestamp to the current time
     * if it has not already been provided, before the entity is first persisted.
     */
    @PrePersist
    public void onCreate() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }
}