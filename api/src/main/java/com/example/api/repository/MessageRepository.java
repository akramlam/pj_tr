package com.example.api.repository;

import com.example.core.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m " +
           "WHERE (m.sender.username = :u1 AND m.recipient.username = :u2) " +
           "OR (m.sender.username = :u2 AND m.recipient.username = :u1) " +
           "ORDER BY m.timestamp")
    List<Message> findConversation(@Param("u1") String user1, @Param("u2") String user2);
}