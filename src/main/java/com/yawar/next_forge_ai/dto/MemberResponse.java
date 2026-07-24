package com.yawar.next_forge_ai.dto;

import com.yawar.next_forge_ai.entity.ProjectMember;
import com.yawar.next_forge_ai.entity.enums.ProjectMemberRole;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResponse {
    private String id;
    private String username;
    private String email;
    private String name;
    private ProjectMemberRole projectMemberRole;
    private Instant createdAt;
}
