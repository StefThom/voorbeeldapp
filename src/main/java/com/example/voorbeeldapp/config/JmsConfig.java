package com.example.voorbeeldapp.config;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.jms.Queue;

@Configuration
public class JmsConfig {

    @Bean
    public Queue voetballerQueue() {
        return new ActiveMQQueue("voetballerQueue");
    }
}
