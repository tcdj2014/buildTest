package com.txm.test.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQListenerConfig {

    @Bean
    public Queue testQueue() {
        return new Queue("test.queue", true); // durable=true
    }
}