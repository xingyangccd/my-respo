package com.xingyang.chat.service.impl;

import com.xingyang.chat.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Redis服务实现类
 *
 * @author XingYang
 */
@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public <T> void setCacheObject(final String key, final T value, final long timeout, final TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCacheObject(final String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    @Override
    public boolean deleteObject(final String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    @Override
    public long deleteObject(final Collection<String> collection) {
        return redisTemplate.delete(collection);
    }
} 