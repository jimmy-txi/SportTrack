package fr.utc.miage.sporttrack.service.User.Communication;

import fr.utc.miage.sporttrack.entity.User.Communication.Message;
import fr.utc.miage.sporttrack.repository.User.Communication.MessageRepository;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }
}
