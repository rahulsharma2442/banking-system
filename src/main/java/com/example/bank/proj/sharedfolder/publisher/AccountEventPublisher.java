package com.example.bank.proj.sharedfolder.publisher;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AccountEventPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;


    public AccountEventPublisher(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishAccountEvent(String topic, Object event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(topic, json);
            System.out.println("📤 Published Account Event: " + json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish Account Event", e);
        }
    }
}
