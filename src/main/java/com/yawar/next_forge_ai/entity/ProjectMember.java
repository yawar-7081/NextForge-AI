package com.yawar.next_forge_ai.entity;

import com.yawar.next_forge_ai.entity.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "project_member_tx",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"project_id","user_id"})
        },
        indexes = @Index(name = "idx_project_member_user_id",columnList = "user_id")
)
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    ProjectRole projectRole;

    @CreationTimestamp
    Instant createdAt;

    @Builder.Default
    boolean isDeleted = false;
}