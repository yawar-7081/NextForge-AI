package com.yawar.next_forge_ai.service;


import com.yawar.next_forge_ai.dto.StreamResponse;
import reactor.core.publisher.Flux;

import java.util.Optional;

public interface AiGenerationService {

    Flux<StreamResponse> streamResponse(String message, String projectId);
}
