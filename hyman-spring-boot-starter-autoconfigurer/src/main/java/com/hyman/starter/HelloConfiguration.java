package com.hyman.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 自动配置类
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties({HelloProperties.class})
public class HelloConfiguration {

    @Autowired
    private HelloProperties properties;

    @Bean
    public HelloService getservice(){
        HelloService service = new HelloService();
        service.setProperties(properties);
        return service;
    }
}
