package com.yawar.next_forge_ai.repository;

import com.yawar.next_forge_ai.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken,String> {
    Optional<EmailVerificationToken> findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(String email);
}
