package com.yawar.next_forge_ai.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class ChatRequest {
    private String message;
    private String projectId;
}
