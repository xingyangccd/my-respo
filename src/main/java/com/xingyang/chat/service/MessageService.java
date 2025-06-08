package com.xingyang.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xingyang.chat.model.dto.ChatMessageDto;
import com.xingyang.chat.model.entity.Message;

import java.util.List;
import java.util.Optional;

/**
 * Message Service Interface
 *
 * @author XingYang
 */
public interface MessageService extends IService<Message> {

    /**
     * Add message to conversation
     *
     * @param conversationId Conversation ID
     * @param role Message role (user, assistant, system)
     * @param content Message content
     * @return Chat message DTO
     */
    ChatMessageDto addMessage(Long conversationId, String role, String content);
    
    /**
     * Get messages by conversation ID
     *
     * @param conversationId Conversation ID
     * @return List of chat message DTOs
     */
    List<ChatMessageDto> getMessagesByConversationId(Long conversationId);
    
    /**
     * Find cached response for a similar question
     *
     * @param question Question content
     * @return Optional containing a response if found, empty otherwise
     */
    Optional<String> findCachedResponse(String question);
    
    /**
     * Cache a question and its response
     *
     * @param question Question content
     * @param response Response content
     * @param expirationMinutes Cache expiration in minutes (default: 5)
     */
    void cacheQuestionResponse(String question, String response, int expirationMinutes);
} 