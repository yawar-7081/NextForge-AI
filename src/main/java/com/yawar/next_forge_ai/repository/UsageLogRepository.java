package com.yawar.next_forge_ai.repository;

import com.yawar.next_forge_ai.entity.UsageLog;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog,String> {
    Optional<UsageLog> findByUserId(String userId);
}
