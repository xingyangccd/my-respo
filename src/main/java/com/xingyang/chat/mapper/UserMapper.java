package com.xingyang.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingyang.chat.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * User Mapper
 *
 * @author XingYang
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
} 