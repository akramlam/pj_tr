package com.example.api.service;

import com.example.api.repository.DebugLogRepository;
import com.example.core.domain.DebugLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DebugLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(DebugLogService.class);
    
    @Autowired
    private DebugLogRepository debugLogRepository;
    
    public DebugLog saveDebugLog(DebugLog debugLog) {
        try {
            // Log to console as well for immediate debugging
            logger.info("DEBUG LOG [{}] User: {}, Action: {}, Screen: {}, Details: {}, Error: {}", 
                debugLog.getTimestamp(),
                debugLog.getUserId(),
                debugLog.getAction(),
                debugLog.getScreen(),
                debugLog.getDetails(),
                debugLog.getErrorMessage()
            );
            
            return debugLogRepository.save(debugLog);
        } catch (Exception e) {
            logger.error("Failed to save debug log", e);
            throw e;
        }
    }
    
    public List<DebugLog> getRecentLogs(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return debugLogRepository.findRecentLogs(since);
    }
    
    public List<DebugLog> getLogsByUser(String userId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return debugLogRepository.findRecentLogsByUser(userId, since);
    }
    
    public List<DebugLog> getErrorLogs() {
        return debugLogRepository.findErrorLogs();
    }
    
    public List<DebugLog> getLogsByAction(String action) {
        return debugLogRepository.findByActionOrderByTimestampDesc(action);
    }
    
    public List<DebugLog> getLogsByScreen(String screen) {
        return debugLogRepository.findByScreenOrderByTimestampDesc(screen);
    }
} 