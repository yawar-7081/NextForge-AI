package com.yawar.next_forge_ai.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectResponse {
    private String id;
    private String name;
    private boolean isPublic;
    private String createdAt;
}
