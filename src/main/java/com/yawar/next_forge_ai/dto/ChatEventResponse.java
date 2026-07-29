package com.yawar.next_forge_ai.dto;

import com.yawar.next_forge_ai.entity.enums.ChatEventType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ChatEventResponse {
    private String id;
    private ChatEventType type;
    private Integer sequenceOrder;
    private String metadata;
    private String filePath;
    private String content;
}
