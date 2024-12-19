package com.example.thtruemk2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Bean
    public Bot myTelegramBot() {
        return new Bot();
    }
}
