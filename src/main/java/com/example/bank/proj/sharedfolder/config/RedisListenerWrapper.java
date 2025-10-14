package com.example.bank.proj.sharedfolder.config;

import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

public class RedisListenerWrapper {
    private final MessageListenerAdapter adapter;
    private final String topic;

    public RedisListenerWrapper(MessageListenerAdapter adapter, String topic) {
        this.adapter = adapter;
        this.topic = topic;
    }

    public MessageListenerAdapter getAdapter() {
        return adapter;
    }

    public String getTopic() {
        return topic;
    }
}
