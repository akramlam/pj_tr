package com.example.client.ui.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageStorage {
    private static MessageStorage instance;
    private Map<String, List<ConversationDetailFragment.MessageItem>> conversationMessages;
    
    private MessageStorage() {
        conversationMessages = new HashMap<>();
        initializeDefaultMessages();
    }
    
    public static MessageStorage getInstance() {
        if (instance == null) {
            instance = new MessageStorage();
        }
        return instance;
    }
    
    public void reset() {
        conversationMessages.clear();
        initializeDefaultMessages();
    }
    
    private void initializeDefaultMessages() {
        // Initialize default messages for different conversations
        
        // Student 1 conversation
        List<ConversationDetailFragment.MessageItem> student1Messages = new ArrayList<>();
        student1Messages.add(new ConversationDetailFragment.MessageItem(
            "Hey! I saw we're both studying Computer Science. Want to work on a project together?",
            false, // received message
            "10:30 AM"
        ));
        student1Messages.add(new ConversationDetailFragment.MessageItem(
            "That sounds great! I'm really interested in machine learning projects. What did you have in mind?",
            true, // sent message
            "10:35 AM"
        ));
        student1Messages.add(new ConversationDetailFragment.MessageItem(
            "I was thinking we could build a recommendation system for students to find study groups.",
            false,
            "10:40 AM"
        ));
        student1Messages.add(new ConversationDetailFragment.MessageItem(
            "Perfect! I have some experience with Python and TensorFlow. When can we start?",
            true,
            "10:45 AM"
        ));
        conversationMessages.put("Student 1", student1Messages);
        
        // Student 2 conversation
        List<ConversationDetailFragment.MessageItem> student2Messages = new ArrayList<>();
        student2Messages.add(new ConversationDetailFragment.MessageItem(
            "Thanks for the help with the algorithms assignment! Really appreciate it.",
            false,
            "Yesterday"
        ));
        student2Messages.add(new ConversationDetailFragment.MessageItem(
            "No problem! Happy to help. How did the assignment go?",
            true,
            "Yesterday"
        ));
        conversationMessages.put("Student 2", student2Messages);
        
        // Student 3 conversation
        List<ConversationDetailFragment.MessageItem> student3Messages = new ArrayList<>();
        student3Messages.add(new ConversationDetailFragment.MessageItem(
            "Are you available for the group project meeting tomorrow at 3 PM?",
            false,
            "2 days ago"
        ));
        student3Messages.add(new ConversationDetailFragment.MessageItem(
            "Yes, I'll be there! Should I bring anything specific?",
            true,
            "2 days ago"
        ));
        conversationMessages.put("Student 3", student3Messages);
        
        // Additional students can be added as needed
        List<ConversationDetailFragment.MessageItem> defaultMessages = new ArrayList<>();
        defaultMessages.add(new ConversationDetailFragment.MessageItem(
            "Great match! I'm also interested in machine learning projects. Let's chat!",
            false,
            "5 hours ago"
        ));
        // This will be used for any conversation that doesn't have specific initialization
    }
    
    public List<ConversationDetailFragment.MessageItem> getMessages(String conversationPartner) {
        List<ConversationDetailFragment.MessageItem> messages = conversationMessages.get(conversationPartner);
        if (messages == null) {
            // Create new conversation if it doesn't exist
            messages = new ArrayList<>();
            conversationMessages.put(conversationPartner, messages);
        }
        return messages;
    }
    
    public void addMessage(String conversationPartner, ConversationDetailFragment.MessageItem message) {
        List<ConversationDetailFragment.MessageItem> messages = getMessages(conversationPartner);
        messages.add(message);
    }
    
    public String getLastMessage(String conversationPartner) {
        List<ConversationDetailFragment.MessageItem> messages = getMessages(conversationPartner);
        if (messages.isEmpty()) {
            return "No messages yet";
        }
        ConversationDetailFragment.MessageItem lastMessage = messages.get(messages.size() - 1);
        String content = lastMessage.getContent();
        // Truncate long messages for display in conversation list
        if (content.length() > 50) {
            return content.substring(0, 47) + "...";
        }
        return content;
    }
    
    public String getLastMessageTime(String conversationPartner) {
        List<ConversationDetailFragment.MessageItem> messages = getMessages(conversationPartner);
        if (messages.isEmpty()) {
            return "";
        }
        return messages.get(messages.size() - 1).getTimestamp();
    }
    
    public boolean hasUnreadMessages(String conversationPartner) {
        List<ConversationDetailFragment.MessageItem> messages = getMessages(conversationPartner);
        if (messages.isEmpty()) {
            return false;
        }
        // For now, just return true if the last message was received (not sent by user)
        return !messages.get(messages.size() - 1).isSent();
    }
} 