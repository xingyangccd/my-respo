package com.xingyang.chat.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Chat Message Entity
 *
 * @author XingYang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_message")
@Schema(description = "Chat Message Entity")
public class ChatMessage extends BaseEntity {

    @Schema(description = "Conversation ID")
    private Long conversationId;
    
    @Schema(description = "Message role (user, assistant, system)")
    private String role;
    
    @Schema(description = "Message content")
    private String content;
    
    @Schema(description = "Message sequence in conversation")
    private Integer sequence;
    
    @Schema(description = "Token count")
    private Integer tokenCount;
    
    @Schema(description = "Model ID used for this message")
    private String modelId;
} 