package com.yawar.next_forge_ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectRequest {
    @NotBlank(message = "'projectName' should not be blank or null")
    @Size(min = 5,max = 100, message = "'projectName' character length should be between 5 to 100")
    private String projectName;
}
