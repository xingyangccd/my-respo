package com.xingyang.chat.service.impl;

import com.xingyang.chat.config.AiModelConfig;
import com.xingyang.chat.model.dto.ChatMessageDto;
import com.xingyang.chat.model.dto.ChatRequestDto;
import com.xingyang.chat.model.dto.ConversationDto;
import com.xingyang.chat.service.AiChatService;
import com.xingyang.chat.service.ConversationService;
import com.xingyang.chat.service.MessageService;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.model.StreamingResponseHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.Optional;

/**
 * AI Chat Service Implementation
 * 
 * @author xingyang
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    @Autowired
    private AiModelConfig aiModelConfig;
    
    @Autowired
    private ChatLanguageModel chatModel;
    
    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private MessageService messageService;
    
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Send chat request and get reply
     *
     * @param request chat request
     * @return AI response message
     */
    @Override
    public ChatMessageDto chat(ChatRequestDto request) {
        try {
            // Convert messages to LangChain4j format
            List<ChatMessage> messages = convertToChatMessages(request.getMessages());
            
            // Get AI response
            Response<AiMessage> response = chatModel.generate(messages);
            AiMessage aiMessage = response.content();
            
            // Build and return response message
            return ChatMessageDto.assistantMessage(aiMessage.text());
        } catch (Exception e) {
            log.error("AI chat error", e);
            return ChatMessageDto.assistantMessage("Sorry, I encountered an issue and cannot answer your question. Error: " + e.getMessage());
        }
    }
    
    /**
     * Send chat request and get streaming reply
     *
     * @param request chat request
     * @param responseConsumer response consumer for handling streaming response
     */
    @Override
    public void streamChat(ChatRequestDto request, Consumer<String> responseConsumer) {
        try {
            // First check if we have this question in cache
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                ChatMessageDto lastUserMessage = request.getMessages().stream()
                        .filter(msg -> "user".equals(msg.getRole()))
                        .reduce((first, second) -> second)
                        .orElse(null);
                
                if (lastUserMessage != null) {
                    // Try to find a cached response
                    Optional<String> cachedResponse = messageService.findCachedResponse(lastUserMessage.getContent());
                    if (cachedResponse.isPresent()) {
                        log.info("Found cached response for question: {}", lastUserMessage.getContent());
                        
                        // Return the cached response immediately
                        responseConsumer.accept(cachedResponse.get());
                        
                        // Save to conversation if needed
                        if (request.getConversationId() != null) {
                            saveConversation(request, cachedResponse.get());
                        } else if (request.isSaveConversation()) {
                            createAndSaveConversation(request, cachedResponse.get());
                        }
                        
                        return;
                    }
                }
            }
            
            // If no cached response, proceed with normal API call
            // For all models, use direct API call approach rather than using LangChain4j
            directApiStreamChat(request, responseConsumer);
            
        } catch (Exception e) {
            log.error("Stream chat error", e);
            responseConsumer.accept("\n\nSorry, I encountered an issue and cannot answer your question. Error: " + e.getMessage());
        }
    }
    
    /**
     * Direct API call to the Alibaba Cloud model without using LangChain4j
     * This is a more reliable approach to avoid compatibility issues
     */
    private void directApiStreamChat(ChatRequestDto request, Consumer<String> responseConsumer) {
        try {
            log.info("Using direct API call for chat request");
            
            // Create request payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", aiModelConfig.getModelId());
            
            // Convert DTO messages to OpenAI format
            List<Map<String, String>> messages = new ArrayList<>();
            for (ChatMessageDto dto : request.getMessages()) {
                Map<String, String> message = new HashMap<>();
                message.put("role", dto.getRole());
                message.put("content", dto.getContent());
                messages.add(message);
            }
            payload.put("messages", messages);
            
            // Add other parameters
            if (request.getTemperature() != null) {
                payload.put("temperature", request.getTemperature());
            } else {
                payload.put("temperature", aiModelConfig.getTemperature());
            }
            
            if (request.getMax_tokens() != null) {
                payload.put("max_tokens", request.getMax_tokens());
            } else {
                payload.put("max_tokens", aiModelConfig.getMaxTokens());
            }
            
            // Make direct API call to Alibaba Cloud
            String apiUrl = aiModelConfig.getEndpoint() + "/chat/completions";
            log.info("Sending direct API request to: {}", apiUrl);
            
            try {
                // Setup headers with proper authorization
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("Authorization", "Bearer " + aiModelConfig.getApiKey());
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                
                // Create entity with headers and body
                org.springframework.http.HttpEntity<Map<String, Object>> requestEntity = 
                    new org.springframework.http.HttpEntity<>(payload, headers);
                
                // Call API with proper headers
                org.springframework.http.ResponseEntity<Map> responseEntity = restTemplate.postForEntity(
                    apiUrl,
                    requestEntity,
                    Map.class
                );
                
                Map<String, Object> response = responseEntity.getBody();
                
                if (response != null && response.containsKey("choices")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    if (!choices.isEmpty()) {
                        Map<String, Object> choice = choices.get(0);
                        if (choice.containsKey("message")) {
                            @SuppressWarnings("unchecked")
                            Map<String, String> message = (Map<String, String>) choice.get("message");
                            String content = message.get("content");
                            
                            // 发送完整内容给客户端
                            log.info("Sending complete response to client, length: {}", content.length());
                            responseConsumer.accept(content);
                            
                            // Add a small delay to ensure client has time to process
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            
                            // Cache the question and response
                            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                                ChatMessageDto lastUserMessage = request.getMessages().stream()
                                        .filter(msg -> "user".equals(msg.getRole()))
                                        .reduce((first, second) -> second)
                                        .orElse(null);
                                
                                if (lastUserMessage != null) {
                                    messageService.cacheQuestionResponse(lastUserMessage.getContent(), content, 5);
                                }
                            }
                            
                            // Save the conversation if conversationId is provided
                            if (request.getConversationId() != null) {
                                saveConversation(request, content);
                            } else {
                                // Create a new conversation if requested
                                if (request.isSaveConversation()) {
                                    Long newConversationId = createAndSaveConversation(request, content);
                                    
                                    // 返回创建的会话ID给客户端
                                    if (newConversationId != null) {
                                        log.info("Created new conversation with ID: {}", newConversationId);
                                    }
                                }
                            }
                            
                            return;
                        }
                    }
                }
                
                // If we can't extract the message, send an error
                responseConsumer.accept("Sorry, could not extract response from API");
                
            } catch (Exception e) {
                log.error("Error in direct API call", e);
                responseConsumer.accept("\n\nError calling AI API: " + e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Error in direct API streaming", e);
            responseConsumer.accept("\n\nSorry, an error occurred: " + e.getMessage());
        }
    }
    
    /**
     * Save conversation to database
     */
    private void saveConversation(ChatRequestDto request, String response) {
        try {
            conversationService.saveChat(request.getConversationId(), request, response);
            log.info("Saved chat to conversation ID: {}", request.getConversationId());
        } catch (Exception e) {
            log.error("Failed to save conversation", e);
        }
    }
    
    /**
     * Create a new conversation and save it
     */
    private Long createAndSaveConversation(ChatRequestDto request, String response) {
        try {
            // Get first user message for title
            String title = "New Conversation";
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                ChatMessageDto firstMessage = request.getMessages().get(0);
                if ("user".equals(firstMessage.getRole())) {
                    // Use first 30 chars of first message as title
                    String content = firstMessage.getContent();
                    title = content.length() > 30 ? content.substring(0, 30) + "..." : content;
                }
            }
            
            // Create conversation
            ConversationDto conversation = conversationService.createConversation(title, request.getModel());
            
            // Save messages
            if (conversation != null && conversation.getId() != null) {
                request.setConversationId(conversation.getId());
                saveConversation(request, response);
                log.info("Created and saved new conversation with ID: {}", conversation.getId());
                return conversation.getId();
            }
        } catch (Exception e) {
            log.error("Failed to create and save conversation", e);
        }
        return null;
    }
    
    /**
     * Switch chat model
     *
     * @param modelId model ID
     * @return whether switch was successful
     */
    @Override
    public boolean switchModel(String modelId) {
        try {
            // Update model ID in config
            aiModelConfig.setModelId(modelId);
            log.info("Switched AI model to: {}", modelId);
            return true;
        } catch (Exception e) {
            log.error("Failed to switch AI model", e);
            return false;
        }
    }
    
    /**
     * Convert DTO message list to LangChain4j message list
     *
     * @param dtoMessages DTO message list
     * @return LangChain4j message list
     */
    private List<ChatMessage> convertToChatMessages(List<ChatMessageDto> dtoMessages) {
        List<ChatMessage> langChainMessages = new ArrayList<>();
        
        if (dtoMessages != null) {
            for (ChatMessageDto dto : dtoMessages) {
                switch (dto.getRole()) {
                    case "system":
                        langChainMessages.add(new SystemMessage(dto.getContent()));
                        break;
                    case "user":
                        langChainMessages.add(new UserMessage(dto.getContent()));
                        break;
                    case "assistant":
                        langChainMessages.add(new AiMessage(dto.getContent()));
                        break;
                    default:
                        log.warn("Unknown message role: {}", dto.getRole());
                }
            }
        }
        
        // If no system message, add a default one
        if (langChainMessages.stream().noneMatch(m -> m instanceof SystemMessage)) {
            langChainMessages.add(0, new SystemMessage(
                    "You are Qwen, a large language model developed by Alibaba Cloud. You can provide information, answer questions, " +
                    "create content, and assist users with various tasks. Please be friendly, professional, and helpful. " +
                    "If you're not sure about an answer, be honest and say so instead of making up information."));
        }
        
        return langChainMessages;
    }
} 