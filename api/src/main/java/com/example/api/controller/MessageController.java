package com.example.api.controller;

import com.example.api.service.MessageService;
import com.example.api.service.MessageService.MessageDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Get all conversations for the authenticated user
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> getConversations(Principal principal) {
        // For now, return mock conversations
        // In a real implementation, you'd fetch from database
        List<ConversationDto> conversations = createMockConversations(principal.getName());
        return ResponseEntity.ok(conversations);
    }

    /**
     * Get messages in a specific conversation
     */
    @GetMapping("/conversation/{otherUsername}")
    public ResponseEntity<List<MessageDto>> getConversationMessages(
            @PathVariable String otherUsername,
            Principal principal) {
        
        List<MessageDto> messages = messageService.getConversation(principal.getName(), otherUsername);
        return ResponseEntity.ok(messages);
    }

    /**
     * Send a message to another user
     */
    @PostMapping("/send")
    public ResponseEntity<MessageDto> sendMessage(
            @RequestBody SendMessageRequest request,
            Principal principal) {
        
        MessageDto message = messageService.send(
            principal.getName(), 
            request.recipientUsername, 
            request.content
        );
        
        return ResponseEntity.ok(message);
    }

    /**
     * Mark conversation as read
     */
    @PostMapping("/conversation/{otherUsername}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String otherUsername,
            Principal principal) {
        
        // Implementation would mark all messages in conversation as read
        return ResponseEntity.ok().build();
    }

    private List<ConversationDto> createMockConversations(String currentUser) {
        List<ConversationDto> conversations = new ArrayList<>();
        
        // Add some sample conversations based on potential matches
        conversations.add(new ConversationDto(
            "alice_johnson",
            "Alice Johnson",
            "Hey! I saw we're both studying Computer Science. Want to work on a project together?",
            LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ofPattern("HH:mm")),
            true,
            2
        ));
        
        conversations.add(new ConversationDto(
            "bob_smith",
            "Bob Smith", 
            "Thanks for the help with the algorithms assignment! Really appreciate it.",
            LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("MMM dd")),
            false,
            0
        ));
        
        conversations.add(new ConversationDto(
            "carol_davis",
            "Carol Davis",
            "Are you available for the group project meeting tomorrow at 3 PM?",
            LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ofPattern("MMM dd")),
            false,
            0
        ));
        
        conversations.add(new ConversationDto(
            "david_wilson",
            "David Wilson",
            "Great match! I'm also interested in machine learning projects. Let's chat!",
            LocalDateTime.now().minusHours(5).format(DateTimeFormatter.ofPattern("HH:mm")),
            true,
            1
        ));
        
        return conversations;
    }

    // DTOs
    public static class ConversationDto {
        public String username;
        public String displayName;
        public String lastMessage;
        public String timestamp;
        public boolean hasUnreadMessages;
        public int unreadCount;

        public ConversationDto(String username, String displayName, String lastMessage, 
                              String timestamp, boolean hasUnreadMessages, int unreadCount) {
            this.username = username;
            this.displayName = displayName;
            this.lastMessage = lastMessage;
            this.timestamp = timestamp;
            this.hasUnreadMessages = hasUnreadMessages;
            this.unreadCount = unreadCount;
        }
    }

    public static class SendMessageRequest {
        public String recipientUsername;
        public String content;
    }
}