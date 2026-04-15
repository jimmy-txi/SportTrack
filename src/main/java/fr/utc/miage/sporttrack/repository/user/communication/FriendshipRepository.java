package fr.utc.miage.sporttrack.repository.user.communication;

import fr.utc.miage.sporttrack.entity.enumeration.FriendshipStatus;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import fr.utc.miage.sporttrack.entity.user.communication.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {

    /**
     * Checks if a friendship exists between two athletes (bidirectional query).
     * Returns the record if a relationship exists, regardless of who is the initiator or the recipient.
     *
     * @param a The first athlete
     * @param b The second athlete
     * @return The friendship record between the two, if it exists.
     */
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.initiator = :a AND f.recipient = :b) OR " +
            "(f.initiator = :b AND f.recipient = :a)")
    Optional<Friendship> findBetweenAthletes(@Param("a") Athlete a, @Param("b") Athlete b);

    List<Friendship> findByRecipientAndStatus(Athlete recipient, FriendshipStatus status);

    List<Friendship> findByInitiatorAndStatus(Athlete initiator, FriendshipStatus status);

    /**
     * Retrieves all friendships for a given athlete and status.
     * Returns records where the athlete acts as initiator or recipient.
     *
     * @param athleteId Athlete ID
     * @param status    Friendship status
     * @return List of matching friendship records
     */
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.initiator.id = :athleteId OR f.recipient.id = :athleteId) AND " +
            "f.status = :status")
    List<Friendship> findByAthleteAndStatus(@Param("athleteId") Integer athleteId,
                                            @Param("status") FriendshipStatus status);

    /**
     * Checks if blocker has blocked blocked (one-directional).
     *
     * @param blocker the athlete who initiated the block
     * @param blocked the athlete who was blocked
     * @return true if such a BLOCKED relationship exists
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f " +
            "WHERE f.initiator = :blocker AND f.recipient = :blocked AND f.status = 'BLOCKED'")
    boolean existsBlock(@Param("blocker") Athlete blocker, @Param("blocked") Athlete blocked);

    /**
     * Returns the IDs of all users who have blocked the given user.
     */
    @Query("SELECT f.initiator.id FROM Friendship f WHERE f.recipient.id = :userId AND f.status = 'BLOCKED'")
    List<Integer> findBlockedByUserIds(@Param("userId") Integer userId);

    /**
     * Returns the IDs of all users that the given user has blocked.
     */
    @Query("SELECT f.recipient.id FROM Friendship f WHERE f.initiator.id = :userId AND f.status = 'BLOCKED'")
    List<Integer> findBlockedUserIds(@Param("userId") Integer userId);
}
