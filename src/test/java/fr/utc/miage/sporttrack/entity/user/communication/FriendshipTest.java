package fr.utc.miage.sporttrack.entity.user.communication;

import fr.utc.miage.sporttrack.entity.enumeration.FriendshipStatus;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FriendshipTest {

    private static final LocalDateTime NOW = LocalDateTime.now();

    private Athlete createAthlete(Integer id) {
        Athlete a = new Athlete();
        a.setUsername("user" + id);
        a.setPassword("pass" + id);
        a.setEmail("user" + id + "@test.com");
        // Use reflection to set idU since there is no setter for idU
        try {
            var field = a.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(a, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return a;
    }

    // ==================== Constructors ====================

    @Test
    void shouldCreateFriendshipWithNoArgConstructor() {
        Friendship f = new Friendship();
        assertNull(f.getId());
        assertNull(f.getInitiator());
        assertNull(f.getRecipient());
        assertNull(f.getStatus());
        assertNull(f.getCreatedAt());
    }

    @Test
    void shouldCreateFriendshipWithTwoArgConstructor() {
        Athlete initiator = createAthlete(1);
        Athlete recipient = createAthlete(2);

        Friendship f = new Friendship(initiator, recipient);

        assertNull(f.getId());
        assertEquals(initiator, f.getInitiator());
        assertEquals(recipient, f.getRecipient());
        assertEquals(FriendshipStatus.PENDING, f.getStatus());
        assertNotNull(f.getCreatedAt());
    }

    @Test
    void shouldCreateFriendshipWithThreeArgConstructor() {
        Athlete initiator = createAthlete(1);
        Athlete recipient = createAthlete(2);

        Friendship f = new Friendship(initiator, recipient, FriendshipStatus.BLOCKED);

        assertNull(f.getId());
        assertEquals(initiator, f.getInitiator());
        assertEquals(recipient, f.getRecipient());
        assertEquals(FriendshipStatus.BLOCKED, f.getStatus());
        assertNotNull(f.getCreatedAt());
    }

    @Test
    void shouldCreateFriendshipWithThreeArgConstructorAccepted() {
        Athlete initiator = createAthlete(1);
        Athlete recipient = createAthlete(2);

        Friendship f = new Friendship(initiator, recipient, FriendshipStatus.ACCEPTED);

        assertEquals(FriendshipStatus.ACCEPTED, f.getStatus());
    }

    // ==================== Getters and Setters ====================

    @Test
    void shouldGetIdAsNullForNewInstance() {
        Friendship f = new Friendship();
        assertNull(f.getId());
    }

    @Test
    void shouldSetAndGetInitiator() {
        Friendship f = new Friendship();
        Athlete initiator = createAthlete(1);
        f.setInitiator(initiator);
        assertEquals(initiator, f.getInitiator());
    }

    @Test
    void shouldSetAndGetRecipient() {
        Friendship f = new Friendship();
        Athlete recipient = createAthlete(2);
        f.setRecipient(recipient);
        assertEquals(recipient, f.getRecipient());
    }

    @Test
    void shouldSetAndGetStatus() {
        Friendship f = new Friendship();
        f.setStatus(FriendshipStatus.ACCEPTED);
        assertEquals(FriendshipStatus.ACCEPTED, f.getStatus());
    }

    @Test
    void shouldSetAndGetCreatedAt() {
        Friendship f = new Friendship();
        f.setCreatedAt(NOW);
        assertEquals(NOW, f.getCreatedAt());
    }

    // ==================== equals ====================

    @Test
    void shouldBeEqualWithSameReference() {
        Friendship f = new Friendship();
        assertEquals(f, f);
    }

    @Test
    void shouldBeEqualWithSameId() {
        Friendship f1 = new Friendship();
        Friendship f2 = new Friendship();
        // Set id via reflection
        setId(f1, 1);
        setId(f2, 1);
        assertEquals(f1, f2);
    }

    @Test
    void shouldNotBeEqualWithDifferentId() {
        Friendship f1 = new Friendship();
        Friendship f2 = new Friendship();
        setId(f1, 1);
        setId(f2, 2);
        assertNotEquals(f1, f2);
    }

    @Test
    void shouldNotBeEqualToNull() {
        Friendship f = new Friendship();
        assertNotEquals(null, f);
    }

    @Test
    void shouldNotBeEqualToDifferentType() {
        Friendship f = new Friendship();
        assertNotEquals("not a Friendship", f);
    }

    @Test
    void shouldBeEqualWhenBothIdsAreNull() {
        Friendship f1 = new Friendship();
        Friendship f2 = new Friendship();
        assertEquals(f1, f2);
    }

    // ==================== hashCode ====================

    @Test
    void shouldHaveSameHashCodeWithSameId() {
        Friendship f1 = new Friendship();
        Friendship f2 = new Friendship();
        setId(f1, 1);
        setId(f2, 1);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    void shouldHaveConsistentHashCode() {
        Friendship f = new Friendship();
        setId(f, 1);
        int h1 = f.hashCode();
        int h2 = f.hashCode();
        assertEquals(h1, h2);
    }

    // ==================== toString ====================

    @Test
    void shouldToStringWithNullFields() {
        Friendship f = new Friendship();
        String str = f.toString();
        assertNotNull(str);
        assertTrue(str.contains("Friendship{"));
        assertTrue(str.contains("initiator=null"));
        assertTrue(str.contains("recipient=null"));
    }

    @Test
    void shouldToStringWithPopulatedFields() {
        Athlete initiator = createAthlete(1);
        Athlete recipient = createAthlete(2);
        Friendship f = new Friendship(initiator, recipient);
        setId(f, 10);

        String str = f.toString();
        assertTrue(str.contains("id=10"));
        assertTrue(str.contains("initiator=1"));
        assertTrue(str.contains("recipient=2"));
        assertTrue(str.contains("status=PENDING"));
    }

    // ==================== Helper ====================

    private void setId(Friendship f, Integer id) {
        try {
            var field = f.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(f, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
