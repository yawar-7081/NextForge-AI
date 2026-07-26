package com.yawar.next_forge_ai.service.impl;


import com.yawar.next_forge_ai.dto.StreamResponse;
import com.yawar.next_forge_ai.entity.*;
import com.yawar.next_forge_ai.entity.enums.ChatEventType;
import com.yawar.next_forge_ai.entity.enums.MessageRole;
import com.yawar.next_forge_ai.error.ResourceNotFoundException;
import com.yawar.next_forge_ai.llm.ChatEventParser;
import com.yawar.next_forge_ai.llm.FileTreeContextAdvisor;
import com.yawar.next_forge_ai.llm.CodeGenerationTools;
import com.yawar.next_forge_ai.repository.*;
import com.yawar.next_forge_ai.security.JwtService;
import com.yawar.next_forge_ai.service.AiGenerationService;
import com.yawar.next_forge_ai.service.ProjectFileService;
import com.yawar.next_forge_ai.util.PromptUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiGenerationServiceImpl implements AiGenerationService {

    private final ChatEventRepository chatEventRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final JwtService jwtService;
    private final ChatClient chatClient;
    private final FileTreeContextAdvisor fileTreeContextAdvisor;
    private final ProjectFileService projectFileService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ChatEventParser chatEventParser;

    @Override
    public Flux<StreamResponse> streamResponse(String message, String projectId) {

        String userId = jwtService.getLoggedInUserId();

        CodeGenerationTools codeGenerationTools = new CodeGenerationTools(projectFileService,projectId);

        ChatSession chatSession = createChatSessionIfNotExists(userId,projectId);

        Map<String,Object> advisorParams = Map.of(
                "projectId",projectId,
                "userId",userId
        );

        StringBuilder fullResponseBuffer = new StringBuilder();

        AtomicReference<Long> startTime = new AtomicReference<>(0L);
        AtomicReference<Long> endTime = new AtomicReference<>(0L);

        return chatClient.prompt()
                .system(PromptUtils.CODE_GENERATION_SYSTEM_PROMPT)
                .user(message)
                .advisors(spec -> {
                    spec.params(advisorParams);
                    spec.advisors(
                            fileTreeContextAdvisor);
                })
                .tools(codeGenerationTools)
                .stream()
                .chatResponse()
                .doOnNext(response -> {
                    String content = response.getResult().getOutput().getText();
                    fullResponseBuffer.append(content);

                    if(content != null && !content.isEmpty() && endTime.get()==0){
                        endTime.set(System.currentTimeMillis());
                    }

                })
                .doOnComplete(() -> {
                    Long duration = (endTime.get() - startTime.get()) / 1000;
                    finalizeChats(chatSession,fullResponseBuffer,message,projectId,duration);
                })
                .doOnError(error -> {
                    log.error("Error while generating code - {}",error.toString());
                })
                .map(response -> {
                    String text = response.getResult().getOutput().getText();
                    return new StreamResponse(text != null ? text : "");
                });
    }

    private void finalizeChats(ChatSession chatSession, StringBuilder fullResponseBuffer, String userMessage,String projectId, Long duration) {

        ChatMessage userChatMessage = ChatMessage.builder()
                .chatSession(chatSession)
                .content(userMessage)
                .role(MessageRole.USER)
                .build();

        chatMessageRepository.save(userChatMessage);

        ChatMessage assistantMessage = ChatMessage.builder()
                .chatSession(chatSession)
                .content("Assistant Message")
                .role(MessageRole.ASSISTANT)
                .build();

        assistantMessage = chatMessageRepository.save(assistantMessage);

       List<ChatEvent> chatEvents = chatEventParser.parse(assistantMessage,fullResponseBuffer);

       chatEvents.addFirst(ChatEvent.builder()
                       .chatMessage(assistantMessage)
                       .chatEventType(ChatEventType.THOUGHT)
                       .content("Thought for - "+duration+"s")
                       .sequenceOrder(0)
               .build()
       );

       chatEvents.stream().filter(ce -> ce.getChatEventType()==ChatEventType.FILE_EDIT)
                       .forEach(ce -> projectFileService.saveFile(projectId,ce.getFilePath(),ce.getContent()));

       chatEventRepository.saveAll(chatEvents);
    }

    private ChatSession createChatSessionIfNotExists(String userId, String projectId) {

        ChatSession chatSession = chatSessionRepository.findByProjectIdAndUserIdAndIsDeletedFalse(projectId,userId)
                .orElse(null);

        if(chatSession == null){
            Project project = projectRepository.findAccessibleProject(projectId,userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project",projectId));

            User user = jwtService.extractUser();


            chatSession = ChatSession
                    .builder()
                    .project(project)
                    .user(user)
                    .build();

            chatSession = chatSessionRepository.save(chatSession);
        }

        return chatSession;
    }
}














