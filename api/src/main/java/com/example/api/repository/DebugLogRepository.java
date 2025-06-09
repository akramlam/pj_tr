package com.example.api.repository;

import com.example.core.domain.DebugLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DebugLogRepository extends JpaRepository<DebugLog, Long> {
    
    List<DebugLog> findByUserIdOrderByTimestampDesc(String userId);
    
    List<DebugLog> findByActionOrderByTimestampDesc(String action);
    
    List<DebugLog> findByScreenOrderByTimestampDesc(String screen);
    
    @Query("SELECT d FROM DebugLog d WHERE d.timestamp >= :since ORDER BY d.timestamp DESC")
    List<DebugLog> findRecentLogs(@Param("since") LocalDateTime since);
    
    @Query("SELECT d FROM DebugLog d WHERE d.userId = :userId AND d.timestamp >= :since ORDER BY d.timestamp DESC")
    List<DebugLog> findRecentLogsByUser(@Param("userId") String userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT d FROM DebugLog d WHERE d.errorMessage IS NOT NULL ORDER BY d.timestamp DESC")
    List<DebugLog> findErrorLogs();
} 