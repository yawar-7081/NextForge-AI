package com.yawar.next_forge_ai.dto;

import com.yawar.next_forge_ai.entity.enums.ProjectMemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
public class AddMemberRequest {
    @NotBlank(message = "'username' cannot be blank or empty")
    private String username;

    @NotNull(message = "'role' is required")
    private ProjectMemberRole role;
}
