package com.yawar.next_forge_ai.service.impl;

import com.yawar.next_forge_ai.dto.ChatEventResponse;
import com.yawar.next_forge_ai.dto.ChatResponse;
import com.yawar.next_forge_ai.entity.ChatEvent;
import com.yawar.next_forge_ai.entity.ChatMessage;
import com.yawar.next_forge_ai.entity.enums.MessageRole;
import com.yawar.next_forge_ai.repository.ChatEventRepository;
import com.yawar.next_forge_ai.repository.ChatMessageRepository;
import com.yawar.next_forge_ai.repository.ChatSessionRepository;
import com.yawar.next_forge_ai.security.JwtService;
import com.yawar.next_forge_ai.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatEventRepository chatEventRepository;

    private final JwtService jwtService;

    @Override
    public List<ChatResponse> getProjectChatHistory(String projectId) {
        String userId = jwtService.getLoggedInUserId();

        List<ChatResponse> chatResponses = new ArrayList<>();

        List<ChatMessage> chatMessage = chatMessageRepository.findByProjectIdAndUserId(projectId,userId);

        for(ChatMessage cm : chatMessage){
            List<ChatEventResponse> chatEventResponses = new ArrayList<>();
            if(cm.getRole()== MessageRole.ASSISTANT){
                List<ChatEvent> chatEvents = chatEventRepository.findByChatMessageId(cm.getId());

                chatEventResponses = chatEvents.stream().map(
                        ce -> ChatEventResponse.builder()
                                .id(ce.getId())
                                .type(ce.getChatEventType())
                                .content(ce.getContent())
                                .filePath(ce.getFilePath())
                                .metadata(ce.getMetadata())
                                .sequenceOrder(ce.getSequenceOrder())
                                .build()
                ).toList();

            }
            chatResponses.add(
                    ChatResponse.builder()
                            .id(cm.getId())
                            .events(chatEventResponses)
                            .role(cm.getRole())
                            .tokenUsed(cm.getTokenUsed())
                            .createdAt(cm.getCreatedAt())
                            .content(cm.getContent())
                            .build()
            );
        }

        return chatResponses;
    }
}
