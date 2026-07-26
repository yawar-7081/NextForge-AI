package com.yawar.next_forge_ai.repository;

import com.yawar.next_forge_ai.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession,String> {

    Optional<ChatSession> findByProjectIdAndUserIdAndIsDeletedFalse(String projectId,String userId);

}
