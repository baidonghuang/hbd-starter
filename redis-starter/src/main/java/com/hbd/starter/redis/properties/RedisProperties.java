package com.hbd.starter.redis.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Redisson属性类
 * 
 */
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties {
    
    /**
     * 地址
     */
    private String host;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 端口
     */
    private Integer port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}