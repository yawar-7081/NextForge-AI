package com.yawar.next_forge_ai.repository;


import com.yawar.next_forge_ai.entity.ChatEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatEventRepository extends JpaRepository<ChatEvent,String> {
    List<ChatEvent> findByChatMessageId(String id);
}
