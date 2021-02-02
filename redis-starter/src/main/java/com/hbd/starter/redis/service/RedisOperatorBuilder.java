package com.hbd.starter.redis.service;

import com.hbd.starter.redis.constans.RedisDBEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis多库配置表
 * created by hbd
 * 2019-01-10
 */
public class RedisOperatorBuilder {

    private Logger logger = LoggerFactory.getLogger(RedisOperatorBuilder.class);

    public static String host = "127.0.0.1";
    public static Integer port = 6379;
    public static String password = null;

    /**
     * redis数据库连接映射表
     */
    private static Map<Integer, RedisOperator> connectionMap = new HashMap();

    /**
     * 获取默认Redis默认库
     * @return
     */
    public static RedisOperator getInstance() {
        return getInstance(RedisDBEnum.DEFAULT);
    }

    /**
     * 根据模块获取redis 不同db连接
     * @param dbEnum
     * @return
     */
    public synchronized static RedisOperator getInstance(RedisDBEnum dbEnum) {
        if(connectionMap.containsKey(dbEnum.getIndex())) {
            return connectionMap.get(dbEnum.getIndex());
        }
        RedisSerializer stringSerializer = new StringRedisSerializer();
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        if("json".equals(dbEnum.getSerializer())) {
            redisTemplate.setValueSerializer(stringSerializer);
            redisTemplate.setHashValueSerializer(stringSerializer);
        }
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(host, port);
        lettuceConnectionFactory.setPassword(password);
        lettuceConnectionFactory.setDatabase(dbEnum.getIndex());
        lettuceConnectionFactory.afterPropertiesSet();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        redisTemplate.afterPropertiesSet();
        lettuceConnectionFactory.resetConnection();
        RedisOperator redisOperator = new RedisOperator();
        redisOperator.setRedisTemplate(redisTemplate);
        redisOperator.setRedisDBEnum(dbEnum);
        connectionMap.put(dbEnum.getIndex(), redisOperator);
        return redisOperator;
    }

    /*
    public static RedisClusterConfiguration createRedisClusterConfiguration() {
        List<String> hostAndPorts = Arrays.asList("10.9.40.211:7000,10.9.40.211:7001,10.9.40.211:7002,10.9.40.211:7003,10.9.40.211:7004,10.9.40.211:7005".split(","));
        RedisClusterConfiguration configuration = new RedisClusterConfiguration(hostAndPorts);
        configuration.setPassword(RedisPassword.of(""));
        return configuration;
    }*/

}
