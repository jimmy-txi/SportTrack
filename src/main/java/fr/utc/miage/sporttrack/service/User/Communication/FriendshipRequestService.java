package fr.utc.miage.sporttrack.service.User.Communication;

import fr.utc.miage.sporttrack.entity.User.Communication.FriendshipRequest;
import fr.utc.miage.sporttrack.repository.User.Communication.FriendshipRequestRepository;
import org.springframework.stereotype.Service;

@Service
public class FriendshipRequestService {

    private final FriendshipRequestRepository friendshipRequestRepository;

    public FriendshipRequestService(FriendshipRequestRepository friendshipRequestRepository) {
        this.friendshipRequestRepository = friendshipRequestRepository;
    }
}
