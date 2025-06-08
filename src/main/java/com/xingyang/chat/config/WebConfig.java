package com.xingyang.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web Configuration
 *
 * @author XingYang
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置全局CORS设置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    /**
     * 创建一个高优先级的CorsFilter，确保CORS预检请求能够被正确处理
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有源
        config.addAllowedOrigin("*");
        
        // 允许所有头信息
        config.addAllowedHeader("*");
        
        // 允许所有方法
        config.addAllowedMethod("*");
        
        // 允许Cookie等凭证
        config.setAllowCredentials(false);  // 当allowedOrigin为"*"时，必须设置为false
        
        // 预检请求的有效期，单位为秒
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
} 