package fr.utc.miage.sporttrack.service.user.communication;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.enumeration.InteractionType;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.user.communication.Comment;
import fr.utc.miage.sporttrack.repository.user.communication.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer component responsible for managing {@link Comment} entities
 * within the SportTrack application.
 *
 * <p>Provides business logic for adding, retrieving, and deleting comments
 * (social interactions) on activities.</p>
 *
 * @author SportTrack Team
 */
@Service
public class CommentService {

    /** The repository used for persisting and retrieving comment entities. */
    private CommentRepository commentRepository;

    /**
     * Constructs a new {@code CommentService} with the given repository.
     *
     * @param commentRepository the repository for comment data access
     */
    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Creates and persists a new comment on an activity.
     *
     * @param author   the athlete who authored the comment
     * @param activity the activity to which the comment is attached
     * @param content  the textual content of the comment
     * @param type     the type of social interaction (e.g., LIKE, CHEER)
     * @return the newly created and persisted {@link Comment}
     */
    public Comment addComment(Athlete author, Activity activity, String content, InteractionType type) {
        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setActivity(activity);
        comment.setContent(content);
        comment.setInteractionType(type);
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    /**
     * Returns all comments for the specified activity, ordered by creation time descending.
     *
     * @param activityId the unique identifier of the activity
     * @return a list of comments, newest first
     */
    public List<Comment> getCommentsForActivity(int activityId) {
        return commentRepository.findByActivityIdOrderByCreatedAtDesc(activityId);
    }

    /**
     * Deletes the comment with the given identifier.
     *
     * @param commentId the unique identifier of the comment to delete
     */
    public void deleteComment(int commentId) {
        commentRepository.deleteById(commentId);
    }
}