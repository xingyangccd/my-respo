package com.xingyang.chat.util;

import com.xingyang.chat.model.entity.User;
import com.xingyang.chat.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Security Utility
 *
 * @author XingYang
 */
@Component
public class SecurityUtil {

    // Add static reference to UserService
    private static UserService userService;

    private static JwtTokenUtil jwtTokenUtil;
    
    public static void setJwtTokenUtil(JwtTokenUtil jwtTokenUtil) {
        SecurityUtil.jwtTokenUtil = jwtTokenUtil;
    }

    // Constructor injection
    public SecurityUtil(UserService userService) {
        SecurityUtil.userService = userService;
    }

    /**
     * Get current user ID
     *
     * @return User ID or null if not authenticated
     */
    public static Long getCurrentUserId() {
        try {
            // 1. 先尝试从JWT令牌中获取用户ID
            if (jwtTokenUtil != null && RequestContextHolder.getRequestAttributes() != null) {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                String bearerToken = request.getHeader("Authorization");
                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    Long userId = jwtTokenUtil.getUserIdFromToken(bearerToken);
                    if (userId != null) {
                        return userId;
                    }
                }
            }
            
            // 2. 如果从令牌中获取失败，则尝试从SecurityContext中获取用户名，再查询用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            
            // 3. 获取用户名
            String username = null;
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                username = (String) principal;
            }
            
            // 4. 如果获取到用户名，查询用户ID
            if (username != null && userService != null) {
                User user = userService.getUserByUsername(username);
                if (user != null) {
                    return user.getId();
                }
            }
            
            return null;
        } catch (Exception e) {
            // 捕获任何异常，确保不会因为认证逻辑影响正常业务
            return null;
        }
    }
    
    /**
     * Get current username
     *
     * @return Username or null if not authenticated
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        
        return null;
    }
    
    /**
     * Check if current user is authenticated
     *
     * @return True if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
} 