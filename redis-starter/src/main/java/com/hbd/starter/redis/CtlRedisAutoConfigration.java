package com.hbd.starter.redis;

import com.hbd.starter.redis.lock.RedisLockUtil;
import com.hbd.starter.redis.lock.aspect.RedisLockAspect;
import com.hbd.starter.redis.properties.RedisProperties;
import com.hbd.starter.redis.properties.RedissonProperties;
import com.hbd.starter.redis.service.*;
import com.hbd.starter.redis.service.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

/**
 * @Description: redis自动配置类
 * @Author yangqh
 * @Date 15:57 2018/12/21
 **/
@Configuration
@Import({CtlRedisAutoConfigration.RedisAutoConfigure.class,
        CtlRedisAutoConfigration.RedissonAutoConfiguration.class,
        RedisLockAspect.class,
        RedisOperator.class})
@EnableConfigurationProperties({RedissonProperties.class, RedisProperties.class})
@Slf4j
public class CtlRedisAutoConfigration {

    /**
     * @Description: redis
     **/
    @Configuration
    @ConditionalOnClass(RedisTemplate.class)
    @ConfigurationProperties(prefix = "spring.redis")
    public class RedisAutoConfigure {
        @Bean
        public RedisTemplate setRedisTemplate(RedisTemplate redisTemplate) {
            StringRedisSerializer stringSerializer = new StringRedisSerializer();
            redisTemplate.setValueSerializer(stringSerializer);
            redisTemplate.setHashValueSerializer(stringSerializer);
            redisTemplate.setKeySerializer(stringSerializer);
            redisTemplate.setHashKeySerializer(stringSerializer);
            log.info("RedisTemplate init");
            return redisTemplate;
        }

    }

    /**
     * @Description: 分布式锁
     **/
    @Configuration
    @EnableConfigurationProperties({RedissonProperties.class})
    @ConditionalOnProperty(name = {"ctl.synchronization-lock.redisson.enabled"}, havingValue = "true", matchIfMissing = false)
    public class RedissonAutoConfiguration {

        @Bean(destroyMethod = "shutdown")
        public RedissonClient redissonClient(RedissonProperties properties) {
            Config config = new Config();
            config.setLockWatchdogTimeout(20000);//锁超时检查20秒
            if (StringUtils.isEmpty(properties.getAddress())) {
                ClusterServersConfig clusterServersConfig = config.useClusterServers();
                clusterServersConfig.setScanInterval(2000);
                clusterServersConfig.addNodeAddress(properties.getNodeAddresses().toArray(new String[properties.getNodeAddresses().size()]));
                if (properties.getConnectionPoolSize() != null) {
                    clusterServersConfig.setMasterConnectionPoolSize(properties.getConnectionPoolSize());
                    clusterServersConfig.setSlaveConnectionPoolSize(properties.getConnectionPoolSize());
                }
                clusterServersConfig.setPingConnectionInterval(properties.getPingConnectionInterval());
            } else {
                // 只设置地址跟密码，其他配置默认
                SingleServerConfig singleServerConfig = config.useSingleServer();
                singleServerConfig.setAddress(properties.getAddress());
                if (properties.getConnectionPoolSize() != null) {
                    singleServerConfig.setConnectionPoolSize(properties.getConnectionPoolSize());
                }
                if (!StringUtils.isEmpty(properties.getPassword())) {
                    singleServerConfig.setPassword(properties.getPassword());
                }
                singleServerConfig.setPingConnectionInterval(properties.getPingConnectionInterval());
            }
            RedissonClient redissonClient = Redisson.create(config);
            RedisLockUtil.init(redissonClient, properties);
            log.info("RedissonClient init");
            return redissonClient;
        }
    }

    @Primary
    @Bean
    public LockService lockService() {
        return new AbstractLockService() {
        };
    }

    @Bean
    public RedisOperatorBuilder redisOperatorBuilder(RedisProperties redisProperties) {
        log.info("===============>redis host:"+redisProperties.getHost());
        System.out.println("===============>redis host:"+redisProperties.getHost());
        RedisOperatorBuilder.password = redisProperties.getPassword() == null ? RedisOperatorBuilder.password : redisProperties.getPassword();
        RedisOperatorBuilder.host = redisProperties.getHost() == null ? RedisOperatorBuilder.host : redisProperties.getHost();
        RedisOperatorBuilder.port = redisProperties.getPort() == null ? RedisOperatorBuilder.port : redisProperties.getPort();
        System.out.println("===============>RedisOperatorBuilder.host:"+RedisOperatorBuilder.host);
        return new RedisOperatorBuilder();
    }

    @Bean
    public RedisUtil redisOpt(RedisTemplate redisTemplate) {
        RedisUtil.redisTemplate = redisTemplate;
        return new RedisUtil();
    }

}
