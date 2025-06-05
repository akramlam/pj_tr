package com.example.api.service;

import com.example.api.repository.MessageRepository;
import com.example.api.repository.UserRepository;
import com.example.core.domain.Message;
import com.example.core.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository,
                          UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MessageDto send(String senderUsername, String recipientUsername, String content) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User recipient = userRepository.findByUsername(recipientUsername)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        Message msg = new Message();
        msg.setSender(sender);
        msg.setRecipient(recipient);
        msg.setContent(content);
        msg.setTimestamp(Instant.now());
        msg = messageRepository.save(msg);
        return toDto(msg);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getConversation(String user1, String user2) {
        return messageRepository.findConversation(user1, user2)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private MessageDto toDto(Message msg) {
        return new MessageDto(
                msg.getId(),
                msg.getSender().getUsername(),
                msg.getRecipient().getUsername(),
                msg.getContent(),
                msg.getTimestamp()
        );
    }

    public static class MessageDto {
        private Long id;
        private String sender;
        private String recipient;
        private String content;
        private Instant timestamp;

        public MessageDto(Long id, String sender, String recipient, String content, Instant timestamp) {
            this.id = id;
            this.sender = sender;
            this.recipient = recipient;
            this.content = content;
            this.timestamp = timestamp;
        }

        public Long getId() { return id; }
        public String getSender() { return sender; }
        public String getRecipient() { return recipient; }
        public String getContent() { return content; }
        public Instant getTimestamp() { return timestamp; }
    }
}