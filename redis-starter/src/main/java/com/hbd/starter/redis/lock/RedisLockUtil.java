package com.hbd.starter.redis.lock;

import com.hbd.starter.redis.properties.RedissonProperties;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * redis分布式锁工具
 * created by hbd
 * 2019-06-03
 */
public class RedisLockUtil {

    private static Logger logger = LoggerFactory.getLogger(RedisLockUtil.class);

    /**
     * 分布式锁名前缀
     */
    public static final String DISTRIBUTED_SYNCHRONIZATION_LOCK = "SYN_LOCK_";

    private static RedissonClient redissonClient;

    private static RedissonProperties redissonProperties;

    /**
     * 初始化
     * @param redissonClient
     * @param redissonProperties
     */
    public static void init(RedissonClient redissonClient, RedissonProperties redissonProperties) {
        RedisLockUtil.redissonClient = redissonClient;
        RedisLockUtil.redissonProperties = redissonProperties;
    }

    /**
     * 判断锁是否存在 如果存在返回false 如果不存在，生成一个锁，并且返回true
     *
     * @param lockKey
     *            锁的名称
     * @return
     */
    public static boolean tryLock(String lockKey) {
        RedissonProperties.Lock lock = redissonProperties.getLock();
        return tryLock(lock.getWaitTime(), lock.getLeaseTime(), lockKey);
    }

    /**
     * 释放锁
     *
     * @param lockKey
     *            锁的名称
     */
    public static void unLock(String lockKey) {
        RLock rLock = getLock(lockKey);
        try {
            rLock.unlock();
        } catch (IllegalMonitorStateException e) {
            logger.error("释放锁失败", e);
        }
    }

    public static boolean tryLock(long waitTime, long leaseTime, String lockKey) {
        boolean rtn = false;
        try {
            if(leaseTime <= 0) {
                //leaseTime等于0时，启用看门狗
                rtn = tryLock(waitTime, lockKey);
            } else {
                //到了leaseTime自动释放锁
                RLock rLock = getLock(lockKey);
                rtn = rLock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            logger.error("获取锁失败", e);
        }
        return rtn;
    }

    public static boolean tryLock(long waitTime, String lockKey) {
        boolean rtn = false;
        try {
            RLock rLock = getLock(lockKey);
            rtn = rLock.tryLock(waitTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("获取锁失败", e);
        }
        return rtn;
    }
    /**
     * 获取锁
     *
     * @param lockKey
     *            锁的名称
     * @return
     */
    private static RLock getLock(String lockKey) {
        String key = DISTRIBUTED_SYNCHRONIZATION_LOCK.concat(lockKey);
        return redissonClient.getLock(key);
    }

}
