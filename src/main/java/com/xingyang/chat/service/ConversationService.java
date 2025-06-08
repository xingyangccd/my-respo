package com.xingyang.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xingyang.chat.model.dto.ChatMessageDto;
import com.xingyang.chat.model.dto.ChatRequestDto;
import com.xingyang.chat.model.dto.ConversationDto;
import com.xingyang.chat.model.entity.Conversation;
import com.xingyang.chat.model.entity.ChatMessage;

import java.util.List;

/**
 * Conversation Service Interface
 *
 * @author XingYang
 */
public interface ConversationService extends IService<Conversation> {

    /**
     * Create a new conversation
     *
     * @param title Conversation title
     * @param modelId Model ID
     * @return Conversation DTO
     */
    ConversationDto createConversation(String title, String modelId);
    
    /**
     * Get conversation by ID
     *
     * @param id Conversation ID
     * @return Conversation DTO
     */
    ConversationDto getConversationById(Long id);
    
    /**
     * Get conversations by user ID
     *
     * @param page Page number
     * @param size Page size
     * @return Page of conversation DTOs
     */
    Page<ConversationDto> getConversationsByUserId(Integer page, Integer size);
    
    /**
     * Update conversation title
     *
     * @param id Conversation ID
     * @param title New title
     * @return Updated conversation DTO
     */
    ConversationDto updateConversationTitle(Long id, String title);
    
    /**
     * Delete conversation
     *
     * @param id Conversation ID
     * @return Success status
     */
    boolean deleteConversation(Long id);
    
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
     * Save chat request and response to conversation
     *
     * @param conversationId Conversation ID
     * @param request Chat request
     * @param response AI response content
     * @return Conversation DTO
     */
    ConversationDto saveChat(Long conversationId, ChatRequestDto request, String response);
} 