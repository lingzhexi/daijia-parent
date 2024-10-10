package com.atguigu.daijia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MapConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
