package com.hyman.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Starter 组件主要的功能是自动完成装配，分为三种：
 * 包括相关的 jar 包依赖，bean 自动装配，自动声明并加载 application.properties 配置文件中的属性配置。
 *
 * 必须要在 resources 下创建 META-INF/spring.factories 文件，使用 spring-boot 程序可以扫描到该文件，完成自动装配。
 *
 * 自动配置类
 */
@Configuration
@ConditionalOnClass(HelloProperties.class)
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
