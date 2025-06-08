package com.xingyang.chat.controller;

import com.xingyang.chat.model.dto.ChatMessageDto;
import com.xingyang.chat.model.dto.ChatRequestDto;
import com.xingyang.chat.model.vo.Result;
import com.xingyang.chat.service.AiChatService;
import com.xingyang.chat.service.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Chat Controller
 * 
 * @author xingyang
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@Tag(name = "Chat API", description = "Chat with AI models")
public class ChatController {

    @Autowired
    private AiChatService aiChatService;
    
    @Autowired
    private MessageService messageService;

    /**
     * Chat with AI (non-streaming)
     *
     * @param request chat request
     * @return AI response
     */
    @PostMapping
    @Operation(summary = "Chat with AI", description = "Send a chat request and get a response")
    public Result<ChatMessageDto> chat(@RequestBody ChatRequestDto request) {
        log.info("Received chat request, model: {}, messages count: {}", 
                request.getModel(), request.getMessages() != null ? request.getMessages().size() : 0);
        try {
            // Check for cached response first if it's a user query
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                ChatMessageDto lastUserMessage = request.getMessages().stream()
                        .filter(msg -> "user".equals(msg.getRole()))
                        .reduce((first, second) -> second)
                        .orElse(null);
                
                if (lastUserMessage != null) {
                    Optional<String> cachedResponse = messageService.findCachedResponse(lastUserMessage.getContent());
                    if (cachedResponse.isPresent()) {
                        log.info("Using cached response for question: {}", lastUserMessage.getContent());
                        
                        // Convert to DTO and return
                        ChatMessageDto response = ChatMessageDto.assistantMessage(cachedResponse.get());
                        return Result.success(response);
                    }
                }
            }
            
            // No cached response, use regular API
            ChatMessageDto response = aiChatService.chat(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("Error in chat controller", e);
            return Result.error(500, "Chat processing error: " + e.getMessage());
        }
    }

    /**
     * Stream chat with AI using Reactive Streams
     *
     * @param request chat request
     * @return Reactive stream of tokens
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream chat with AI", description = "Send a chat request and get a streaming response")
    public Flux<String> streamChat(@RequestBody ChatRequestDto request) {
        log.info("Received chat request for stream API, model: {}, messages count: {}, conversation ID: {}", 
                request.getModel(), request.getMessages() != null ? request.getMessages().size() : 0, request.getConversationId());
        
        // Check for cached response first if it's a simple user query
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            ChatMessageDto lastUserMessage = request.getMessages().stream()
                    .filter(msg -> "user".equals(msg.getRole()))
                    .reduce((first, second) -> second)
                    .orElse(null);
            
            if (lastUserMessage != null) {
                Optional<String> cachedResponse = messageService.findCachedResponse(lastUserMessage.getContent());
                if (cachedResponse.isPresent()) {
                    log.info("Using cached response for question: {}", lastUserMessage.getContent());
                    
                    // 如果有会话ID，保存消息到数据库
                    if (request.getConversationId() != null) {
                        try {
                            // 保存用户问题
                            log.info("Saving user message to conversation: {}", request.getConversationId());
                            messageService.addMessage(request.getConversationId(), "user", lastUserMessage.getContent());
                            
                            // 保存缓存回复
                            log.info("Saving cached AI response to conversation: {}", request.getConversationId());
                            messageService.addMessage(request.getConversationId(), "assistant", cachedResponse.get());
                        } catch (Exception e) {
                            log.error("Error saving message to database", e);
                        }
                    }
                    
                    return Flux.just(cachedResponse.get());
                }
            }
        }
        
        // No cached response, use streaming API
        return Flux.create(sink -> {
            try {
                // Create a collector for tokens
                StringBuilder fullResponse = new StringBuilder();
                
                // Start streaming the response
                aiChatService.streamChat(request, token -> {
                    // Add token to response and emit it
                    fullResponse.append(token);
                    sink.next(token);
                });
                
                // 完成后，如果有会话ID，保存消息到数据库
                if (request.getConversationId() != null && request.getMessages() != null && !request.getMessages().isEmpty()) {
                    try {
                        // 获取最后一条用户消息
                        ChatMessageDto lastUserMessage = request.getMessages().stream()
                                .filter(msg -> "user".equals(msg.getRole()))
                                .reduce((first, second) -> second)
                                .orElse(null);
                        
                        if (lastUserMessage != null) {
                            // 保存用户问题
                            log.info("Saving user message to conversation: {}", request.getConversationId());
                            messageService.addMessage(request.getConversationId(), "user", lastUserMessage.getContent());
                            
                            // 保存AI回复
                            log.info("Saving AI response to conversation: {}", request.getConversationId());
                            messageService.addMessage(request.getConversationId(), "assistant", fullResponse.toString());
                        }
                    } catch (Exception e) {
                        log.error("Error saving message to database", e);
                    }
                }
                
                // When complete, signal completion
                sink.complete();
            } catch (Exception e) {
                log.error("Error in stream chat controller", e);
                sink.error(e);
            }
        });
    }
    
    /**
     * Switch the AI model
     *
     * @param modelId ID of the model to switch to
     * @return success or error response
     */
    @PostMapping("/model/{modelId}")
    @Operation(summary = "Switch AI model", description = "Change the current AI model")
    public Result<Boolean> switchModel(
            @Parameter(description = "ID of the model to switch to") @PathVariable String modelId) {
        log.info("Switching AI model to: {}", modelId);
        try {
            boolean success = aiChatService.switchModel(modelId);
            return Result.success(success);
        } catch (Exception e) {
            log.error("Error switching model", e);
            return Result.error(500, "Error switching model: " + e.getMessage());
        }
    }
} 