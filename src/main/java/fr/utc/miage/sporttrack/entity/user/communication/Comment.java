package fr.utc.miage.sporttrack.entity.user.communication;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.enumeration.InteractionType;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a comment or interaction left by an athlete on an
 * activity within the SportTrack application.
 *
 * <p>Each comment is associated with an author (athlete), a target activity,
 * an interaction type (e.g., like, cheer), and an optional textual content.
 * The creation timestamp is automatically set upon persistence.</p>
 *
 * @author SportTrack Team
 */
@Entity
public class Comment {

    /** The unique database-generated identifier for this comment. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** The athlete who authored this comment. */
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private Athlete author;

    /** The activity to which this comment is attached. */
    @ManyToOne
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    /** The type of social interaction represented by this comment (e.g., LIKE, CHEER). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InteractionType interactionType;

    /** The optional textual content of the comment. */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** The timestamp at which this comment was created. */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Lifecycle callback that automatically sets the creation timestamp
     * before the entity is first persisted.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * No-argument constructor required by JPA.
     */
    public Comment() {
        // normal if empty
    }

    /**
     * Returns the unique identifier of this comment.
     *
     * @return the comment's database identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this comment.
     *
     * @param id the database identifier to assign
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the athlete who authored this comment.
     *
     * @return the author {@link Athlete}
     */
    public Athlete getAuthor() {
        return author;
    }

    /**
     * Sets the athlete who authored this comment.
     *
     * @param author the author {@link Athlete} to assign
     */
    public void setAuthor(Athlete author) {
        this.author = author;
    }

    /**
     * Returns the activity to which this comment is attached.
     *
     * @return the associated {@link Activity}
     */
    public Activity getActivity() {
        return activity;
    }

    /**
     * Sets the activity to which this comment is attached.
     *
     * @param activity the {@link Activity} to associate
     */
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * Returns the interaction type of this comment.
     *
     * @return the {@link InteractionType}
     */
    public InteractionType getInteractionType() {
        return interactionType;
    }

    /**
     * Sets the interaction type of this comment.
     *
     * @param interactionType the {@link InteractionType} to assign
     */
    public void setInteractionType(InteractionType interactionType) {
        this.interactionType = interactionType;
    }

    /**
     * Returns the textual content of this comment.
     *
     * @return the comment text, or {@code null} if only an interaction type was used
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the textual content of this comment.
     *
     * @param content the comment text to assign
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the creation timestamp of this comment.
     *
     * @return the timestamp when the comment was created
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp of this comment.
     *
     * @param createdAt the creation timestamp to assign
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}