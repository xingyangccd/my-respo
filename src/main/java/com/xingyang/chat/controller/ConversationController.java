package com.xingyang.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyang.chat.model.dto.ChatMessageDto;
import com.xingyang.chat.model.dto.ConversationDto;
import com.xingyang.chat.model.vo.Result;
import com.xingyang.chat.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Conversation Controller
 *
 * @author XingYang
 */
@Slf4j
@RestController
@RequestMapping("/conversation")
@Tag(name = "Conversation API", description = "Manage chat conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    /**
     * Create new conversation
     *
     * @param params Request parameters
     * @return New conversation
     */
    @PostMapping
    @Operation(summary = "Create conversation", description = "Create a new chat conversation")
    public Result<ConversationDto> createConversation(@RequestBody Map<String, String> params) {
        try {
            String title = params.get("title");
            String modelId = params.get("modelId");
            
            log.info("Creating new conversation with title: {}, modelId: {}", title, modelId);
            
            ConversationDto conversation = conversationService.createConversation(title, modelId);
            
            // 确认返回的会话对象是否有效
            if (conversation == null) {
                log.error("Service returned null conversation object");
                return Result.error(500, "Failed to create conversation: null object returned");
            }
            
            log.info("Conversation created successfully with ID: {}", conversation.getId());
            
            // 返回成功结果，包含会话对象
            Result<ConversationDto> result = Result.success(conversation);
            log.info("Returning result with data: {}", result.getData());
            log.info("Full result: {}", result);
            
            return result;
        } catch (Exception e) {
            log.error("Failed to create conversation", e);
            return Result.error(500, "Failed to create conversation: " + e.getMessage());
        }
    }

    /**
     * Get conversation by ID
     *
     * @param id Conversation ID
     * @return Conversation with messages
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get conversation", description = "Get a conversation by ID")
    public Result<ConversationDto> getConversation(
            @Parameter(description = "Conversation ID") @PathVariable Long id) {
        try {
            ConversationDto conversation = conversationService.getConversationById(id);
            return Result.success(conversation);
        } catch (Exception e) {
            log.error("Failed to get conversation", e);
            return Result.error(500, "Failed to get conversation: " + e.getMessage());
        }
    }

    /**
     * Get all conversations for current user
     *
     * @param page Page number
     * @param size Page size
     * @return Page of conversations
     */
    @GetMapping("/list")
    @Operation(summary = "List conversations", description = "Get all conversations for current user")
    public Result<Page<ConversationDto>> listConversations(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size) {
        try {
            log.info("Listing conversations for current user, page: {}, size: {}", page, size);
            
            Page<ConversationDto> conversations = conversationService.getConversationsByUserId(page, size);
            
            log.info("Found {} conversations on page {} (total: {})", 
                    conversations.getRecords().size(), 
                    conversations.getCurrent(), 
                    conversations.getTotal());
            
            if (conversations.getRecords().isEmpty()) {
                log.warn("No conversations found for current user");
            } else {
                log.info("First conversation ID: {}, title: {}", 
                        conversations.getRecords().get(0).getId(),
                        conversations.getRecords().get(0).getTitle());
            }
            
            Result<Page<ConversationDto>> result = Result.success(conversations);
            log.debug("Returning conversations result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Failed to list conversations", e);
            return Result.error(500, "Failed to list conversations: " + e.getMessage());
        }
    }

    /**
     * Update conversation title
     *
     * @param id Conversation ID
     * @param params Request parameters
     * @return Updated conversation
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update conversation", description = "Update a conversation's title")
    public Result<ConversationDto> updateConversation(
            @Parameter(description = "Conversation ID") @PathVariable Long id,
            @RequestBody Map<String, String> params) {
        try {
            String title = params.get("title");
            if (title == null || title.isEmpty()) {
                return Result.error(400, "Title is required");
            }
            
            ConversationDto conversation = conversationService.updateConversationTitle(id, title);
            return Result.success(conversation);
        } catch (Exception e) {
            log.error("Failed to update conversation", e);
            return Result.error(500, "Failed to update conversation: " + e.getMessage());
        }
    }

    /**
     * Delete conversation
     *
     * @param id Conversation ID
     * @return Success status
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete conversation", description = "Delete a conversation")
    public Result<Boolean> deleteConversation(
            @Parameter(description = "Conversation ID") @PathVariable Long id) {
        try {
            boolean deleted = conversationService.deleteConversation(id);
            return Result.success(deleted);
        } catch (Exception e) {
            log.error("Failed to delete conversation", e);
            return Result.error(500, "Failed to delete conversation: " + e.getMessage());
        }
    }

    /**
     * Get messages for a conversation
     *
     * @param conversationId Conversation ID
     * @return List of messages
     */
    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "Get messages", description = "Get all messages for a conversation")
    public Result<List<ChatMessageDto>> getMessages(
            @Parameter(description = "Conversation ID") @PathVariable Long conversationId) {
        try {
            log.info("Getting messages for conversation ID: {}", conversationId);
            
            List<ChatMessageDto> messages = conversationService.getMessagesByConversationId(conversationId);
            
            log.info("Found {} messages for conversation ID: {}", messages.size(), conversationId);
            
            if (messages.isEmpty()) {
                log.warn("No messages found for conversation ID: {}", conversationId);
            } else {
                log.info("First message: role={}, content preview='{}'", 
                        messages.get(0).getRole(),
                        messages.get(0).getContent().length() > 50 
                            ? messages.get(0).getContent().substring(0, 50) + "..." 
                            : messages.get(0).getContent());
            }
            
            Result<List<ChatMessageDto>> result = Result.success(messages);
            log.debug("Returning messages result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get messages for conversation ID: {}", conversationId, e);
            return Result.error(500, "Failed to get messages: " + e.getMessage());
        }
    }

    /**
     * Add message to conversation
     *
     * @param conversationId Conversation ID
     * @param message Message DTO
     * @return Added message
     */
    @PostMapping("/{conversationId}/messages")
    @Operation(summary = "Add message", description = "Add a message to a conversation")
    public Result<ChatMessageDto> addMessage(
            @Parameter(description = "Conversation ID") @PathVariable Long conversationId,
            @RequestBody ChatMessageDto message) {
        try {
            if (message.getRole() == null || message.getContent() == null) {
                return Result.error(400, "Role and content are required");
            }
            
            ChatMessageDto addedMessage = conversationService.addMessage(
                    conversationId, message.getRole(), message.getContent());
            return Result.success(addedMessage);
        } catch (Exception e) {
            log.error("Failed to add message", e);
            return Result.error(500, "Failed to add message: " + e.getMessage());
        }
    }
} 