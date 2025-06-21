package com.example.api.controller;

import com.example.api.service.MessageService;
import com.example.api.service.MessageService.MessageDto;
import com.example.api.service.MessageService.ConversationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(@RequestBody SendRequest request,
                                                   Principal principal) {
        MessageDto dto = messageService.send(
                principal.getName(),
                request.getRecipient(),
                request.getContent()
        );
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<MessageDto>> getConversation(
            @RequestParam("user") String otherUser,
            Principal principal) {
        List<MessageDto> msgs = messageService.getConversation(
                principal.getName(), otherUser
        );
        return ResponseEntity.ok(msgs);
    }
    
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> getConversations(Principal principal) {
        List<ConversationDto> conversations = messageService.getConversationsForUser(
                principal.getName()
        );
        return ResponseEntity.ok(conversations);
    }

    public static class SendRequest {
        private String recipient;
        private String content;

        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}