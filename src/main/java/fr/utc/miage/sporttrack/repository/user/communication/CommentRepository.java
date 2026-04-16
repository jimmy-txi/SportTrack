package fr.utc.miage.sporttrack.repository.user.communication;

import fr.utc.miage.sporttrack.entity.user.communication.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Comment} entities.
 *
 * <p>Provides standard CRUD operations as well as custom query methods
 * for retrieving comments associated with a specific activity.</p>
 *
 * @author SportTrack Team
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    /**
     * Finds all comments attached to the specified activity, ordered by creation time descending.
     *
     * @param activityId the unique identifier of the activity
     * @return a list of comments for the activity, newest first
     */
    List<Comment> findByActivityIdOrderByCreatedAtDesc(int activityId);
}