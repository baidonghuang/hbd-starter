package com.hbd.starter.redis.lock.enums;

/**
 * 锁名策略枚举
 * created by hbd
 * 2019-06-03
 */
public enum RedisLockKeyStrategy {
    //自定义锁名
    CUSTOM,
    //取方法的传参值，基本类型
    PARAM_BASE,
    //去方法的传参值，复杂对象类型
    PARAM_OBJECT;

    private RedisLockKeyStrategy() {
    }
}
