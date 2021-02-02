package com.hbd.starter.redis.lock.annotation;

import com.hbd.starter.redis.lock.enums.RedisLockKeyStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式锁注解,在需要做同步的方法上加注解
 * created by hbd
 * 2019-06-03
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RedisLock {

    /**
     * 锁的唯一键名称
     * @return
     */
    String lockKey() default "";

    /**
     * 锁唯一键策略，当策略设置为CUSTOM，锁名取值为lockKey属性的值
     * @return
     */
    RedisLockKeyStrategy lockStrategy() default RedisLockKeyStrategy.CUSTOM;

    /**
     * 获取锁的超时时间
     * @return
     */
    long waitTime() default 0;

    /**
     * 锁的有效时间
     * @return
     */
    long leaseTime() default 10000;

    /**
     * 参数的位置，当LockKeyStrategy为PARAM_BASE或PARAM_OBJECT时启用
     * @return
     */
    int paramPosition() default 0;

    /**
     * 参数的名称，当LockKeyStrategy为PARAM_OBJECT时启用，
     * 如果参数对象是组合嵌套的对象，参数名可以加.对象导航，
     * 例子：1、普通对象属性name
     *       2、嵌套对象属性store.name
     * @return
     */
    String paramName() default "";
}
