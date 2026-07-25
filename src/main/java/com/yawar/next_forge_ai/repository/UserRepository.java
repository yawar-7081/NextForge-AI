package com.yawar.next_forge_ai.repository;

import com.yawar.next_forge_ai.entity.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {

    @Query("""
        SELECT u FROM User u
        WHERE u.email = :email
        AND u.isDeleted = false
        AND u.isActive = true
    """)
    Optional<User> findByActiveEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("""
        SELECT u FROM User u
        WHERE u.username = :username
        AND u.isDeleted = false
        AND u.isActive = true
    """)
    Optional<User> findByUsername(
            @NotBlank(message = "'username' cannot be blank or empty in repository call")
            @Param("username") String username
    );

    @Query("""
    SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END 
    FROM User u
    WHERE u.username = :username
""")
    boolean existsByUsername(
            @Param("username") String username
    );

    @Query("""
        SELECT u FROM User u 
        WHERE (u.email = :identifier OR u.username = :identifier) 
        AND u.isActive = true 
        AND u.isDeleted = false
    """)
    Optional<User> findActiveOrUndeletedUserByIdentifier(@Param("identifier") String identifier);

}
