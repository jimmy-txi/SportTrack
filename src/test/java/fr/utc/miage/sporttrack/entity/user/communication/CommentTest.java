package fr.utc.miage.sporttrack.entity.user.communication;

import fr.utc.miage.sporttrack.entity.activity.Activity;
import fr.utc.miage.sporttrack.entity.enumeration.InteractionType;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommentTest {

    @Test
    void testCommentEntity() {
        Comment comment = new Comment();
        
        comment.setId(1);
        Athlete author = new Athlete();
        Activity activity = new Activity();
        
        comment.setAuthor(author);
        comment.setActivity(activity);
        comment.setInteractionType(InteractionType.LIKE);
        comment.setContent("Awesome");
        LocalDateTime now = LocalDateTime.now();
        comment.setCreatedAt(now);

        assertEquals(1, comment.getId());
        assertEquals(author, comment.getAuthor());
        assertEquals(activity, comment.getActivity());
        assertEquals(InteractionType.LIKE, comment.getInteractionType());
        assertEquals("Awesome", comment.getContent());
        assertEquals(now, comment.getCreatedAt());
        
        // Test @PrePersist callback
        comment.onCreate();
        assertNotNull(comment.getCreatedAt());
        assertTrue(comment.getCreatedAt().isAfter(now) || comment.getCreatedAt().isEqual(now));
    }

    @Test
    void testInteractionTypeEnum() {
        InteractionType[] types = InteractionType.values();
        assertTrue(types.length > 0);
        
        assertEquals(InteractionType.LIKE, InteractionType.valueOf("LIKE"));
        assertEquals(InteractionType.CHEER, InteractionType.valueOf("CHEER"));
        assertEquals(InteractionType.AVERAGE, InteractionType.valueOf("AVERAGE"));
        assertEquals(InteractionType.NONE, InteractionType.valueOf("NONE"));
    }
}
