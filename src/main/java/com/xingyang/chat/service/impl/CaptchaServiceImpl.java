package com.xingyang.chat.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.xingyang.chat.service.CaptchaService;
import com.xingyang.chat.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

/**
 * Captcha Service Implementation using Hutool
 *
 * @author XingYang
 */
@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {

    @Autowired
    private RedisService redisService;

    @Value("${captcha.expiration:5}")
    private long captchaExpiration;

    private static final String CAPTCHA_CODE_KEY = "captcha:code:";
    
    // 验证码图片宽度
    private static final int WIDTH = 160;
    // 验证码图片高度
    private static final int HEIGHT = 60;
    // 验证码字符数
    private static final int CODE_COUNT = 4;

    @Override
    public BufferedImage generateCaptcha(String uuid) {
        log.info("Generating captcha for UUID: {}", uuid);
        
        try {
            // 使用hutool的CaptchaUtil生成线条干扰验证码
            LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(WIDTH, HEIGHT, CODE_COUNT, 150);
            
            // 获取验证码文本
            String captchaText = lineCaptcha.getCode();
            log.info("Generated captcha text: {}", captchaText);
            
            // 存储验证码到Redis
            String key = CAPTCHA_CODE_KEY + uuid;
            boolean stored = redisService.setCacheObject(key, captchaText, captchaExpiration, TimeUnit.MINUTES);
            log.info("Stored captcha in Redis with key: {}, expiration: {} minutes, success: {}", 
                    key, captchaExpiration, stored);
            
            // 返回验证码图片
            return lineCaptcha.getImage();
        } catch (Exception e) {
            log.error("Error generating captcha with Hutool: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean validateCaptcha(String uuid, String captchaCode) {
        if (uuid == null || captchaCode == null) {
            log.warn("UUID or captcha code is null, uuid: {}, captchaCode: {}", uuid, captchaCode);
            return false;
        }
        
        log.info("Validating captcha for UUID: {}, code: {}", uuid, captchaCode);
        
        try {
            // Get the stored captcha code from Redis
            String key = CAPTCHA_CODE_KEY + uuid;
            String storedCaptchaCode = redisService.getCacheObject(key);
            
            if (storedCaptchaCode == null) {
                log.warn("No captcha found in Redis for UUID: {}", uuid);
                return false;
            }
            
            log.info("Retrieved captcha from Redis: {}", storedCaptchaCode);
            
            // Delete the captcha code from Redis after retrieving (one-time use)
            boolean deleted = redisService.deleteObject(key);
            log.info("Deleted captcha from Redis for UUID: {}, success: {}", uuid, deleted);
            
            // Case insensitive comparison - 清理输入验证码中的空格
            captchaCode = captchaCode.trim();
            boolean result = storedCaptchaCode.equalsIgnoreCase(captchaCode);
            log.info("Captcha validation result for UUID {}: {}, stored: '{}', received: '{}'", 
                    uuid, result, storedCaptchaCode, captchaCode);
            
            return result;
        } catch (Exception e) {
            log.error("Error validating captcha: {}", e.getMessage(), e);
            return false;
        }
    }
} 