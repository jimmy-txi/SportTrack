package fr.utc.miage.sporttrack.repository.User.Communication;

import fr.utc.miage.sporttrack.entity.Enumeration.FriendshipStatus;
import fr.utc.miage.sporttrack.entity.User.Athlete;
import fr.utc.miage.sporttrack.entity.User.Communication.Friendship;
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
            "(f.initiator.idU = :athleteId OR f.recipient.idU = :athleteId) AND " +
            "f.status = :status")
    List<Friendship> findByAthleteAndStatus(@Param("athleteId") Integer athleteId,
                                            @Param("status") FriendshipStatus status);
}
