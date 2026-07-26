package com.yawar.next_forge_ai.controller;

import com.yawar.next_forge_ai.dto.ChatRequest;
import com.yawar.next_forge_ai.dto.StreamResponse;
import com.yawar.next_forge_ai.service.AiGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AiGenerationService aiGenerationService;

    @PostMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<StreamResponse>> streamChat(@RequestBody ChatRequest request){
        return aiGenerationService.streamResponse(request.getMessage(),request.getProjectId())
                .map(data -> ServerSentEvent.<StreamResponse>builder().data(data).build());
    }

}
