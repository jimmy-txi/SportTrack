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

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public Comment addComment(Athlete author, Activity activity, String content, InteractionType type) {
        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setActivity(activity);
        comment.setContent(content);
        comment.setInteractionType(type);
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsForActivity(int activityId) {
        return commentRepository.findByActivityIdOrderByCreatedAtDesc(activityId);
    }

    public void deleteComment(int commentId) {
        commentRepository.deleteById(commentId);
    }
}
