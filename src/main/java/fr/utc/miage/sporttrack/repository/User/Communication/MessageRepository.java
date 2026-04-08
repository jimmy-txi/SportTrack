package fr.utc.miage.sporttrack.repository.User.Communication;

import fr.utc.miage.sporttrack.entity.User.Communication.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
}
