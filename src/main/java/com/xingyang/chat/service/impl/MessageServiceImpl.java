package com.xingyang.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xingyang.chat.exception.BusinessException;
import com.xingyang.chat.mapper.MessageMapper;
import com.xingyang.chat.model.dto.ChatMessageDto;
import com.xingyang.chat.model.entity.Conversation;
import com.xingyang.chat.model.entity.Message;
import com.xingyang.chat.service.ConversationService;
import com.xingyang.chat.service.MessageService;
import com.xingyang.chat.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Message Service Implementation
 *
 * @author XingYang
 */
@Slf4j
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String CHAT_CACHE_PREFIX = "chat:qa:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageDto addMessage(Long conversationId, String role, String content) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("User not authenticated");
        }
        
        // Validate conversation
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException("Conversation not found or access denied");
        }
        
        // Create message
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setTokenCount(estimateTokenCount(content));
        message.setType("text"); // Default type
        
        if (conversation.getModelId() != null) {
            message.setModelId(conversation.getModelId());
        }
        
        // Save message
        boolean saved = this.save(message);
        if (!saved) {
            throw new BusinessException("Failed to add message");
        }
        
        // Update conversation last update time
        conversationService.updateById(conversation);
        
        // If this is an assistant response to a user question, cache it
        if ("assistant".equals(role)) {
            List<Message> conversationMessages = this.baseMapper.findByConversationId(conversationId);
            
            // Find the last user message before this assistant message
            Optional<Message> lastUserMessage = conversationMessages.stream()
                    .filter(m -> "user".equals(m.getRole()) && m.getCreateTime().isBefore(message.getCreateTime()))
                    .reduce((first, second) -> second);
            
            lastUserMessage.ifPresent(userMsg -> {
                // Cache the question and response
                cacheQuestionResponse(userMsg.getContent(), content, 5);
            });
        }
        
        // Convert to DTO and return
        return convertToChatMessageDto(message);
    }

    @Override
    public List<ChatMessageDto> getMessagesByConversationId(Long conversationId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("User not authenticated");
        }
        
        // Validate conversation
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException("Conversation not found or access denied");
        }
        
        // Get messages
        List<Message> messages = this.baseMapper.findByConversationId(conversationId);
        
        // Convert to DTOs
        return messages.stream()
                .map(this::convertToChatMessageDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<String> findCachedResponse(String question) {
        if (question == null || question.trim().isEmpty()) {
            return Optional.empty();
        }
        
        // Normalize the question
        String normalizedQuestion = normalizeQuestion(question);
        String cacheKey = CHAT_CACHE_PREFIX + normalizedQuestion.hashCode();
        
        // Try to get from cache
        String cachedResponse = redisTemplate.opsForValue().get(cacheKey);
        
        log.debug("Cache lookup for question: {}, found: {}", normalizedQuestion, cachedResponse != null);
        
        return Optional.ofNullable(cachedResponse);
    }

    @Override
    public void cacheQuestionResponse(String question, String response, int expirationMinutes) {
        if (question == null || question.trim().isEmpty() || response == null) {
            return;
        }
        
        // Normalize the question
        String normalizedQuestion = normalizeQuestion(question);
        String cacheKey = CHAT_CACHE_PREFIX + normalizedQuestion.hashCode();
        
        // Cache the response
        redisTemplate.opsForValue().set(cacheKey, response, expirationMinutes, TimeUnit.MINUTES);
        
        log.debug("Cached response for question: {}, expiration: {} minutes", normalizedQuestion, expirationMinutes);
    }
    
    /**
     * Convert message entity to DTO
     */
    private ChatMessageDto convertToChatMessageDto(Message message) {
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
    
    /**
     * Normalize a question for caching purposes
     * Removes extra whitespace, converts to lowercase, etc.
     */
    private String normalizeQuestion(String question) {
        if (question == null) {
            return "";
        }
        
        // Simple normalization: lowercase and trim whitespace
        return question.toLowerCase().trim().replaceAll("\\s+", " ");
    }
} 