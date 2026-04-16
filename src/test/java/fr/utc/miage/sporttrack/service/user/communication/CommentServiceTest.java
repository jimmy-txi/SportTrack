package fr.utc.miage.sporttrack.service.user.communication;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.enumeration.InteractionType;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.user.communication.Comment;
import fr.utc.miage.sporttrack.repository.user.communication.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void shouldAddCommentSuccessfully() {
        Athlete author = new Athlete();
        Activity activity = new Activity();
        String content = "Test comment";
        InteractionType type = InteractionType.LIKE;

        when(commentRepository.save(any(Comment.class))).thenAnswer(i -> {
            Comment c = i.getArgument(0);
            c.setId(1);
            return c;
        });

        Comment savedComment = commentService.addComment(author, activity, content, type);

        assertNotNull(savedComment);
        assertEquals(1, savedComment.getId());

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());

        Comment captured = captor.getValue();
        assertEquals(author, captured.getAuthor());
        assertEquals(activity, captured.getActivity());
        assertEquals(content, captured.getContent());
        assertEquals(type, captured.getInteractionType());
        assertNotNull(captured.getCreatedAt());
    }

    @Test
    void shouldGetCommentsForActivity() {
        int activityId = 100;
        List<Comment> mockComments = new ArrayList<>();
        mockComments.add(new Comment());

        when(commentRepository.findByActivityIdOrderByCreatedAtDesc(activityId)).thenReturn(mockComments);

        List<Comment> result = commentService.getCommentsForActivity(activityId);

        assertEquals(1, result.size());
        verify(commentRepository).findByActivityIdOrderByCreatedAtDesc(activityId);
    }

    @Test
    void shouldDeleteComment() {
        int commentId = 50;

        commentService.deleteComment(commentId);

        verify(commentRepository).deleteById(commentId);
    }
}
