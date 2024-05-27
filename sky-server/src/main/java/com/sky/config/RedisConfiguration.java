package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 配置类，redis的配置类
 */
@Slf4j
//@Configuration
public class RedisConfiguration {
//    @Bean
//    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        log.info("开始创建redis模板对象......");
//        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
//        //设置redis连接工厂对象
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        //设置key的序列化方式
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        return redisTemplate;
//    }
}
