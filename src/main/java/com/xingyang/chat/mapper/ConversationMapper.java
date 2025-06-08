package com.xingyang.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyang.chat.model.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Conversation Mapper Interface
 *
 * @author XingYang
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

    /**
     * Find conversations by user ID with pagination
     *
     * @param page Page parameters
     * @param userId User ID
     * @return Page of conversations
     */
    @Select("SELECT * FROM conversation WHERE user_id = #{userId} AND deleted = 0 ORDER BY update_time DESC")
    Page<Conversation> findByUserId(Page<?> page, @Param("userId") Long userId);
} 