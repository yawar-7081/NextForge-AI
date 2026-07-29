package com.yawar.next_forge_ai.controller;

import com.yawar.next_forge_ai.dto.ChatRequest;
import com.yawar.next_forge_ai.dto.ChatResponse;
import com.yawar.next_forge_ai.dto.StreamResponse;
import com.yawar.next_forge_ai.service.AiGenerationService;
import com.yawar.next_forge_ai.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;


@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AiGenerationService aiGenerationService;
    private final ChatService chatService;

    @PostMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<StreamResponse>> streamChat(@RequestBody ChatRequest request){
        return aiGenerationService.streamResponse(request.getMessage(),request.getProjectId())
                .map(data -> ServerSentEvent.<StreamResponse>builder().data(data).build());
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<List<ChatResponse>> getChatHistory(
            @PathVariable String projectId) {
        return ResponseEntity.ok(chatService.getProjectChatHistory(projectId));
    }

}
