package com.yawar.next_forge_ai.dto;

import com.yawar.next_forge_ai.entity.enums.ProjectRole;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectSummaryResponse {
    private String id;
    private String name;
    private ProjectRole projectRole;
    private Instant createdAt;
    private Instant updatedAt;
}
