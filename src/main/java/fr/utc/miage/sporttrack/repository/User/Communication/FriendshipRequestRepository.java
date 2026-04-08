package fr.utc.miage.sporttrack.repository.User.Communication;

import fr.utc.miage.sporttrack.entity.User.Communication.FriendshipRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRequestRepository extends JpaRepository<FriendshipRequest, Integer> {
}
