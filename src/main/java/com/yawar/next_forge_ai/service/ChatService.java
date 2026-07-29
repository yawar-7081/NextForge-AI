package com.yawar.next_forge_ai.service;

import com.yawar.next_forge_ai.dto.ChatResponse;

import java.util.List;

public interface ChatService {
    List<ChatResponse> getProjectChatHistory(String projectId);
}
