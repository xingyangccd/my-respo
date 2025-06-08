package com.xingyang.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingyang.chat.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Chat Message Mapper Interface
 *
 * @author XingYang
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * Find messages by conversation ID
     *
     * @param conversationId Conversation ID
     * @return List of messages
     */
    @Select("SELECT * FROM chat_message WHERE conversation_id = #{conversationId} AND deleted = 0 ORDER BY sequence ASC")
    List<ChatMessage> findByConversationId(@Param("conversationId") Long conversationId);
    
    /**
     * Get the max sequence number for a conversation
     *
     * @param conversationId Conversation ID
     * @return Max sequence number
     */
    @Select("SELECT COALESCE(MAX(sequence), 0) FROM chat_message WHERE conversation_id = #{conversationId} AND deleted = 0")
    Integer getMaxSequence(@Param("conversationId") Long conversationId);
} 