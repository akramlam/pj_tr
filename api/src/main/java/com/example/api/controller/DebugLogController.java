package com.example.api.controller;

import com.example.api.service.DebugLogService;
import com.example.core.domain.DebugLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class DebugLogController {
    
    @Autowired
    private DebugLogService debugLogService;
    
    @PostMapping("/log")
    public ResponseEntity<String> logDebugInfo(@RequestBody DebugLog debugLog) {
        try {
            debugLogService.saveDebugLog(debugLog);
            return ResponseEntity.ok("Debug log saved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to save debug log: " + e.getMessage());
        }
    }
    
    @GetMapping("/logs/recent")
    public ResponseEntity<List<DebugLog>> getRecentLogs(@RequestParam(defaultValue = "1") int hours) {
        try {
            List<DebugLog> logs = debugLogService.getRecentLogs(hours);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/logs/user/{userId}")
    public ResponseEntity<List<DebugLog>> getLogsByUser(
            @PathVariable String userId, 
            @RequestParam(defaultValue = "24") int hours) {
        try {
            List<DebugLog> logs = debugLogService.getLogsByUser(userId, hours);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/logs/errors")
    public ResponseEntity<List<DebugLog>> getErrorLogs() {
        try {
            List<DebugLog> logs = debugLogService.getErrorLogs();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/logs/action/{action}")
    public ResponseEntity<List<DebugLog>> getLogsByAction(@PathVariable String action) {
        try {
            List<DebugLog> logs = debugLogService.getLogsByAction(action);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/logs/screen/{screen}")
    public ResponseEntity<List<DebugLog>> getLogsByScreen(@PathVariable String screen) {
        try {
            List<DebugLog> logs = debugLogService.getLogsByScreen(screen);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Simple endpoint to test connectivity
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Debug logging service is active");
    }
} 