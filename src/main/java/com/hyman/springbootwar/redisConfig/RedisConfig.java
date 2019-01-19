package com.hyman.springbootwar.redisConfig;

import com.hyman.springbootwar.entity.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.net.UnknownHostException;

@Configuration
public class RedisConfig {

    // 该方法只能用于本地使用（即 localhost），因为其 JedisPool 默认就是 this((String)"localhost", 6379)
    //@Bean
    //JedisConnectionFactory jedisConnectionFactory(){
    //    JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
    //    //jedisConnectionFactory.setPassword("123456");
    //    return jedisConnectionFactory;
    //}

    @Bean
    public RedisTemplate<Object, User> myRedisTemplate(RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {

        RedisTemplate<Object, User> template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);

        // 其底层是使用的 ObjectOutputStream 二进制，所以还是用 json。而 redis 保存对象的机制，默认是使用 JDK 的序列化机制。
        //template.setKeySerializer(new StringRedisSerializer());
        //template.setValueSerializer(new RedisObjectSerializer());

        Jackson2JsonRedisSerializer<User> serializer = new Jackson2JsonRedisSerializer(User.class);
        template.setDefaultSerializer(serializer);
        return template;
    }
}
