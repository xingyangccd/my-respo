package com.xingyang.chat.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Conversation DTO
 *
 * @author XingYang
 */
@Data
@Schema(description = "Conversation DTO")
public class ConversationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Conversation ID")
    private Long id;

    @Schema(description = "Conversation title")
    private String title;
    
    @Schema(description = "Model ID")
    private String modelId;
    
    @Schema(description = "Model configuration (JSON format)")
    private String modelConfig;
    
    @Schema(description = "Conversation status (0: archived, 1: active)")
    private Integer status;
    
    @Schema(description = "Creation time")
    private LocalDateTime createTime;
    
    @Schema(description = "Last update time")
    private LocalDateTime updateTime;
    
    @Schema(description = "Messages in the conversation")
    private List<ChatMessageDto> messages;
} 