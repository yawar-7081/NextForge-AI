package com.yawar.next_forge_ai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RemoveProjectMemberRequest {
    @NotNull(message = "'projectMemberId' is required")
    private String projectMemberId;
}
