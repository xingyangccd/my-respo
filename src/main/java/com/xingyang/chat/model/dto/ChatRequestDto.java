package com.xingyang.chat.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Chat Request Data Transfer Object
 * 
 * @author xingyang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chat Request DTO")
public class ChatRequestDto {
    
    /**
     * Chat message history list
     */
    @Schema(description = "Messages")
    private List<ChatMessageDto> messages;
    
    /**
     * Selected model ID
     */
    @Schema(description = "Model ID")
    private String model;
    
    /**
     * Temperature parameter, controls randomness
     */
    @Schema(description = "Temperature (0.0 to 1.0)")
    private Double temperature;
    
    /**
     * Maximum tokens for generation
     */
    @Schema(description = "Maximum tokens to generate")
    private Integer max_tokens;
    
    /**
     * Enable deep thinking mode
     */
    @Schema(description = "Enable deep thinking mode")
    private Boolean deep_thinking;
    
    @Schema(description = "Conversation ID (if continuing an existing conversation)")
    private Long conversationId;
    
    @Schema(description = "Whether to save this conversation")
    @Builder.Default
    private boolean saveConversation = true;
} 