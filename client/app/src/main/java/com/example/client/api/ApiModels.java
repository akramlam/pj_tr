package com.example.client.api;

import java.util.Set;

public class ApiModels {
    
    // Authentication models
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        
        public RegisterRequest(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }
        
        // Getters
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
    }
    
    public static class LoginRequest {
        private String username;
        private String password;
        
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        // Getters
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
    
    public static class AuthResponse {
        private String token;
        
        // Getters and setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
    
    // User models
    public static class User {
        private Long id;
        private String username;
        private String email;
        private Set<String> roles;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Set<String> getRoles() { return roles; }
        public void setRoles(Set<String> roles) { this.roles = roles; }
    }
    
    // Profile models
    public static class Profile {
        private Long id;
        private String formation;
        private Set<String> skills;
        private String preferences;
        private User user;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFormation() { return formation; }
        public void setFormation(String formation) { this.formation = formation; }
        public Set<String> getSkills() { return skills; }
        public void setSkills(Set<String> skills) { this.skills = skills; }
        public String getPreferences() { return preferences; }
        public void setPreferences(String preferences) { this.preferences = preferences; }
        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }
    }
    
    public static class CreateProfileRequest {
        private String formation;
        private Set<String> skills;
        private String preferences;
        
        public CreateProfileRequest(String formation, Set<String> skills, String preferences) {
            this.formation = formation;
            this.skills = skills;
            this.preferences = preferences;
        }
        
        // Getters
        public String getFormation() { return formation; }
        public Set<String> getSkills() { return skills; }
        public String getPreferences() { return preferences; }
    }
    
    // Message models
    public static class Message {
        private Long id;
        private String sender;
        private String recipient;
        private String content;
        private String timestamp;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
    
    public static class SendMessageRequest {
        private String recipient;
        private String content;
        
        public SendMessageRequest(String recipient, String content) {
            this.recipient = recipient;
            this.content = content;
        }
        
        // Getters
        public String getRecipient() { return recipient; }
        public String getContent() { return content; }
    }
    
    // Match models
    public static class PotentialMatch {
        public Long userId;
        public String username;
        public String formation;
        public Set<String> commonSkills;
        public int compatibilityScore;
        
        // Project-specific fields
        public Long projectId;
        public String projectTitle;
        public String projectDescription;
        public String category;
        public String difficulty;
        public String duration;
        public String createdBy;
        public int teamSize;
        public int currentTeamSize;
        public Set<String> matchingSkills;
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFormation() { return formation; }
        public void setFormation(String formation) { this.formation = formation; }
        public Set<String> getCommonSkills() { return commonSkills; }
        public void setCommonSkills(Set<String> commonSkills) { this.commonSkills = commonSkills; }
        public int getCompatibilityScore() { return compatibilityScore; }
        public void setCompatibilityScore(int compatibilityScore) { this.compatibilityScore = compatibilityScore; }
        
        // Project-specific getters and setters
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getProjectTitle() { return projectTitle; }
        public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }
        public String getProjectDescription() { return projectDescription; }
        public void setProjectDescription(String projectDescription) { this.projectDescription = projectDescription; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public int getTeamSize() { return teamSize; }
        public void setTeamSize(int teamSize) { this.teamSize = teamSize; }
        public int getCurrentTeamSize() { return currentTeamSize; }
        public void setCurrentTeamSize(int currentTeamSize) { this.currentTeamSize = currentTeamSize; }
        public Set<String> getMatchingSkills() { return matchingSkills; }
        public void setMatchingSkills(Set<String> matchingSkills) { this.matchingSkills = matchingSkills; }
    }
    
    public static class MatchRequest {
        public Long targetUserId;
        public Long targetProjectId;  // Add this field for project matching
        public String action; // "LIKE" or "PASS"
        
        // Getters and setters
        public Long getTargetUserId() { return targetUserId; }
        public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
        public Long getTargetProjectId() { return targetProjectId; }
        public void setTargetProjectId(Long targetProjectId) { this.targetProjectId = targetProjectId; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }
    
    public static class MatchResponse {
        public boolean isMatch;
        public String message;
        
        // Getters and setters
        public boolean isMatch() { return isMatch; }
        public void setMatch(boolean match) { isMatch = match; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    // Conversation models
    public static class Conversation {
        public String username;
        public String displayName;
        public String lastMessage;
        public String timestamp;
        public boolean hasUnreadMessages;
        public int unreadCount;
        
        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getLastMessage() { return lastMessage; }
        public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public boolean hasUnreadMessages() { return hasUnreadMessages; }
        public void setHasUnreadMessages(boolean hasUnreadMessages) { this.hasUnreadMessages = hasUnreadMessages; }
        public int getUnreadCount() { return unreadCount; }
        public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    }
} 