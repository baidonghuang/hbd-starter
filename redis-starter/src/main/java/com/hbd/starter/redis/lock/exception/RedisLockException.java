package com.hbd.starter.redis.lock.exception;

/**
 * Redis锁异常
 * @Author: hbd
 * @Date: 2019/6/03
 */
public class RedisLockException extends RuntimeException{
    private static final long serialVersionUID = 3455708526465670030L;
    private String code;

    public RedisLockException(String msg){
        super(msg);
    }

    public RedisLockException(String code,String msg){
        super(msg);
        this.code=code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
