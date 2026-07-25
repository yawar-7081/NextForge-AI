package com.yawar.next_forge_ai.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AuthResponse {
    private String userId;
    private String name;
    private String email;
    private String username;
    private String token;
}
