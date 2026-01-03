package com.example.bankcards.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

    @Bean
    public CardNumberEncryptor cardNumberEncryptor() {
        return new CardNumberEncryptor();
    }
}