package com.hyman.springbootwar.redisDemo;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * 除了String类型，实战中我们还经常会在Redis中存储对象，这时候我们就会想是否可以使用类似RedisTemplate<String, User>来初始化并进行操作。
 * 但是Spring Boot并不支持直接使用，需要我们自己实现 RedisSerializer<T> 即序列化接口来对传入对象进行序列化和反序列化。
 *
 * converter ：变流器，转化器；
 */
public class RedisObjectSerializer implements RedisSerializer<Object>{

    // 序列化对象
    private Converter<Object,byte[]> serializer = new SerializingConverter();
    // 反序列化对象
    private Converter<byte[],Object> deserializer = new DeserializingConverter();
    // 定义一个静态的二进制数组，用于存储转换的数据
    static final byte[] EMPTY_ARRAY = new byte[0];


    @Override
    public byte[] serialize(Object o) {
        if(o == null){
            return EMPTY_ARRAY;
        }
        try {
            return serializer.convert(o);
        }catch (Exception e){
            return EMPTY_ARRAY;
        }
    }

    @Override
    public Object deserialize(byte[] bytes) {
        if(isEmpty(bytes)){
            return null;
        }
        try {
            return deserializer.convert(bytes);
        }catch (Exception e){
            throw new SerializationException("不能反序列化为对象",e);
        }
    }

    private boolean isEmpty(byte[] data){
       return (data == null || data.length==0);
    }
}
