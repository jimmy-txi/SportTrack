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

/**
 * Spring Data JPA repository for {@link Friendship} entities.
 *
 * <p>Provides standard CRUD operations as well as custom query methods
 * for retrieving, checking, and managing friendship relationships
 * between athletes, including blocking functionality.</p>
 *
 * @author SportTrack Team
 */
@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {

    /**
     * Finds a friendship between two athletes, regardless of direction.
     * Returns the record if a relationship exists in either direction
     * (initiator→recipient or recipient→initiator).
     *
     * @param a the first athlete
     * @param b the second athlete
     * @return an {@link Optional} containing the friendship if found, empty otherwise
     */
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.initiator = :a AND f.recipient = :b) OR " +
            "(f.initiator = :b AND f.recipient = :a)")
    Optional<Friendship> findBetweenAthletes(@Param("a") Athlete a, @Param("b") Athlete b);

    /**
     * Finds all friendships in which the specified athlete is the recipient
     * and the relationship has the given status.
     *
     * @param recipient the athlete who received the friendship request
     * @param status    the status to filter by
     * @return a list of matching friendships
     */
    List<Friendship> findByRecipientAndStatus(Athlete recipient, FriendshipStatus status);

    /**
     * Finds all friendships in which the specified athlete is the initiator
     * and the relationship has the given status.
     *
     * @param initiator the athlete who sent the friendship request
     * @param status    the status to filter by
     * @return a list of matching friendships
     */
    List<Friendship> findByInitiatorAndStatus(Athlete initiator, FriendshipStatus status);

    /**
     * Finds all friendships for a given athlete and status, regardless of direction.
     * Returns records where the athlete acts as either the initiator or the recipient.
     *
     * @param athleteId the unique identifier of the athlete
     * @param status    the friendship status to filter by
     * @return a list of matching friendship records
     */
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.initiator.id = :athleteId OR f.recipient.id = :athleteId) AND " +
            "f.status = :status")
    List<Friendship> findByAthleteAndStatus(@Param("athleteId") Integer athleteId,
                                            @Param("status") FriendshipStatus status);

    /**
     * Checks whether a specific athlete has blocked another athlete (one-directional).
     *
     * @param blocker the athlete who initiated the block
     * @param blocked the athlete who was blocked
     * @return {@code true} if a BLOCKED relationship exists from blocker to blocked
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f " +
            "WHERE f.initiator = :blocker AND f.recipient = :blocked AND f.status = 'BLOCKED'")
    boolean existsBlock(@Param("blocker") Athlete blocker, @Param("blocked") Athlete blocked);

    /**
     * Returns the identifiers of all users who have blocked the specified user.
     *
     * @param userId the unique identifier of the potentially blocked user
     * @return a list of athlete identifiers who have blocked the given user
     */
    @Query("SELECT f.initiator.id FROM Friendship f WHERE f.recipient.id = :userId AND f.status = 'BLOCKED'")
    List<Integer> findBlockedByUserIds(@Param("userId") Integer userId);

    /**
     * Returns the identifiers of all users that the specified user has blocked.
     *
     * @param userId the unique identifier of the blocking user
     * @return a list of athlete identifiers who have been blocked by the given user
     */
    @Query("SELECT f.recipient.id FROM Friendship f WHERE f.initiator.id = :userId AND f.status = 'BLOCKED'")
    List<Integer> findBlockedUserIds(@Param("userId") Integer userId);
}