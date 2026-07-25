package com.yawar.next_forge_ai.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProjectFileContentResponse {
    private String path;
    private String content;
}
