package com.example.bank.proj.sharedfolder.subscribers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisSubscriberConfig {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public RedisMessageListenerContainer container(MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(listenerAdapter, new ChannelTopic("myChannel"));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapterr(MySubscriber subscriber) {
        // handleMessage method will be called
        return new MessageListenerAdapter(subscriber, "handleMessage");
    }

    @Bean
    public MySubscriber subscriber() {
        return new MySubscriber();
    }

    public static class MySubscriber {
        public void handleMessage(String message) {
            System.out.println("Received message: " + message);
        }
    }
}
