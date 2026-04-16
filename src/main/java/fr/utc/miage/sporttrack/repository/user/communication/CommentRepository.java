package fr.utc.miage.sporttrack.repository.user.communication;

import fr.utc.miage.sporttrack.entity.user.communication.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByActivityIdOrderByCreatedAtDesc(int activityId);
}
