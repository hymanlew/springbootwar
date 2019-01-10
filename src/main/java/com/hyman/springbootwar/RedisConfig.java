package com.hyman.springbootwar;

import com.hyman.springbootwar.entity.User;
import com.hyman.springbootwar.redisDemo.RedisObjectSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // 该注解会使 spring boot 自动生成 bean，并注入到代表它的变量参数中
    @Bean
    JedisConnectionFactory jedisConnectionFactory(){
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setPassword("123456");
        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String,User> redisTemplate(RedisConnectionFactory redisConnectionFactory){

        RedisTemplate<String,User> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new RedisObjectSerializer());
        return template;
    }
}
