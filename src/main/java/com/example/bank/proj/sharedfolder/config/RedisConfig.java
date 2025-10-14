package com.example.bank.proj.sharedfolder.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.example.bank.proj.queryfolder.service.AccountReadService;
import com.example.bank.proj.sharedfolder.config.RedisListenerWrapper;


@Configuration
public class RedisConfig {

    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            List<RedisListenerWrapper> listenerWrappers) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // Thread pool
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.initialize();

        for (RedisListenerWrapper wrapper : listenerWrappers) {
            container.addMessageListener(wrapper.getAdapter(), new ChannelTopic(wrapper.getTopic()));
            System.out.println("✅ Subscribed adapter to topic: " + wrapper.getTopic());
        }

        return container;
    }

    @Bean
    public MessageListenerAdapter accountCreatedAdapter(AccountReadService accountReadService) {
        return new MessageListenerAdapter(accountReadService, "processAccountCreatedEvent");
    }

    @Bean
    public MessageListenerAdapter moneyDepositAdapter(AccountReadService accountReadService) {
        return new MessageListenerAdapter(accountReadService, "moneyDepositEvent");
    }

    @Bean
    public RedisListenerWrapper accountCreatedListener(MessageListenerAdapter accountCreatedAdapter) {
        return new RedisListenerWrapper(accountCreatedAdapter, "accountEvents");
    }

    @Bean
    public RedisListenerWrapper moneyDepositListener(MessageListenerAdapter moneyDepositAdapter) {
        return new RedisListenerWrapper(moneyDepositAdapter, "depositEvents");
    }
}