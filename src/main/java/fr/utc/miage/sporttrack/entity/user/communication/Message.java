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

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "initiator_id", nullable = false)
    private Athlete initiator;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Athlete recipient;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(nullable = false)
    private boolean seen;

    public Message() {
        // Required by JPA
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

    public String getContent() {
        return content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setInitiator(Athlete initiator) {
        this.initiator = initiator;
    }

    public void setRecipient(Athlete recipient) {
        this.recipient = recipient;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @PrePersist
    public void onCreate() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }
}
