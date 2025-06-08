package com.xingyang.chat.config;

import com.xingyang.chat.util.JwtTokenUtil;
import com.xingyang.chat.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;

/**
 * 配置类，用于初始化SecurityUtil中的jwtTokenUtil
 *
 * @author XingYang
 */
@Configuration
public class SecurityUtilConfig {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostConstruct
    public void init() {
        SecurityUtil.setJwtTokenUtil(jwtTokenUtil);
    }
} 