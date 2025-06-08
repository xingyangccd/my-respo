package com.xingyang.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xingyang.chat.exception.BusinessException;
import com.xingyang.chat.mapper.ChatMessageMapper;
import com.xingyang.chat.mapper.ConversationMapper;
import com.xingyang.chat.model.dto.ChatMessageDto;
import com.xingyang.chat.model.dto.ChatRequestDto;
import com.xingyang.chat.model.dto.ConversationDto;
import com.xingyang.chat.model.entity.ChatMessage;
import com.xingyang.chat.model.entity.Conversation;
import com.xingyang.chat.service.ConversationService;
import com.xingyang.chat.util.SecurityUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Conversation Service Implementation
 *
 * @author XingYang
 */
@Slf4j
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationDto createConversation(String title, String modelId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("User not authenticated");
        }
        
        log.info("Creating new conversation for user: {}, title: {}, modelId: {}", userId, title, modelId);
        
        Conversation conversation = new Conversation();
        conversation.setTitle(title != null ? title : "New Conversation");
        conversation.setUserId(userId);
        
        try {
            // 尝试将modelId解析为Long类型
            conversation.setModelId(modelId == null ? null : Long.valueOf(modelId));
            log.info("Using model ID (parsed): {}", conversation.getModelId());
        } catch (NumberFormatException e) {
            log.warn("Invalid model ID format: {}, using default", modelId);
            conversation.setModelId(1L); // 默认使用ID为1的模型
        }
        
        conversation.setStatus(1); // Active
        
        // Save the conversation
        log.info("Saving conversation entity: {}", conversation);
        boolean saved = this.save(conversation);
        if (!saved) {
            log.error("Failed to save conversation entity");
            throw new BusinessException("Failed to create conversation");
        }
        
        log.info("Conversation saved successfully with ID: {}", conversation.getId());
        
        // Convert to DTO
        ConversationDto dto = new ConversationDto();
        BeanUtils.copyProperties(conversation, dto);
        dto.setModelId(conversation.getModelId() != null ? conversation.getModelId().toString() : modelId);
        dto.setMessages(new ArrayList<>());
        
        log.info("Returning conversation DTO: {}", dto);
        
        return dto;
    }

    @Override
    public ConversationDto getConversationById(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("User not authenticated");
        }
        
        // Get conversation
        Conversation conversation = this.getById(id);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException("Conversation not found or access denied");
        }
        
        // Convert to DTO
        ConversationDto dto = new ConversationDto();
        BeanUtils.copyProperties(conversation, dto);
        if (conversation.getModelId() != null) {
            dto.setModelId(conversation.getModelId().toString());
        }
        
        // Get messages
        List<ChatMessage> messages = chatMessageMapper.findByConversationId(id);
        dto.setMessages(messages.stream()
                .map(this::convertToChatMessageDto)
                .collect(Collectors.toList()));
        
        return dto;
    }

    @Override
    public Page<ConversationDto> getConversationsByUserId(Integer page, Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("User not authenticated");
        }
        
        log.info("Fetching conversations for user ID: {}, page: {}, size: {}", userId, page, size);
        
        // Set up pagination
        Page<Conversation> pageParam = new Page<>(page, size);
        
        // Get conversations by user ID
        Page<Conversation> conversationPage = baseMapper.findByUserId(pageParam, userId);
        
        log.info("Found {} conversations (total: {}) for user ID: {}", 
                conversationPage.getRecords().size(), 
                conversationPage.getTotal(),
                userId);
        
        // Convert to DTOs
        Page<ConversationDto> dtoPage = new Page<>();
        BeanUtils.copyProperties(conversationPage, dtoPage, "records");
        
        // Convert records
        List<ConversationDto> dtoList = conversationPage.getRecords().stream()
                .map(this::convertToConversationDto)
                .collect(Collectors.toList());
        
        dtoPage.setRecords(dtoList);
        
        log.debug("Returning page of conversations: current={}, size={}, total={}, records={}",
                dtoPage.getCurrent(), dtoPage.getSize(), dtoPage.getTotal(), dtoPage.getRecords().size());
        
        if (dtoPage.getRecords().isEmpty()) {
            log.warn("No conversations found for user ID: {}", userId);
        } else {
            log.debug("First conversation: ID={}, title={}, modelId={}",
                    dtoPage.getRecords().get(0).getId(),
                    dtoPage.getRecords().get(0).getTitle(),
                    dtoPage.getRecords().get(0).getModelId());
        }
        
        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationDto updateConversationTitle(Long id, String title) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("User not authenticated");
        }
        
        // Get conversation
        Conversation conversation = this.getById(id);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException("Conversation not found or access denied");
        }
        
        // Update title
        conversation.setTitle(title);
        boolean updated = this.updateById(conversation);
        if (!updated) {
            throw new BusinessException("Failed to update conversation title");
        }
        
        // Return updated conversation
        return getConversationById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConversation(Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("User not authenticated");
        }
        
        // Get conversation
        Conversation conversation = this.getById(id);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException("Conversation not found or access denied");
        }
        
        // Delete conversation (logical delete)
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageDto addMessage(Long conversationId, String role, String content) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("User not authenticated");
        }
        
        // Validate conversation
        Conversation conversation = this.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException("Conversation not found or access denied");
        }
        
        // Get next sequence number
        Integer sequence = chatMessageMapper.getMaxSequence(conversationId) + 1;
        
        // Create message
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setSequence(sequence);
        message.setTokenCount(estimateTokenCount(content));
        
        if (conversation.getModelId() != null) {
            message.setModelId(conversation.getModelId().toString());
        }
        
        // Save message
        boolean saved = chatMessageMapper.insert(message) > 0;
        if (!saved) {
            throw new BusinessException("Failed to add message");
        }
        
        // Update conversation last update time
        this.updateById(conversation);
        
        // Convert to DTO and return
        return convertToChatMessageDto(message);
    }

    @Override
    public List<ChatMessageDto> getMessagesByConversationId(Long conversationId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("User not authenticated");
        }
        
        log.info("Fetching messages for conversation ID: {}, user ID: {}", conversationId, userId);
        
        // Validate conversation
        Conversation conversation = this.getById(conversationId);
        if (conversation == null) {
            log.warn("Conversation not found: {}", conversationId);
            throw new BusinessException("Conversation not found");
        }
        
        if (!conversation.getUserId().equals(userId)) {
            log.warn("Access denied: conversation {} belongs to user {}, not current user {}", 
                    conversationId, conversation.getUserId(), userId);
            throw new BusinessException("Access denied");
        }
        
        // Get messages
        List<ChatMessage> messages = chatMessageMapper.findByConversationId(conversationId);
        
        log.info("Found {} messages for conversation ID: {}", messages.size(), conversationId);
        
        // Convert to DTOs
        List<ChatMessageDto> messageDtos = messages.stream()
                .map(this::convertToChatMessageDto)
                .collect(Collectors.toList());
        
        if (messageDtos.isEmpty()) {
            log.warn("No messages found for conversation ID: {}", conversationId);
        } else {
            log.debug("First message: role={}, content preview='{}'",
                    messageDtos.get(0).getRole(),
                    messageDtos.get(0).getContent().length() > 50
                            ? messageDtos.get(0).getContent().substring(0, 50) + "..."
                            : messageDtos.get(0).getContent());
        }
        
        return messageDtos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationDto saveChat(Long conversationId, ChatRequestDto request, String response) {
        // Add user message
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            // Find the last user message
            ChatMessageDto lastUserMessage = request.getMessages().stream()
                    .filter(msg -> "user".equals(msg.getRole()))
                    .reduce((first, second) -> second)
                    .orElse(null);
            
            if (lastUserMessage != null) {
                addMessage(conversationId, "user", lastUserMessage.getContent());
            }
        }
        
        // Add assistant response
        addMessage(conversationId, "assistant", response);
        
        // Return updated conversation
        return getConversationById(conversationId);
    }
    
    /**
     * Convert entity to DTO
     */
    private ConversationDto convertToConversationDto(Conversation conversation) {
        ConversationDto dto = new ConversationDto();
        BeanUtils.copyProperties(conversation, dto);
        
        if (conversation.getModelId() != null) {
            dto.setModelId(conversation.getModelId().toString());
        }
        
        // Don't load messages for list view
        dto.setMessages(new ArrayList<>());
        
        return dto;
    }
    
    /**
     * Convert message entity to DTO
     */
    private ChatMessageDto convertToChatMessageDto(ChatMessage message) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setRole(message.getRole());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getCreateTime().toEpochSecond(java.time.ZoneOffset.UTC) * 1000);
        return dto;
    }
    
    /**
     * Simple token count estimator
     * This is a rough estimate - for more accurate counting, a tokenizer should be used
     */
    private Integer estimateTokenCount(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        
        // Very rough estimate: 1 token ~= 4 characters for English text
        return (int) Math.ceil(content.length() / 4.0);
    }
} 