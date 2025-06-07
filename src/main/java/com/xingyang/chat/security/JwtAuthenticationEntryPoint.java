package com.xingyang.chat.security;

import com.alibaba.fastjson.JSON;
import com.xingyang.chat.model.vo.Result;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Handle authentication exceptions for unauthorized requests
 *
 * @author XingYang
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        Result<?> result = Result.error(Result.ResultCode.UNAUTHORIZED.getCode(), 
                                      authException.getMessage() != null ? 
                                      authException.getMessage() : "Unauthorized access");
        
        response.getWriter().write(JSON.toJSONString(result));
    }
} 