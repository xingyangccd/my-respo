package com.xingyang.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingyang.chat.model.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Message Mapper Interface
 *
 * @author XingYang
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * Find messages by conversation ID
     *
     * @param conversationId Conversation ID
     * @return List of messages
     */
    @Select("SELECT * FROM message WHERE conversation_id = #{conversationId} AND deleted = 0 ORDER BY create_time ASC")
    List<Message> findByConversationId(@Param("conversationId") Long conversationId);
    
    /**
     * Find similar questions asked within the past day
     *
     * @param content Question content
     * @param threshold Similarity threshold
     * @return List of similar messages
     */
    @Select("SELECT * FROM message WHERE role = 'user' AND deleted = 0 AND " +
            "create_time > DATE_SUB(NOW(), INTERVAL 1 DAY) " +
            "ORDER BY create_time DESC LIMIT 10")
    List<Message> findSimilarQuestions(@Param("content") String content);
} 