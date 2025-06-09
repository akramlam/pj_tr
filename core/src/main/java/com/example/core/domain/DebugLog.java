package com.example.core.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "debug_logs")
public class DebugLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String action;
    
    @Column(length = 1000)
    private String details;
    
    @Column(length = 500)
    private String screen;
    
    @Column(length = 2000)
    private String errorMessage;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 100)
    private String deviceInfo;
    
    @Column(length = 50)
    private String appVersion;
    
    // Constructors
    public DebugLog() {
        this.timestamp = LocalDateTime.now();
    }
    
    public DebugLog(String userId, String action, String details, String screen) {
        this();
        this.userId = userId;
        this.action = action;
        this.details = details;
        this.screen = screen;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public String getScreen() {
        return screen;
    }
    
    public void setScreen(String screen) {
        this.screen = screen;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    
    public String getAppVersion() {
        return appVersion;
    }
    
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
    
    @Override
    public String toString() {
        return "DebugLog{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", action='" + action + '\'' +
                ", screen='" + screen + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
} 